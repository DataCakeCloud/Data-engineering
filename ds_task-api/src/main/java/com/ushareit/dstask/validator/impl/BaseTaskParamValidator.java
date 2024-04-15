package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.BaseEntity;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SchedulerCycleEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.BASE_PARAM_CHECK)
public class BaseTaskParamValidator implements Validator {

    @GrpcClient("pipeline-server")
    private static TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;
    @Autowired
    private TaskMapper taskMapper;

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        // 1.校验名称
        preCheckName(task);

        // 2.Name不重复校验
        checkOnUpdate(taskMapper.selectByName(task.getName()), task, String.format("任务名字【%s】已经存在，请更换", task.getName()));

        // 3. main class 校验
        if (StringUtils.isNotEmpty(task.getMainClass()) && !match(task.getMainClass(), DsTaskConstant.APPLICATION_MAIN_CLASS) && !match(task.getMainClass(), DsTaskConstant.TEMPLATE_PARAM_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.MAIN_CLASS_NOT_MATCH);
        }

        // 4.离线任务校验
        preCheckOfflineTask(task);
    }

    private void preCheckName(Task task) {
        if (isStreamingTemplate(task)) {
            if (!match(task.getName(), DsTaskConstant.APPLICATION_NAME_PATTERN)) {
                throw new ServiceException(BaseResponseCodeEnum.NAME_NOT_MATCH);
            }
        } else {
            if (!match(task.getName(), DsTaskConstant.SCHEDULED_APPLICATION_NAME_PATTERN)) {
                throw new ServiceException(BaseResponseCodeEnum.OFFLINE_TASK_NAME_NOT_MATCH);
            }
        }
    }

    private Boolean isStreamingTemplate(Task task) {
        Boolean isStreamingTemplateCode = false;
        boolean stream = TemplateEnum.valueOf(task.getTemplateCode()).isStreamingTemplate();
        try {
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            if (stream) {
                Boolean isBatchTask = runtimeConfig.getAdvancedParameters().getIsBatchTask();
                isStreamingTemplateCode = isBatchTask == null || !isBatchTask;
            } else if (TemplateEnum.valueOf(task.getTemplateCode()) == TemplateEnum.Mysql2Hive) {
                Integer syncType = runtimeConfig.getAdvancedParameters().getSyncType();
                if (syncType == null) {
                    syncType = runtimeConfig.getCatalog().getSync_mode();
                }
                if (syncType == 2) isStreamingTemplateCode = true;
            }
        } catch (Exception e) {
            log.error(BaseResponseCodeEnum.SYS_ERR.name() + "runtimeConfig解析失败", e);
        }
        return isStreamingTemplateCode;
    }

    private void preCheckOfflineTask(Task task) {
        // 1.校验任务不能依赖删除的任务
        checkTaskEventDepends(task);

        // 2.离线任务修改名称，必须下线状态，且调用airflow接口
        checkOfflineName(task);

        // 3.校验前置依赖的周期
        TriggerParam dependTaskTriggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
        if (dependTaskTriggerParam.getType().equals(DsTaskConstant.CRON_TRIGGER)) {
            // cron任务不需要校验
            return;
        }

        if(schedulerService.isIrregularSheduler(task)){
            String sql = task.getContent();
            if(sql!=null && !sql.isEmpty()){
                String decodeSql = URLDecoder.decode(new String(Base64.getDecoder().decode(sql.getBytes())));
                String param = schedulerService.hasCycleParam(decodeSql);
                if(!param.isEmpty()){
                    throw new ServiceException(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.name(), String.format(BaseResponseCodeEnum.IRREGULAR_SCHEDULER_PARAM_ERROR.getMessage(),param));
                }
            }
        }


        String outputGranularity = dependTaskTriggerParam.getOutputGranularity();
        SchedulerCycleEnum outputCycle = SchedulerCycleEnum.valueOf(outputGranularity.toUpperCase());

        if (task.getEventDepends() != null) {
            List<EventDepend> eventDepends = JSON.parseArray(task.getEventDepends(), EventDepend.class);
            for (EventDepend eventDepend : eventDepends) {
                SchedulerCycleEnum dependCycle = SchedulerCycleEnum.valueOf(eventDepend.getGranularity().toUpperCase());
                if (outputCycle.compare(dependCycle)) {
                    return;
                }
            }
        }

        if (task.getInputDataset() != null) {
            List<Dataset> inputDatasets = JSON.parseArray(task.getInputDataset(), Dataset.class);
            for (Dataset inputDataset : inputDatasets) {
                if (inputDataset.getGranularity() != null && !inputDataset.getGranularity().isEmpty()) {
                    SchedulerCycleEnum dependCycle = SchedulerCycleEnum.valueOf(inputDataset.getGranularity().toUpperCase());
                    if (outputCycle.compare(dependCycle)) {
                        return;
                    }
                }
            }
        }
    }

    private void checkTaskEventDepends(Task task) {
        if (task.getEventDepends() != null) {
            List<EventDepend> eventDepends = JSON.parseArray(task.getEventDepends(), EventDepend.class);
            ArrayList<String> ids = new ArrayList<>();
            for (EventDepend eventDe : eventDepends) {
                if (eventDe.getIsDelete()) {
                    ids.add(eventDe.getTaskId());
                }
            }
            if (ids.size() != 0) {
                throw new ServiceException(BaseResponseCodeEnum.TASK_IS_DELETED, String.join(",", ids));
            }
        }
    }

    private void checkOfflineName(Task task) {
        if (task.getId() == null || task.getIsStreamingTemplateCode()) {
            return;
        }

        Task taskFromDB = taskMapper.selectByPrimaryKey(task.getId());
        if (taskFromDB == null || StringUtils.equalsIgnoreCase(task.getName(), taskFromDB.getName())) {
            return;
        }

        if (task.getOnline() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.ONLINE_JOB_CANNOT_UPDATE_NAME);
        } else {
            TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.taskUpdatename(
                    TaskSchedulerApi.UpdateTaskNameRequest
                            .newBuilder()
                            .setNewName(task.getName())
                            .setOldName(taskFromDB.getName())
                            .build());

            if (taskCommonResponse.getCode() != 0) {
                throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
            }
        }
    }

    /**
     * @param recordFromDb  from Db
     * @param recordFromWeb from Web
     * @param errorMsg      custom error message
     */
    private <T extends BaseEntity> void checkOnUpdate(T recordFromDb, T recordFromWeb, String errorMsg) {
        if (recordFromDb != null && recordFromWeb != null) {
            if (!recordFromDb.getId().equals(recordFromWeb.getId())) {
                throw new ServiceException(BaseResponseCodeEnum.NAME_IS_NOT_UNIQUE.name(), errorMsg);
            }
        }
    }

    /**
     * 名字匹配正则表达式
     *
     * @param string  要匹配的字符串
     * @param pattern 正则表达式模板
     * @return
     */
    public Boolean match(String string, String pattern) {
        return match(string, pattern, null);
    }

    public Boolean match(String string, String pattern, Integer flags) {
        Pattern r = Pattern.compile(pattern);
        if (flags != null) {
            r = Pattern.compile(pattern, flags);
        }
        Matcher m = r.matcher(string);
        return m.matches();
    }
}
