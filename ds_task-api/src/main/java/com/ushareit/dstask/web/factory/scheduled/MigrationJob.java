package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Column;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MigrationJob extends ScheduledJob{
    private String sourceRegion;
    private String targetRegion;
    private boolean isAcrossCloud;
    private String table;
    private String partitions;
    private String location;
    List<Column> columns;


    public MigrationJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        sourceRegion = runtimeConfigObject.getString("sourceRegion");
        targetRegion = runtimeConfigObject.getString("targetRegion");
        isAcrossCloud = sourceRegion.equals("sg2") || targetRegion.equals("sg2");
        String database = runtimeConfigObject.getString("targetDb");
        String tableName = runtimeConfigObject.getString("targetTable");
        table = database + "." + tableName;
        String schema = runtimeConfigObject.getString("columns");
        columns = JSON.parseArray(schema, Column.class);
        partitions = runtimeConfigObject.getOrDefault("partitions", "").toString();
        if (partitions.isEmpty()) {
            Dataset dataset = inputDatasets.get(0);
            int offset = dataset.getOffset();
            if (offset >= 0) {
                partitions = "datepart='{{(execution_date + macros.timedelta(days=" + offset + ")).strftime(\"%Y%m%d\")}}'";
            } else {
                partitions = "datepart='{{(execution_date - macros.timedelta(days=" + (-offset) + ")).strftime(\"%Y%m%d\")}}'";
            }
            if ("hourly".equals(dataset.getGranularity())) {
                if (offset >= 0) {
                    partitions += ",hour='{{(execution_date + macros.timedelta(hours=" + offset + ")).strftime(\"%H\")}}'";
                } else {
                    partitions += ",hour='{{(execution_date - macros.timedelta(hours=" + (-offset) + ")).strftime(\"%H\")}}'";
                }
            }
        }

        switch (targetRegion) {
            case "ue1":
                location = DsTaskConstant.UE1_HIVE_LOCATION + database + "/" + tableName;
                break;
            case "sg1":
                location = DsTaskConstant.SG1_HIVE_LOCATION + database + "/" + tableName;
                break;
            case "sg2":
                location = DsTaskConstant.SG2_HIVE_LOCATION + database + "/" + tableName;
                break;
            case "sg3":
                location = DsTaskConstant.SG3_HIVE_LOCATION + database + "/" + tableName;
                break;
            default:
                throw new ServiceException("-1", "不支持的区域");
        }
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if (isAcrossCloud) {
            clusterTags = String.format("type:migration");
        } else {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:spark-submit-ds");
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
        return taskItems;
    }

    @Override
    protected String buildCommand() {
        String args = String.format("-a %s -sourceRegion %s -targetRegion %s -table %s -location %s ",
                isAcrossCloud, sourceRegion, targetRegion, table, location);
        if (partitions != null && !partitions.isEmpty()) {
            args += "-partitions " + partitions;
        }
        return sparkConfig + args;
    }

    @Override
    void setSparkConfig(String resourceLevel, JSONArray params, String batchParams) {
        super.setSparkConfig(resourceLevel, params, batchParams);
        if (isAcrossCloud) {
            TemplateRegionImp templateRegionImp =
                    taskServiceImpl.templateRegionImpService.selectOne(new TemplateRegionImp().setTemplateCode(templateCode).setRegionCode(region));
            this.jarUrl = templateRegionImp.getUrl();
        }
    }

    @Override
    public void beforeExec() throws Exception {
        try {
            taskServiceImpl.olapGateWayUtil.executeBySparkByTendency(getCreateTableSql(), owner, taskServiceImpl.getTransformedRegion(targetRegion), tenantName);
        } catch (SQLException e) {
            throw new ServiceException(BaseResponseCodeEnum.HIVE_TABLE_FAIL, BaseResponseCodeEnum.HIVE_TABLE_FAIL.getMessage(), e.getMessage());
        }
    }


    public String getCreateTableSql(){
        String[] partition = new String[2];
        List<Column> addColumns = null;

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
            addColumns = Arrays.asList(new Column(partition[0],"string","","",null,null,""));

        }
        if(addColumns!=null && !columns.containsAll(addColumns)){
            columns.addAll(addColumns);
        }
        // 生成查询sql语句
        String sql;

        try {
            String schema = getTableSchema(columns);
            sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) USING iceberg PARTITIONED BY (%s) LOCATION '%s'","iceberg."+table,schema,partition[0],location);
        }catch (NullPointerException e){
                throw new ServiceException(BaseResponseCodeEnum.COLUMN_BLANK,"请检查表是否存在");
        }
        log.info("建标语句："+sql);
        return sql;
    }

    public String getTableSchema(List<Column> cloneColumns){
        return cloneColumns.stream().distinct().map(column -> {
            String columnType;
            if (column.getColumnType().toLowerCase().startsWith("varchar")) {
                columnType = "string";
            } else {
                columnType = column.getColumnType();
            }
            return String.format("%s %s COMMENT '%s'", column.getColumnName(), columnType, column.getComment());
        }).collect(Collectors.joining(","));
    }

    @Override
    public void afterExec() {

    }
}
