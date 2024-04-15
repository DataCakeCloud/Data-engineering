package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Column;
import com.ushareit.dstask.web.factory.scheduled.param.DBEtlConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.utils.OlapGateWayUtil;
import com.ushareit.dstask.web.utils.SparkSqlParseUtil;
import com.ushareit.engine.seatunnel.util.SeaTunnelParser;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
public class DbETLJob extends ScheduledJob{
    private final String MYSQL2HIVE = "Mysql2Hive";
    private final String HIVE2MYSQL = "Hive2Mysql";
    private final String HIVE2CLICKHOUSE = "Hive2Clickhouse";
    private final String HIVE2DORIS = "Hive2Doris";
    List<Column> distinctCols;
    private boolean existTargetTable;

    private DBEtlConfig dbEtlConfig = new DBEtlConfig();

    public DbETLJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        if (context.getExecuteMode().equals("local")){
            this.dynamicsCmd = true;
            taskItem.put("command_tags", "type:local-submit");
            taskItem.put("workdir", "/work");
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
    protected String buildCommand() throws ServiceException {
        try {
            if (context.getExecuteMode().equals("local")){
                return "/work/"+dataxExecuteShell;
            }else{
                return SparkSqlParseUtil.appendConf2SubimitStr(executeShell, sparkConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(BaseResponseCodeEnum.GET_SEATUNNEL_JOB_SHELL_FAIL);
        }
    }

    @Override
    public void beforeExec() {
    }

    public String getCreateTableSql(){
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
        List<Column> cloneColumns = (List<Column>)columns.clone();

        // 生成查询sql语句
        String targetTable = dbEtlConfig.getTargetTable();
        String sql;
        if(addColumns!=null){
            cloneColumns.addAll(addColumns);
        }
        String schema = getTableSchema(cloneColumns);

        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        String collect = cloudResource.getList().stream()
                .map(data -> data.getRegionAlias()).collect(Collectors.joining("|"));

        targetTable = targetTable.replaceFirst(collect,"iceberg");
        if(partitions.isEmpty() || partitions == null){
            sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) USING iceberg LOCATION '%s'",targetTable,schema,dbEtlConfig.getLocation());
        }else{
            sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) USING iceberg PARTITIONED BY (%s) LOCATION '%s'",targetTable,schema,partition[0],dbEtlConfig.getLocation());
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
            return String.format("`%s` %s COMMENT '%s'", column.getColumnName(), columnType, column.getComment());
        }).collect(Collectors.joining(","));
        return schema;
    }

    @Override
    public void afterExec() {

    }

    void parseTargetTable(String id){
        // hive格式: analyst.ods_mi_push_info_likeit_lite_mi_push_info_likeit_lite_all_daily@sg2
        // 1.db 2.tbl 3.region
        String hiveIdPattern = "([^.]*)\\.([^.]*)@([^.]*)";
        // mysql, ck格式: clickhouse.ue1.ch_aws_test_35135.ds_test.dws_beyla_device_active_agg_inc_daily
        // 2.region 3.source 4.db 5.tbl
        String myAndCkIdPattern = "([^.]*)\\.([^.]*)\\.([^.]*)\\.([^.]*)\\.([^.]*)";

        Pattern hiveComplile = Pattern.compile(hiveIdPattern);
        Matcher hiveMatcher = hiveComplile.matcher(id);
        if (hiveMatcher.find()) {
            dbEtlConfig.setTargetTable(hiveMatcher.group(1) + "." + hiveMatcher.group(2));
            return;
        }

        Pattern myAndCkComplile = Pattern.compile(myAndCkIdPattern);
        Matcher myAndCkMatcher = myAndCkComplile.matcher(id);
        if (myAndCkMatcher.find()) {
            //ck  do_prod_large_ch_aws集群需要在表面后加上_local
            if (myAndCkMatcher.group(1).equals("clickhouse") && myAndCkMatcher.group(3).equals("do_prod_large_ch_aws")) {
                dbEtlConfig.setTargetTable(myAndCkMatcher.group(4) + "." + myAndCkMatcher.group(5) + "_local");
            } else {
                dbEtlConfig.setTargetTable(myAndCkMatcher.group(4) + "." + myAndCkMatcher.group(5));
            }
        }

    }

}
