package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 1. 校验目标表分区字段名称是否已在源表schema中存在；
 * 2. 校验目标表分区字段格式(包含jinjia表达式)是否合法；
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.HIVE_PARTITION_FIELD)
public class HivePartitionFieldValidator implements Validator {

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Override
    public void validateImpl(Task task, TaskContext context) throws Exception {
        String templateCode = task.getTemplateCode();
        JSONObject runtimeConfig = JSONObject.parseObject(task.getRuntimeConfig());
        switch (templateCode) {
            case "DataMigration":
                if (runtimeConfig.getBoolean("isTable")) {
                    validatePartition(runtimeConfig.getString("partitions"));

                }
                break;
            case "Mysql2Hive":
            case "Hive2Sharestore":
                validatePartition(runtimeConfig.getString("partitions"));
//                JSONArray columns = (JSONArray) JSON.parse(runtimeConfig.getString("columns"));
//            validateColumn(columns, partitions[0]);
                break;
            case "Hive2Mysql":
            case "Hive2Doris":
                JSONArray partitions = (JSONArray) JSON.parse(runtimeConfig.getString("partitions"));
                String name = ((JSONObject) partitions.get(0)).getString("name").trim();
                String original = ((JSONObject) partitions.get(0)).getString("value").trim();
                if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(original)) {
                    validateJinja(original);
                } else if ((StringUtils.isEmpty(name) && StringUtils.isNotEmpty(original)) ||
                        (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(original))) {
                    throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.PARTITION_NO_EMPTY.getMessage());
                }
                break;
        }
    }

    private void validatePartition(String partitions) {
        if (StringUtils.isNotEmpty(partitions)) {  // 分区字段为空时不校验
            String[] temp = partitions.replaceAll(" ", "").split("=\\{\\{", 2);  // 过滤空格是为防止分区字段后面有空格
            if (temp.length != 2) {
                throw new ServiceException(BaseResponseCodeEnum.PARTITION_NEED_EQUAL_SIGN.name(), BaseResponseCodeEnum.PARTITION_NEED_EQUAL_SIGN.getMessage());
            }
            String original = partitions.split("=", 2)[1].trim();
            validateJinja(original);
        }
    }

    private void validateJinja(String original) {
        String partition = schedulerService.parseJinja(original);
        if (original.length() > 1 && partition.equals("")) {
            throw new ServiceException(BaseResponseCodeEnum.JINJA_ERROR.name(), BaseResponseCodeEnum.JINJA_ERROR.getMessage());
        }
    }

    private void validateColumn(JSONArray columns, String partitions) {
        for (Integer i = 0; i < columns.size(); i++) {
            String name = columns.getJSONObject(i).getString("name");
            if (name.equals(partitions)) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.PARTITION_NO_FOUND_SOURCE.getMessage());
            }
        }
    }

}
