package com.ushareit.dstask.web.factory.flink.submitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskInstance;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.FlinkExecModeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.Submitter;
import com.ushareit.dstask.web.factory.flink.job.FlinkBaseJob;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.FlinkApiUtil;
import com.ushareit.dstask.web.utils.RetryUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobStatus;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author wuyan
 * @date 2021/12/10
 */
@Slf4j
public class BaseSubmitter implements Submitter {

    protected TaskServiceImpl taskServiceImp;

    protected Job job;

    protected FlinkBaseJob baseJob;

    public BaseSubmitter() {}

    public BaseSubmitter(TaskServiceImpl taskServiceImp, Job job) {
        this.taskServiceImp = taskServiceImp;
        this.job = job;
        if (job != null) {
            this.baseJob = (FlinkBaseJob) job;
        }
    }

    @Override
    public BaseResponse submitAsync() throws Exception {
        saveTaskInstance();
//        this.submit();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        RetryUtil.asyncExecuteWithRetry(new Callable<BaseResponse>() {
            @Override
            public BaseResponse call() throws Exception {
                submit();
                InfTraceContextHolder.get().setTenantName(tenantName);
                job.afterExec();
                checkSubmitResult();
                return null;
            }
        }, DsTaskConstant.MAX_JOB_CREATION_ATTEMPTS, 2000L, true);
        return BaseResponse.success(BaseResponseCodeEnum.APP_RELEASE_SUCCESS);
    }

    @Override
    public Map<String, String> update() {
        return null;
    }

    private void saveTaskInstance() {
        //一个部署最多只能有0个job正在RUNNINNG
        taskServiceImp.taskInstanceService.checkJobIsAlive(baseJob.runTimeTaskBase.getId());
        //8、生成taskInstance
        baseJob.taskInstance = (TaskInstance)new TaskInstance()
                .setTaskId(baseJob.task.getId())
                .setVersionId(baseJob.tagId)
                .setStatusCode(JobStatus.CREATED.name())
                .setClusterId(baseJob.cluster.getId())
                .setSnapshotId(baseJob.savepointId)
                .setServiceAddress(baseJob.webUi)
                .setCreateBy(InfTraceContextHolder.get().getUserName());
        taskServiceImp.taskInstanceService.save(baseJob.taskInstance);
    }

    public void submit() {
    }

    @Override
    public void processException() {}

    protected void checkSubmitResult() {
        try {
//            deleteOnlineUdfJarObsUrl();

            String flinkJmAddress = getFlinkUiDns(baseJob.task.getName().toLowerCase(), baseJob.cluster.getAddress());
            //getFlinkJobId
            log.info("JM地址:" + flinkJmAddress);
            BaseResponse response = RetryUtil.executeWithRetry(new Callable<BaseResponse>() {
                @Override
                public BaseResponse call() throws Exception {
                    return getFlinkJobId(flinkJmAddress);
                }
            }, 60, 3000L, false);
        } catch (Exception e) {
            log.error("checkSubmitResult 获取flink job url失败！失败原因:" + CommonUtil.printStackTraceToString(e));
            processException();
        }
    }


    @Override
    public String getFlinkUiDns(String appName, String region) {
        switch (FlinkExecModeEnum.valueOf(baseJob.cluster.getTypeCode())) {
            case YARN:
                return baseJob.getWebUi();
            case K8S:
                return baseJob.getWebUi();
            default:
                return MessageFormat.format("http://{0}-rest.{1}.flink.ushareit.org", appName, region);
        }
    }

    @Override
    public void deleteResource() {

    }

    @Override
    public void autoScaleTm(Integer count) {
        log.info("not support autoscale taskmanager!");
    }

    @Override
    public Integer getTmNum() {
        return 0;
    }

    private BaseResponse getFlinkJobId(String flinkJmAddress) throws Exception{
        log.info("发送获取job的请求，请求url：" + FlinkApiUtil.getJobs(flinkJmAddress));
        BaseResponse response = HttpUtil.get(FlinkApiUtil.getJobs(flinkJmAddress));
        log.info("发送获取job的请求，请求结果：" + response);
        if (!BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "等待服务可用中");
        }

        JSONArray jobs = response.get().getJSONArray("jobs");

        if (jobs == null || jobs.size() == 0) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "等待服务中的flink集群返回job id");
        } else {
            String flinkJobId = JSON.parseObject(jobs.get(0).toString()).getString("jid");
            baseJob.taskInstance.setServiceAddress(flinkJmAddress)
                    .setEngineInstanceId(flinkJobId)
                    .setStatusCode(JobStatus.RUNNING.name());
            taskServiceImp.taskInstanceService.update(baseJob.taskInstance);
        }

        return response;
    }


    protected void updateInstanceUI(String submitUid) {
        if (baseJob.taskInstance == null) {
            return;
        }
        baseJob.taskInstance.setSubmitUid(submitUid)
                .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                .setUpdateBy("system");
        taskServiceImp.taskInstanceService.update(baseJob.taskInstance);
    }

    protected void updateTaskAndInstance() {
        if (baseJob.taskInstance == null) {
            return;
        }
        baseJob.taskInstance.setStatusCode(JobStatus.FAILED.name())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                .setUpdateBy("system");
        taskServiceImp.taskInstanceService.update(baseJob.taskInstance);
        taskServiceImp.changeStateCode(baseJob.task, JobStatus.FAILED.name());
//        taskServiceImp.alarmNoticeUtil.notice(baseJob.task, String.format("实时任务报警通知：\n" +
//                " %s任务运行失败。", baseJob.task.getName()));
    }

    private void deleteOnlineUdfJarObsUrl() throws Exception {
        // 只删除udf和主jar
        if (baseJob.onlineUdfJarObsUrl != null) {
            CloudBaseClientUtil cloudClientUtil = taskServiceImp.cloudFactory.getCloudClientUtilByUrl(baseJob.onlineUdfJarObsUrl);
            cloudClientUtil.delete(baseJob.onlineUdfJarObsUrl);
        }
    }
}
