package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 1、前缀
 * 2、jinja 表达式
 * 3、路径重复
 *
 * @see com.ushareit.dstask.validator.checker.SuccessFileChecker
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
//@Component
@Deprecated
@ValidFor(type = ValidType.SUCCESS_FILE)
public class SuccessFileValidator implements Validator {
    @Resource
    public TaskMapper taskMapper;

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        if (StringUtils.isEmpty(task.getOutputDataset())) {
            return;
        }
        List<Dataset> datasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
        if (datasets == null || datasets.size() == 0){
            return;
        }
        for (Dataset dataset : datasets){
            String path = dataset.getLocation();
            String fileName = dataset.getFileName();
            if (StringUtils.isEmpty(path) || StringUtils.isEmpty(fileName)){
                continue;
            }

            //前缀校验
            if (!validatePath(path, DsTaskConstant.OBS_AWS_PATH_PATTERN)) {
                throw new ServiceException(BaseResponseCodeEnum.FILE_FORMAT_ERROR, BaseResponseCodeEnum.FILE_FORMAT_ERROR.getMessage() + path);
            }
            String param = schedulerService.hasCycleParam(path);
            if(schedulerService.isIrregularSheduler(task) && !param.isEmpty()){
                throw new ServiceException(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.name(), String.format(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.getMessage(),param));
            }

            //jinjia表达式校验 调用田谞接口
            String cmd = path.replaceAll("s3://", "").replaceAll("obs://", "").replaceAll("gs//","");;
            cmd = schedulerService.parseJinja(cmd);
            if (cmd.contains("{{") || cmd.contains("}}") || cmd.contains("//")) {
                throw new ServiceException(BaseResponseCodeEnum.JINJA_ERROR.name(), BaseResponseCodeEnum.JINJA_ERROR.getMessage());
            }
            //路径重复校验
            List<Task> list = taskMapper.getTaskBySuccessPath(context.getTaskFromDB().getId(), fileName, path);
            if (list != null && list.size() > 0) {
                throw new ServiceException(BaseResponseCodeEnum.TASK_OUTPUT_ERROR, BaseResponseCodeEnum.TASK_OUTPUT_ERROR.getMessage() + list.get(0).getName());
            }
        }
    }
    private Boolean validatePath(String string, String pattern) {
        return match(string, pattern, null);
    }
    private Boolean match(String string, String pattern, Integer flags) {
        Pattern r = Pattern.compile(pattern);
        if (flags != null) {
            r = Pattern.compile(pattern, flags);
        }
        Matcher m = r.matcher(string);
        return m.matches();
    }
}
