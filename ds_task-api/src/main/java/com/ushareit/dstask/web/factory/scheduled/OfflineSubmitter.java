package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.Submitter;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.ScheduledJobParam;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2021/12/10
 */
@Slf4j
public class OfflineSubmitter implements Submitter {
    private ScheduledJob scheduledJob;

    private TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;
    public OfflineSubmitter(Job job, TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub) {
        this.scheduledJob = (ScheduledJob) job;
        this.taskSchedulerRpcApiBlockingStub = taskSchedulerRpcApiBlockingStub;
    }

    @Override
    public BaseResponse submitAsync() {

        ScheduleJobOuterClass.TaskCode taskCode = scheduledJob.toPbTaskCode();
        ScheduleJobOuterClass.ScheduleJob scheduleJob = ScheduleJobOuterClass.ScheduleJob.newBuilder()
                .setTaskId(scheduledJob.task.getId())
                .setTaskName(scheduledJob.task.getName())
                .setTaskCode(taskCode)
                .build();

        log.info("task schedulejob is  :" + scheduleJob);
        System.out.println(scheduleJob);

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse;

        if (scheduledJob.task.getRealtimeExecute()) {
             taskCommonResponse = taskSchedulerRpcApiBlockingStub.updateAndExec(
                    TaskSchedulerApi.UpdateAndExecRequest
                            .newBuilder()
                            .setId(scheduledJob.task.getId())
                            .setCallbackUrl(scheduledJob.task.getCallbackUrl())
                            .setArgs(scheduledJob.task.getMainClassArgs())
                            .setScheduleJob(scheduleJob)
                            .build()
            );
        }else{
             taskCommonResponse = taskSchedulerRpcApiBlockingStub.updateTask(
                    TaskSchedulerApi.UpdateParamRequest
                            .newBuilder()
                            .setScheduleJob(scheduleJob)
                            .build()
            );
        }

        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR,
                    String.format("%s: code[%d], message[%s]", BaseResponseCodeEnum.SCHEDULER_START_ERR.getMessage(), code, msg));
        }
        return new BaseResponse().getInstance(taskCommonResponse.getCode(),taskCommonResponse.getMessage(),taskCommonResponse.getData());
    }

    @Override
    public Map<String, String> update() {
        ScheduledJobParam scheduledJobParam = new ScheduledJobParam(scheduledJob, scheduledJob.buildTaskItems());
        PropertyPreFilters.MySimplePropertyPreFilter filterField = scheduledJobParam.getFilterField();
        String jobParamJson = JSON.toJSONString(scheduledJobParam, filterField);
        log.info("update scheduled job param: " + jobParamJson);
        Map<String, String> params = new HashMap<>();
        params.put("task_code", jobParamJson);
        return params;
    }

    @Override
    public void processException() {

    }

    @Override
    public String getFlinkUiDns(String appName, String region) {
        return null;
    }

    @Override
    public void deleteResource() throws Exception {

    }

    @Override
    public void autoScaleTm(Integer count) {

    }

    @Override
    public Integer getTmNum() {
        return null;
    }
}
