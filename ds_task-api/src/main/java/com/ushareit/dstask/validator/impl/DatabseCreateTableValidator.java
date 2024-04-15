package com.ushareit.dstask.validator.impl;


import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.service.impl.LakeServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import io.lakecat.catalog.common.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ValidFor(type = ValidType.DATABASE_CREATE_TABLE)
public class DatabseCreateTableValidator implements Validator {
    @Autowired
    private LakeServiceImpl lakeService;


    @Override
    public void validateImpl(Task task, TaskContext context) throws Exception {
        com.ushareit.engine.param.RuntimeConfig newConfig = JSON.parseObject(task.getRuntimeConfig(), com.ushareit.engine.param.RuntimeConfig.class);
        String targetDb = newConfig.getCatalog().getTargetDb();

        boolean allowForDb = lakeService.allowForDb(newConfig.getSourceRegion(), targetDb, Operation.CREATE_TABLE, InfTraceContextHolder.get().getUuid());
        if(!allowForDb){
            throw new ServiceException(BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.getMessage() + String.format(":%s", targetDb));
        }

    }
}
