package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.engine.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 1. 一个回车要与一个分号对应。
 * 2. jinja 表达式合法
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.PYTHON_SHELL)
public class PythonShellValidator implements Validator {

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        if (task.getTemplateCode().equals("PythonShell")) {
            RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            String cmd = runtimeConfig.getAdvancedParameters().getCmds();
            String param = schedulerService.hasCycleParam(cmd);
            if(schedulerService.isIrregularSheduler(task) && !param.isEmpty()){
                throw new ServiceException(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.name(), String.format(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.getMessage(),param));
            }
            cmd = cmd.replaceAll("s3://", "").replaceAll("obs://", "").replaceAll("gs//","");
            cmd = schedulerService.parseJinja(cmd);
            if (cmd.contains("{{") || cmd.contains("}}") || cmd.contains("//")) {
                throw new ServiceException(BaseResponseCodeEnum.JINJA_ERROR.name(), BaseResponseCodeEnum.JINJA_ERROR.getMessage());
            }
        }
    }
}
