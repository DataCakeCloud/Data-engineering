package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.third.lakecat.LakeCatService;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.HIVE_READ_PRI)
public class HiveReadPriValidator implements Validator {

    @Resource
    private LakeCatService lakeCatService;

    @Autowired
    private TaskService taskService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        JSONObject runtimeConfig = (JSONObject) JSON.parse(task.getRuntimeConfig());
        String user = runtimeConfig.getString("owner");

        String templateCode = task.getTemplateCode();
        if (templateCode.equals("DataMigration")) {
            if (runtimeConfig.getBoolean("isTable")) {
                String sourceDB = runtimeConfig.getString("sourceDb");
                String sourceTable = runtimeConfig.getString("sourceTable");
                String sourceRegion = runtimeConfig.getString("sourceRegion");

                lakeCatService.checkTablePri(user, sourceRegion, sourceDB, sourceTable, CommonConstant.SELECT_TABLE);
            }
        } else if (templateCode.equals("Hive2Mysql") || templateCode.equals("Hive2Doris") || templateCode.equals("Hive2Clickhouse") || templateCode.equals("Hive2Redis")) {
            JSONArray inputDataset = (JSONArray) JSON.parse(task.getInputDataset());
            JSONObject metadata = inputDataset.getJSONObject(0).getJSONObject("metadata");
            String db = metadata.getString("db");
            String table = metadata.getString("table");
            String region = metadata.getString("region");

            lakeCatService.checkTablePri(user, region, db, table, CommonConstant.SELECT_TABLE);
        } else if (templateCode.equals("Hive2Sharestore")) {
            String sourceDB = runtimeConfig.getString("sourceDb");
            String sourceTable = runtimeConfig.getString("sourceTable");
            String sourceRegion = runtimeConfig.getString("sourceRegion");

            lakeCatService.checkTablePri(user, sourceRegion, sourceDB, sourceTable, CommonConstant.SELECT_TABLE);
        } else if (templateCode.equals("Hive2Hive")) {
            String sql = task.getContent();
            String region = runtimeConfig.getString("sourceRegion");
            Map<String, Object> result = taskService.getEtlSqlTbl(sql, region);
            ArrayList<HashMap<String, String>> inputTables = (ArrayList<HashMap<String, String>>) result.get("inputTbls");
            for (HashMap<String, String> dbTable : inputTables) {
                lakeCatService.checkTablePri(user, region, dbTable.get("db"), dbTable.get("tbl"), CommonConstant.SELECT_TABLE);
            }
        }
    }
    
}
