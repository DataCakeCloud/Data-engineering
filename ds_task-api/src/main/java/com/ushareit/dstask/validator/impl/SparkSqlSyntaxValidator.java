package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.dstask.web.utils.EncryptUtil;
import com.ushareit.engine.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 校验sql语法的正确性，需加缓存
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.SPARK_SQL_SYNTAX, enabled = false)
public class SparkSqlSyntaxValidator implements Validator {

    @Resource
    public TaskService taskService;

    @Override
    public void validateImpl(Task task, TaskContext context) throws Exception {
        //对sql进行md5
        if (StringUtils.isNotEmpty(task.getContent())) {
            String secret = DigestUtils.md5DigestAsHex(task.getContent().getBytes());
            task.setMd5Sql(EncryptUtil.encrypt(secret, DsTaskConstant.METADATA_PASSWDKEY));
        }

        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(runtimeConfigJson);
        String region = runtimeConfig.getSourceRegion();
        String owner = runtimeConfig.getAdvancedParameters().getOwner();
        if (StringUtils.isEmpty(task.getMd5Sql()) || StringUtils.isEmpty(region) ||
                StringUtils.isEmpty(owner)) {
            return;
        }
        taskService.cacheCheck(task, task.getId(), task.getMd5Sql(), region, owner);
    }


}
