package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Column;
import com.ushareit.dstask.web.factory.scheduled.param.DBEtlConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.OlapGateWayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * lakehouse2db job
 * date:2023-06-06
 * author:xuebotao
 */
@Slf4j
public class LakeHouse2DBJob extends ScheduledJob {

    private final String Hive2Doris = "Hive2Doris";

    private String acrossCloud;
    List<Column> distinctCols;
    private boolean existTargetTable;

    private DBEtlConfig dbEtlConfig = new DBEtlConfig();

    public LakeHouse2DBJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        if(StringUtils.isNotEmpty(runtimeConfigObject.getString("executeMode"))){
            return;
        }
        acrossCloud = runtimeConfigObject.getOrDefault("acrossCloud", "common").toString().trim();
        existTargetTable = runtimeConfigObject.getBoolean("existTargetTable") != null ? runtimeConfigObject.getBoolean("existTargetTable") : true;
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

        JSONArray defaut = JSON.parseObject("[{\"name\":\"datepart\",\"value\":\"{{ yesterday_ds }}\"}]", JSONArray.class);
        JSONArray partition = (JSONArray) runtimeConfigObject.getOrDefault("partitions", defaut);
        String partitions = partition.stream().map(data -> {
            JSONObject json = JSON.parseObject(String.valueOf(data));
            return String.format("%s=%s", json.getString("name"), json.getString("value"));
//            if ("Hive2Clickhouse".equals(templateCode)) {
//                return String.format("%s=%s", json.getString("name"), json.getString("value"));
//            } else {
//                return String.format("%s='%s'", json.getString("name"), json.getString("value"));
//            }
        }).collect(Collectors.joining(","));

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
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        String clusterTags;
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:spark-submit-ds");
        taskItem.put("task_type", "KyuubiOperator");
        taskItem.put("command", buildCommand());
        return taskItems;
    }

    @Override
    protected String buildCommand() throws ServiceException {
        String dbType;
        switch (templateCode) {
            case Hive2Doris:
                dbType = "doris";
                break;
            default:
                throw new ServiceException("-1", "Unsupport templateCode: " + templateCode);
        }

        dbEtlConfig.setDbType(dbType);
        String json = JSON.toJSONString(dbEtlConfig);
        String s = json.replaceAll("\"", "\\\\\"");//.replaceAll("/转译双引号/", "\\\\\"");
        return sparkConfig + "\"" + s + "\"";
    }

    @Override
    public void beforeExec() {

    }


    @Override
    public void afterExec() {

    }


}
