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
import lombok.extern.slf4j.Slf4j;

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
    private String acrossCloud;
    List<Column> distinctCols;
    private boolean existTargetTable;

    private DBEtlConfig dbEtlConfig = new DBEtlConfig();

    public DbETLJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        acrossCloud = runtimeConfigObject.getOrDefault("acrossCloud","common").toString().trim();
        existTargetTable = runtimeConfigObject.getBoolean("existTargetTable")!= null ? runtimeConfigObject.getBoolean("existTargetTable") : true;
        dbEtlConfig.setConnectionUrl(runtimeConfigObject.getString("connectionUrl"));
        dbEtlConfig.setDbUser(runtimeConfigObject.getString("dbUser"));
        dbEtlConfig.setDbPassword(runtimeConfigObject.getString("dbPassword"));
        String srcQualifyName = runtimeConfigObject.getOrDefault("sourceDb", "") + "." + runtimeConfigObject.getOrDefault("sourceTable", "");
        if (!srcQualifyName.equals("\\.")) {
            dbEtlConfig.setSourceTable(srcQualifyName);
        }
        // 解析生成targetTable(兼容历史数据，原来是从output_dataset获取table)
        String id = outputDatasets.get(0).getId();
        dbEtlConfig.setTargetTable(runtimeConfigObject.getString("targetDB") + "." + runtimeConfigObject.getString("targetTable"));
        String cluster = runtimeConfigObject.getString("cluster");
        dbEtlConfig.setCluster(cluster);
//        parseTargetTable(outputDatasets.get(0).getId());
        String partitions;
        if("Hive2Clickhouse".equals(templateCode) || "Hive2Mysql".equals(templateCode)){
            try {
                JSONArray defaut = JSON.parseObject("[{\"name\":\"datepart\",\"value\":\"{{ yesterday_ds }}\"}]", JSONArray.class);
                JSONArray partition = (JSONArray)runtimeConfigObject.getOrDefault("partitions",defaut);
                partitions = partition.stream().map(data -> {
                    JSONObject json = JSON.parseObject(String.valueOf(data));
                    if("Hive2Clickhouse".equals(templateCode)){
                        return String.format("%s=%s",json.getString("name"),json.getString("value"));
                    }else{
                        return String.format("%s='%s'",json.getString("name"),json.getString("value"));
                    }
                }).collect(Collectors.joining(","));
            }catch (Exception e){
                log.info("分区数据格式异常，采用旧逻辑兼容");
                partitions = (String) runtimeConfigObject.getOrDefault("partitions", "datepart='{{ yesterday_ds }}'");
            }
        }else{
            partitions = (String) runtimeConfigObject.getOrDefault("partitions", "datepart='{{ yesterday_ds }}'");
        }

        dbEtlConfig.setPartitions(partitions);
        // JSONArray schema = runtimeConfigObject.getJSONArray("schema");
        String schema = runtimeConfigObject.getString("columns");
        List<Column> columns = JSON.parseArray(schema, Column.class);
        // 存在重复传入的cols, 这里做一次去重
        distinctCols = Column.distinctColumns(columns);
//         this.columns = schema.stream().map(column -> (String) ((JSONObject) column).get("column_name")).collect(Collectors.joining(","));
        dbEtlConfig.setColumns(distinctCols);

        String sqlStr = task.getContent();
        if (sqlStr != null && !sqlStr.isEmpty()) {
            dbEtlConfig.setSql(encodeFlag(sqlStr));
        }

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
        taskItem.put("command_tags",getEngineCommand(false));
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
        return taskItems;
    }

    @Override
    protected String buildCommand() throws ServiceException {
        String dbType;
        switch (templateCode) {
            case HIVE2MYSQL:
                dbType = "mysql";
                break;
            case HIVE2CLICKHOUSE:
                dbType = "clickhouse";
                break;
            default:
                throw new ServiceException("-1", "Unsupport templateCode: " + templateCode);
        }

        dbEtlConfig.setDbType(dbType);
        String json = JSON.toJSONString(dbEtlConfig);
        String s = json.replaceAll("\"", "\\\\\"");//.replaceAll("/转译双引号/", "\\\\\"");
        s = s.replaceAll("},","}, "); // 兼容genie
        return sparkConfig + "\"" + s + "\"";
    }

    @Override
    public void beforeExec() {
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
