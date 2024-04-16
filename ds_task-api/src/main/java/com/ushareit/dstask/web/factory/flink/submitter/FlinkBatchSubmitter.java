package com.ushareit.dstask.web.factory.flink.submitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/1/18
 */
@Slf4j
public class FlinkBatchSubmitter extends K8sNativeSubmitter {
    public FlinkBatchSubmitter(TaskServiceImpl taskServiceImp, Job job) {
        super(taskServiceImp, job);
    }

    @GrpcClient("pipeline-server")
    private static TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;

    @Override
    public BaseResponse submitAsync() throws Exception {
        execBatch();
        return BaseResponse.success();
    }


    protected void execBatch() {
        String command = this.getCommand();
        log.info("execBatch command:" + command);
        Map<String, Object> taskCode = new HashMap(6);
        List<Map<String, String>> list = new ArrayList<>(1);
        Map<String, String> items = new HashMap(3);
        items.put("flink_conn_id", baseJob.cluster.getAddress());
        items.put("task_type", "FlinkBatchOperator");
        items.put("command", command);
        list.add(items);
        taskCode.put("name", baseJob.runTimeTaskBase.getName());
        taskCode.put("input_datasets", JSON.parseArray(baseJob.runTimeTaskBase.getInputDataset(), Dataset.class));
        taskCode.put("output_datasets", JSON.parseArray(baseJob.runTimeTaskBase.getOutputDataset(), Dataset.class));
        taskCode.put("task_items", list);
        taskCode.put("owner", baseJob.runtimeConfig.getOwner());
        taskCode.put("emails", baseJob.runtimeConfig.getEmails());
        taskCode.put("email_on_retry", false);
        taskCode.put("email_on_failure", false);
        taskCode.put("email_on_success", false);
        taskCode.put("event_depends", baseJob.eventDepends);
        taskCode.put("trigger_param", baseJob.triggerParam);
        taskCode.put("depend_types", baseJob.dependTypes);

        List<Integer> alertTypes = baseJob.runtimeConfig.getAlertType();
        alertTypes.stream().forEach(alertType -> {
            switch (alertType) {
                case 1:
                    taskCode.put("email_on_success", true);
                    break;
                case 2:
                    taskCode.put("email_on_failure", true);
                    break;
                case 3:
                    taskCode.put("email_on_retry", true);
                    break;
            }
        });

        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(taskCode);
        ScheduleJobOuterClass.TaskCode taskCodeParam = jsonObject.toJavaObject(ScheduleJobOuterClass.TaskCode.class);

        ScheduleJobOuterClass.ScheduleJob scheduleJob = ScheduleJobOuterClass.ScheduleJob.newBuilder()
                .setTaskId(baseJob.runTimeTaskBase.getId())
                .setTaskName(baseJob.runTimeTaskBase.getName())
                .setTaskCode(taskCodeParam)
                .build();

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.updateTask(
                TaskSchedulerApi.UpdateParamRequest
                        .newBuilder()
                        .setScheduleJob(scheduleJob)
                        .build()
        );

        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            job.afterExec();
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }

        job.afterExec();

    }
}
