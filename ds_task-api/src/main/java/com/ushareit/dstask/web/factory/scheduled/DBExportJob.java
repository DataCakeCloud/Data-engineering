package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.scheduled.param.AddColumnVo;
import com.ushareit.dstask.web.factory.scheduled.param.ColumnDataGrade;
import com.ushareit.dstask.web.factory.scheduled.param.DBEtlConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.dstask.web.utils.SparkSqlParseUtil;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import com.ushareit.dstask.web.factory.scheduled.param.Column;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.ushareit.dstask.constant.TemplateEnum.SqlServerHive;

@Slf4j
public class DBExportJob extends ScheduledJob{
    private String acrossCloud;
    private List<DBEtlConfig> tableList;
    private JSONArray tables;
    private boolean existDatabase;
    private String targetDB;
    private String lakeHouseType;

    public DBExportJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        acrossCloud = runtimeConfig.getAdvancedParameters().getAcrossCloud().trim();
        List<Table> tables = runtimeConfig.getAdvancedParameters().getTables();
        this.tables =JSONArray.parseArray(JSON.toJSONString(tables));
        String sourceDb = runtimeConfig.getAdvancedParameters().getSourceDb();
        lakeHouseType = runtimeConfig.getAdvancedParameters().getLakeHouseType();
        targetDB = runtimeConfig.getAdvancedParameters().getTargetDb();
        String connectionUrl = runtimeConfig.getAdvancedParameters().getConnectionUrl();
        String dbUser = runtimeConfig.getAdvancedParameters().getDbUser();
        String dbPassword = runtimeConfig.getAdvancedParameters().getDbPassword();
        String dataOriginType = runtimeConfig.getAdvancedParameters().getDataOriginType();
        String sqlStr = task.getContent();
        existDatabase = runtimeConfig.getAdvancedParameters().getExistDatabase();
        tableList = this.tables.stream().map(data -> {
            DBEtlConfig dbEtlConfig = JSON.parseObject(data.toString(), DBEtlConfig.class);

            if (StringUtils.isNotEmpty(dataOriginType) && dataOriginType.equals("sqlserver")) {
                dbEtlConfig.setSourceTable(sourceDb + "." + "dbo." + dbEtlConfig.getSourceTable());
            } else {
                dbEtlConfig.setSourceTable(sourceDb + "." + dbEtlConfig.getSourceTable());
            }
            if("paimon".equalsIgnoreCase(lakeHouseType)){
                dbEtlConfig.setTargetTable("paimon"+"."+targetDB+"."+dbEtlConfig.getTargetTable());
            }else{
                dbEtlConfig.setTargetTable(targetDB+"."+dbEtlConfig.getTargetTable());
            }
            dbEtlConfig.setDbUser(dbUser);
            dbEtlConfig.setDbPassword(dbPassword);
            dbEtlConfig.setConnectionUrl(connectionUrl);
            dbEtlConfig.setColumns(dbEtlConfig.getColumns());
            dbEtlConfig.setAddColumns(dbEtlConfig.getAddColumns());
            dbEtlConfig.setPrimaryKey(dbEtlConfig.getPrimaryKey());
            if (sqlStr != null && !sqlStr.isEmpty()) {
                dbEtlConfig.setSql(sqlStr);
            }

            return dbEtlConfig;
        }).collect(Collectors.toList());

    }


    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", getEngineCommand(false));
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
        return taskItems;
    }



    @Override
    protected String buildCommand() {
        PropertyPreFilters.MySimplePropertyPreFilter excludefilter = new PropertyPreFilters().addFilter();
        String[] EXCLUDE_PROPERTIES = {"targetTablePart"};
        excludefilter.addExcludes(EXCLUDE_PROPERTIES);

        String json = JSON.toJSONString(tableList,excludefilter);
        String s = json.replaceAll("\"", "\\\\\"");//.replaceAll("/转译双引号/", "\\\\\"");
        s = s.replaceAll("},","}, "); // 兼容genie
        return sparkConfig + "\"" + s + "\"" + " "+owner+" "+taskServiceImpl.getTransformedRegion(region)+" "+tenantName;
    }

    @Override
    public void beforeExec() throws Exception {
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        String location = cloudResource.getStorage();
        tableList.forEach(dbEtlConfig->{
            String[] tableDetail = dbEtlConfig.getTargetTable().split("\\.");
            String resLocation = location + String.join("/", Arrays.copyOfRange(tableDetail,1,tableDetail.length));
            dbEtlConfig.setLocation(resLocation);
        });

        tableList.forEach(dbEtlConfig->{
            String sql = getCreateTableSql(dbEtlConfig);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                log.info("start auto create table ...");
                log.info(String.format("creat table sql: %s",sql));
                RestTemplate restTemplate = new RestTemplate();
                boolean executeSuccess = SparkSqlParseUtil.executeHiveSql(sql, region, owner);
            });
            }
        );
    }

    @Override
    public void afterExec() {

    }

    public String getCreateTableSql(DBEtlConfig dbEtlConfig){
        String partitions = dbEtlConfig.getPartitions();
        String[] partition = new String[2];
        ArrayList<Column> columns = (ArrayList<Column>)dbEtlConfig.getColumns();
        List<Column> addColumns = dbEtlConfig.getAddColumns();

        if(!partitions.isEmpty() && partitions != null){
            String[] partitionKV = partitions.split(",")[0].split("=");
            String partitionValue = partitionKV[1];
            if (partitionValue.startsWith("'") || partitionValue.startsWith("\"")) {
                partitionValue = partitionValue.substring(1);
            }
            if (partitionValue.endsWith("'") || partitionValue.endsWith("\"")) {
                partitionValue = partitionValue.substring(0, partitionValue.length() - 1);
            }
            partition[0] = partitionKV[0];
            partition[1] = partitionValue;
            if(addColumns != null){
                addColumns.add(new Column(partition[0],"string","","",null,"",""));
            }else{
                addColumns = Arrays.asList(new Column(partition[0],"string","","",null,"",""));
            }

        }
        List<Column> columnsList = new ArrayList<>();
        ArrayList<Column> cloneColumns = (ArrayList<Column>)columns.clone();
        columnsList.addAll(cloneColumns);

        // 生成查询sql语句
        String targetTable = dbEtlConfig.getTargetTable();
        String sql;
        if(addColumns!=null && !columnsList.containsAll(addColumns)){
            columnsList.addAll(addColumns);
        }

        String schema = getTableSchema(columnsList);
        if("paimon".equalsIgnoreCase(lakeHouseType)){
            if(partitions.isEmpty() || partitions == null){
                sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) TBLPROPERTIES ('file.format' = 'orc','bucket' = '-1')",targetTable,schema);
            }else{
                sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) PARTITIONED BY (%s) TBLPROPERTIES ('file.format' = 'orc','bucket' = '-1')",targetTable,schema,partition[0]);
            }
        }else{
            if(partitions.isEmpty() || partitions == null){
                sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) USING %s LOCATION '%s'",targetTable,schema,lakeHouseType.toLowerCase(),dbEtlConfig.getLocation());
            }else{
                sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) USING %s PARTITIONED BY (%s) LOCATION '%s'",targetTable,schema,lakeHouseType.toLowerCase(),partition[0],dbEtlConfig.getLocation());
            }
        }
        log.info("建标语句："+sql);
        return sql;
    }

    public String getTableSchema(List<Column> cloneColumns){
        String schema = cloneColumns.stream().distinct().map(column -> {
            String columnType;
            if (column.getColumnType().toLowerCase().startsWith("varchar")) {
                columnType = "string";
            } else {
                columnType = column.getColumnType();
            }
            return String.format("%s %s COMMENT '%s'", column.getColumnName(), columnType, column.getComment());
        }).collect(Collectors.joining(","));
        return schema;
    }
}
