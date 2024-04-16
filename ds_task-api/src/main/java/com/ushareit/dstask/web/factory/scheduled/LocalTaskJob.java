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
import com.ushareit.dstask.web.utils.SparkSqlParseUtil;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.util.SeaTunnelParser;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.ushareit.dstask.constant.TemplateEnum.SqlServerHive;

@Slf4j
public class LocalTaskJob extends ScheduledJob{
    private List<DBEtlConfig> tableList;
    private JSONArray tables;
    private boolean existDatabase;
    private String targetDB;
    private Catalog catalog;
    private String location;

    public LocalTaskJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        com.ushareit.engine.param.RuntimeConfig newConfig = JSON.parseObject(task.getRuntimeConfig(), com.ushareit.engine.param.RuntimeConfig.class);
        catalog = newConfig.getCatalog();
        location = newConfig.getTaskParam().getLocation();
        String partitions = context.getRuntimeConfig().getCatalog().getTables().get(0).getPartitions();
        if (StringUtils.isNoneBlank(partitions)){
            ArrayList<String> path = new ArrayList<>();
            Arrays.stream(partitions.split(","))
                    .forEach(partition->{
                        path.add(partition);
                    });
            location = location.replace(String.join("/",path),"");
        }
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
        if (context.getExecuteMode().equals("local")){
            this.dynamicsCmd = true;
            taskItem.put("command_tags", "type:local-submit");
            taskItem.put("workdir", "/neworiental/data/xstream/work");
            taskItem.put("bash_command", buildCommand());
            taskItem.put("task_type", "BashOperator");
            taskItem.put("files",Arrays.asList());
        }else {
            taskItem.put("command_tags", "type:spark-submit-ds");
            taskItem.put("task_type", "KyuubiOperator");
            taskItem.put("command", buildCommand());
        }
        return taskItems;
    }



    @Override
    protected String buildCommand() {
        try {
            if (context.getExecuteMode().equals("local")){
                return "/neworiental/data/xstream/work/"+dataxExecuteShell;
            }else{
                return SparkSqlParseUtil.appendConf2SubimitStr(executeShell, sparkConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(BaseResponseCodeEnum.GET_SEATUNNEL_JOB_SHELL_FAIL);
        }
    }

    @Override
    public void beforeExec() throws Exception {
        createTable(catalog);
    }

    @Override
    public void afterExec() {

    }

    public String getCreateTableSql(Catalog catalog){
        String targetTable = String.format("%s.%s", catalog.getTargetDb(), catalog.getTables().get(0).getTargetTable());
        Table table = catalog.getTables().get(0);
        String schema = table.getColumns().stream().map(column -> {
            String columnType;
            if (column.getColumnType().toLowerCase().startsWith("varchar")) {
                columnType = "string";
            } else {
                columnType = column.getColumnType();
            }
            return String.format("`%s` %s COMMENT '%s'", column.getColumnName(), columnType, column.getColumnComment());
        }).collect(Collectors.joining(","));

        String sql;
        RuntimeConfig.TaskParam taskParam = context.getRuntimeConfig().getTaskParam();
        if(StringUtils.isBlank(table.getPartitions())){
            sql = String.format("CREATE EXTERNAL TABLE IF NOT EXISTS %s (%s) COMMENT '%s' row format delimited\n" +
                            "fields terminated by '\\2'\n" +
                            "STORED AS orc location '%s' TBLPROPERTIES ('orc.compression'='SNAPPY')",targetTable,schema,taskParam.getTableComment(),
                    location);
        }else{
            String partitions = Arrays.stream(table.getPartitions().split(","))
                    .map(partition -> {
                        return String.format("%s string", partition.split("=", 2)[0]);
                    }).collect(Collectors.joining(","));
            sql = String.format("CREATE EXTERNAL TABLE IF NOT EXISTS %s (%s) COMMENT '%s' PARTITIONED BY (%s)  row format delimited\n" +
                            "fields terminated by '\\2' STORED AS orc location '%s' TBLPROPERTIES ('orc.compression'='SNAPPY')"
                    ,targetTable,schema,taskParam.getTableComment(),partitions,location);
        }
        return sql;
    }

    public void createTable(Catalog catalog){
        RuntimeConfig.TaskParam taskParam = context.getRuntimeConfig().getTaskParam();
        String sql = getCreateTableSql(catalog);
        String url = taskServiceImpl.lakecatutil.gatewayUrl +"metadata/table/addCloumnLevel";
        AddColumnVo addColumnVo = new AddColumnVo();
        addColumnVo.setCatalog(region);
        addColumnVo.setDbName(catalog.getTargetDb());
        addColumnVo.setName(catalog.getTables().get(0).getTargetTable());
        List<ColumnDataGrade> columnDataGrades = catalog.getTables().get(0).getColumns().stream().map(
                column -> {
                    ColumnDataGrade columnDataGrade = new ColumnDataGrade();
                    columnDataGrade.setName(column.getColumnName());
                    columnDataGrade.setDataGrade(column.getSecurityLevel());
                    return columnDataGrade;
                }
        ).collect(Collectors.toList());
        ColumnDataGrade columnDataGrade = new ColumnDataGrade();
        columnDataGrade.setDataGrade(taskParam.getTableLevel());
        columnDataGrades.add(columnDataGrade);
        addColumnVo.setColsGrade(columnDataGrades);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            log.info("start auto create table ...");
            log.info(String.format("creat table sql: %s",sql));
            RestTemplate restTemplate = new RestTemplate();
            boolean executeSuccess = SparkSqlParseUtil.executeHiveSql(sql, region, owner);
            if(executeSuccess){
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authentication", InfTraceContextHolder.get().getAuthentication());
                    HttpEntity<AddColumnVo> requestEntity = new HttpEntity<AddColumnVo>(addColumnVo, headers);
                    String response = restTemplate.postForObject(url, requestEntity, String.class);
                    log.info(String.format("接口响应成功:%s",response));
                }catch (Exception e){
                    throw e;
                }
            }
        });
    }



}
