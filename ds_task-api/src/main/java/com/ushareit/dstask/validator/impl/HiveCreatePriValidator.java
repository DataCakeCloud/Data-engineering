package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.third.lakecat.LakeCatService;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.HIVE_CREATE_PRI)
public class HiveCreatePriValidator implements Validator {

    @Resource
    private LakeCatService lakeCatService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        String templateCode = task.getTemplateCode();
        JSONObject runtimeConfig = (JSONObject) JSON.parse(task.getRuntimeConfig());
        if (templateCode.equals("DataMigration")) {
            if (runtimeConfig.getBoolean("isTable")) {
                String user = runtimeConfig.getString("owner");
                String targetDB = runtimeConfig.getString("targetDb");
                String targetTable = runtimeConfig.getString("targetTable");
                String targetRegion = runtimeConfig.getString("targetRegion");

                lakeCatService.checkTablePri(user, targetRegion, targetDB, targetTable, CommonConstant.CREATE_TABLE);
            }
        } else if (templateCode.equals("Mysql2Hive")) {
            String user = runtimeConfig.getString("owner");
            String targetDB = runtimeConfig.getString("targetDb");
            String targetTable = runtimeConfig.getString("targetTable");
            String targetRegion = runtimeConfig.getString("sourceRegion");
            lakeCatService.checkTablePri(user, targetRegion, targetDB, targetTable, CommonConstant.CREATE_TABLE);
        }
    }
}
