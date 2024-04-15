package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author fengxiao
 * @date 2023/2/10
 */
@Slf4j
@Component
@ValidFor(type = ValidType.PARTITION_FIELD_NOT_IN_TABLE_FIELD)
public class MySQLPartitionFieldValidator implements Validator {
    
    @Override
    public void validateImpl(Task task, TaskContext context) {
        if (task.getTemplateCode().equals("Hive2Mysql")) {
            JSONObject runtimeConfig = (JSONObject) JSON.parse(task.getRuntimeConfig());
            JSONArray partitions = (JSONArray) JSON.parse(runtimeConfig.getString("partitions"));
            String partitionName = ((JSONObject) partitions.get(0)).getString("name");

            JSONArray sourceColumns = (JSONArray) JSON.parse(runtimeConfig.getString("sourceColumns"));
            for (Object sourceColumn : sourceColumns) {
                String name = ((JSONObject) sourceColumn).getString("name");
                if (name.equals(partitionName)) {
                    throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.PARTITION_FIELD_NOT_IN_TABLE_FIELD.getMessage());
                }
            }
        }
    }
}
