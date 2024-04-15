package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.third.lakecat.LakeCatService;
import com.ushareit.dstask.utils.GsonUtil;
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
@ValidFor(type = ValidType.HIVE_WRITE_PRI)
public class HiveWritePriValidator implements Validator {

    @Resource
    private LakeCatService lakeCatService;

    @Autowired
    private TaskService taskService;
    
    @Override
    public void validateImpl(Task task, TaskContext context) {
        String templateCode = task.getTemplateCode();
        JSONObject runtimeConfig = (JSONObject) JSON.parse(task.getRuntimeConfig());
        String user = runtimeConfig.getString("owner");

        if (templateCode.equals("DataMigration")) {
            if (runtimeConfig.getBoolean("isTable")) {
                String targetDB = runtimeConfig.getString("targetDb");
                String targetTable = runtimeConfig.getString("targetTable");
                String targetRegion = runtimeConfig.getString("targetRegion");

                lakeCatService.checkTablePri(user, targetRegion, targetDB, targetTable, CommonConstant.INSERT_TABLE);
            }
        } else if (templateCode.equals("Mysql2Hive")) {
            boolean existTargetTable = true;
            if (runtimeConfig.containsKey("existTargetTable")) {
                existTargetTable = runtimeConfig.getBoolean("existTargetTable");
            }
            String targetDB = runtimeConfig.getString("targetDb");
            String targetTable = runtimeConfig.getString("targetTable");
            String targetRegion = runtimeConfig.getString("sourceRegion");
            if (existTargetTable) {
                lakeCatService.checkTablePri(user, targetRegion, targetDB, targetTable, CommonConstant.INSERT_TABLE);
            } else {  // hive表不存在，需创建
                lakeCatService.checkTablePri(user, targetRegion, targetDB, targetTable, CommonConstant.CREATE_TABLE);
            }
        } else if (templateCode.equals("Hive2Hive")) {
            String sql = task.getContent();
            String region = runtimeConfig.getString("sourceRegion");
            Map<String, Object> result = taskService.getEtlSqlTbl(sql, region);
            log.info("hivemap-->{}", GsonUtil.toJson(result,false));
            ArrayList<HashMap<String, String>> outputTables = (ArrayList<HashMap<String, String>>) result.get("outputTbls");
            for (HashMap<String, String> dbTable : outputTables) {
                lakeCatService.checkTablePri(user, region, dbTable.get("db"), dbTable.get("tbl"), CommonConstant.INSERT_TABLE);
            }
        }
    }
}
