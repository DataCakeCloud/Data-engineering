package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskInstanceMapper;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.utils.ReleaseResourceUtil;
import com.ushareit.dstask.web.autoscale.AutoScaleExecutor;
import com.ushareit.dstask.web.factory.flink.submitter.AutoScaleSubmitter;
import com.ushareit.dstask.web.utils.*;
import com.ushareit.dstask.web.vo.BaseResponse;
import com.ushareit.engine.param.RuntimeConfig;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.flink.api.common.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class TaskInstanceServiceImpl extends AbstractBaseServiceImpl<TaskInstance> implements TaskInstanceService {
    private static final Set<Integer> JOB_EXIST_SET = new HashSet<>();
    @GrpcClient("pipeline-server")
    private static TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;

    @Resource
    public TaskService taskService;

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Autowired
    private TaskSnapshotService taskSnapshotService;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    public FlinkClusterService flinkClusterService;
    @Resource
    public TaskScaleStrategyService taskScaleStrategyService;
    @Resource
    public TaskParChangeService taskParChangeService;
    @Resource
    public TaskVersionService taskVersionService;
    @Resource
    private TaskInstanceMapper taskInstanceMapper;
    @Resource
    private TaskInstanceService taskInstanceService;
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private ScmpUtil scmpUtil;

    @Resource
    private AlarmNoticeUtil alarmNoticeUtil;

    @Value("${server-url.host}")
    private String dsHost;

    private Long count = 0L;

    @Override
    public CrudMapper<TaskInstance> getBaseMapper() {
        return taskInstanceMapper;
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    public void ScheduledMonitorNonTerminalJob() {
        taskInstanceService.monitorNonTerminalJob();
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 120000)
    public void ScheduledMonitorAutoScaleJob() {
        taskInstanceService.monitorAutoScaleJob();
    }

    /**
     * 异步+定时监控flink平台中的job的状态改变
     */
    @MultiTenant
    @DisLock(key = "monitorNonTerminalJob", expiredSeconds = 4, isRelease = false)
    public void monitorNonTerminalJob() {
        if (count++ % DsTaskConstant.LOG_PERCENT == 0) {
            log.info("定时监控所有flink集群中非终止状态的job");
        }
        //查找所有部署非最终状态的job
        List<String> nonTerminalJobStatus = Arrays.stream(JobStatus.values())
                .filter(x -> !x.isTerminalState())
                .map(JobStatus::name)
                .collect(Collectors.toList());
        nonTerminalJobStatus.add(DsTaskConstant.JOB_STATUS_INITIALIZING);

        List<TaskInstance> jobs = taskInstanceMapper.queryByAppIdAndStatus(null, nonTerminalJobStatus);
        if (jobs == null || jobs.size() == 0) {
            return;
        }

        jobs.stream()
                .filter(job -> job.getEngineInstanceId() != null)
                .forEach(this::monitorPerJob);
    }

    private void monitorPerJob(TaskInstance job) {
        Task task = taskService.getById(job.getTaskId());
        // 任务已删除不监控
        if (task == null) {
            log.error("task id:" + job.getTaskId() + " job id:" + job.getId() + " 任务已删除，不应该有运行中实例。");
            return;
        }

        Iterable<Tag> tag = null;
        String jobUrl = FlinkApiUtil.getJob(job.getServiceAddress(), job.getEngineInstanceId());
        try {
            // 添加任务监控标签
            metricsInit(task, tag = Collections.singletonList(Tag.of("name", task.getName())));

            // 连续监控10 * 10S
            BaseResponse response = RetryUtil.executeWithRetry(new Callable<BaseResponse>() {
                @Override
                public BaseResponse call() throws Exception {
                    return getJobState(jobUrl);
                }
            }, 10, 10000L, false);
            String state = response.get().getString("state");
            /**
             * 状态处理
             * 1、状态不变或FAILING return
             * 2、job->task同步状态
             * 3、自动扩缩任务FAILED + 对应实例FAILED -> 关闭资源
             * 4、如Task状态已同步为GloballyTerminalState，可能是其他监控同步或者statehook
             *   job状态改为task状态
             * 5、异常置failed
             */
            // 1、状态不变或FAILING return
            if (state.equalsIgnoreCase(task.getStatusCode()) || JobStatus.FAILING.name().equalsIgnoreCase(state)) {
                if (JobStatus.FAILING.name().equals(state)) {
                    log.info("task id:" + task.getId() + " Job状态访status：" + state);
                }
                return;
            }

            // 2、state->job->task同步状态
            setTaskAndJobState(task, job, state);

            // 3、自动扩缩任务GloballyTerminalState -> 尝试关闭资源
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            Boolean isAutoScaleMode = runtimeConfig.getAdvancedParameters().getIsAutoScaleMode();
            if (isAutoScaleMode
                    && !state.equalsIgnoreCase(DsTaskConstant.JOB_STATUS_INITIALIZING)
                    && JobStatus.valueOf(state).isGloballyTerminalState()) {
                autoScaleModeDeleteResource(task, job);
            }
        } catch (Exception e) {
            // 4、如Task状态已同步为GloballyTerminalState，可能是其他监控同步或者statehook
            String state = taskInstanceService.getById(job.getId()).getState();
            if (Arrays.stream(JobStatus.values()).filter(JobStatus::isTerminalState)
                    .anyMatch(item -> StringUtils.equalsIgnoreCase(item.name(), state))) {
                return;
            }

            // 5、异常置failed
            log.error("task id:" + job.getTaskId() + " Job状态访问失败：" + jobUrl + ",失败原因:" + CommonUtil.printStackTraceToString(e));

            // 6、更新状态成功，上报指标
            if (setTaskAndJobState(task, job, JobStatus.FAILED.name())) {
                metricsReport(task, tag);
                log.error(" dsHost is :" + DataCakeConfigUtil.getDataCakeConfig().getServerHost() + " env is :" + InfTraceContextHolder.get().getEnv());
                alarmNoticeUtil.notice(task, String.format("DataCake实时任务报警通知：\n" +
                        " %s任务运行失败。\n %s", task.getName(), DataCakeConfigUtil.getDataCakeConfig().getServerHost() + "task/info?id=" + task.getId()));
            }
        }
    }



    private boolean setTaskAndJobState(Task task, TaskInstance job, String state) {
        try {
            Example example = new Example(TaskInstance.class);
            example.or()
                    .andEqualTo("id", job.getId())
                    .andEqualTo("statusCode", job.getStatusCode());
            job.setStatusCode(state).setUpdateBy("monitor").setUpdateTime(new Timestamp(System.currentTimeMillis()));
            if (taskInstanceMapper.updateByExampleSelective(job, example) == NumberUtils.INTEGER_ZERO) {
                log.info("task instance {} origin status is {}, update to {} failed", job.getId(), job.getStatusCode(), state);
                return false;
            }

            Example taskExample = new Example(Task.class);
            taskExample.or()
                    .andEqualTo("id", task.getId())
                    .andEqualTo("statusCode", task.getStatusCode());
            task.setStatusCode(state)
                    .setUpdateBy("monitor").setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return taskMapper.updateByExampleSelective(task, taskExample) > NumberUtils.INTEGER_ZERO;
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return false;
        }
    }

    private void autoScaleModeDeleteResource(Task task, TaskInstance job) {
        log.info("process autoscale task resource! task id is " + task.getId());
        Integer clusterId = job.getClusterId();
        FlinkCluster cluster = flinkClusterService.getById(clusterId);
        AutoScaleSubmitter submitter = new AutoScaleSubmitter(cluster.getNameSpace(), cluster.getAddress(), "jobmanager-" + task.getName().toLowerCase());
        submitter.deleteResource();
        ReleaseResourceUtil.close(submitter.getClient());
    }

    private void metricsInit(Task task, Iterable<Tag> tag) {
        if (!JOB_EXIST_SET.contains(task.getId())) {
            JOB_EXIST_SET.add(task.getId());
            Metrics.counter("ds.application.failed", tag).increment(0.0);
            log.info("添加默认指标，应用id是" + task.getId());
        }
    }

    private void metricsReport(Task task, Iterable<Tag> tag) {
        try {
            log.info("应用id：" + task.getId() + "挂了，job状态:" + task.getStatusCode());
            Metrics.counter("ds.application.failed", tag).increment();
            log.info("prometheus metric ds.application.failed：" + task.getName());
        } catch (Exception e) {
            log.error("prometheus metric pool error ", e);
        }
    }

    private BaseResponse getJobState(String jobUrl) {
        BaseResponse response = HttpUtil.get(jobUrl);
        if (!BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, String.format("等待服务可用中,失败原因:%s", response.getMessage()));
        }
        return response;
    }

    /**
     * 取消flink中的作业，并实时更新job表中job的状态 部署表中部署的当前状态
     *
     * @param task
     */
    @Override
    public void cancelJob(Task task, TaskInstance job) {
        log.info("正在取消flink集群中的作业，任务id是:" + task.getId() + ", 任务名称是:" + task.getName());
        if (job == null) {
            job = getAliveJobs(task.getId());
        }
        if (job.getEngineInstanceId() != null) {
            String cancelUrl = FlinkApiUtil.getCancel(job.getServiceAddress(), job.getEngineInstanceId());
            BaseResponse cancelResponse = HttpUtil.get(cancelUrl);
            if (cancelResponse == null || !BaseResponseCodeEnum.SUCCESS.name().equals(cancelResponse.getCodeStr())) {
                throw new ServiceException(BaseResponseCodeEnum.JOB_CANCEL_FAIL, String.format("cancelUrl=%s,失败原因:%s", cancelUrl, cancelResponse.getData()));
            }
        }

        job.setStatusCode(JobStatus.CANCELED.name())
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        log.info("任务id:" + task.getId() + ",任务名称:" + task.getName() + ",cancelJob:往数据库更新时间:" + new Timestamp(System.currentTimeMillis()));
        super.update(job);
    }

    @Override
    public String stopEtlJob(String name, String status, String executionDate, String flinkUrl) {
        Task task = new Task();
        task.setName(name);
        task.setDeleteStatus(0);
        Task taskRes = taskService.selectOne(task);
        if (taskRes != null) {
            String message = String.format("对%s的实例进行停止", executionDate);
            AuditlogUtil.auditlog(DsTaskConstant.TASK, taskRes.getId(), BaseActionCodeEnum.STOP, message);
        }
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse;
        if ("1".equals(status)) {
            taskCommonResponse = RetryBlockingStub.executeWithRetry(() ->taskSchedulerRpcApiBlockingStub.setSuccess(
                    TaskSchedulerApi.StateSetRequest
                            .newBuilder()
                            .setName(name)
                            .setExecutionDate(executionDate)
                            .build()
            ));
        } else {
            taskCommonResponse = taskSchedulerRpcApiBlockingStub.setFail(
                    TaskSchedulerApi.StateSetRequest
                            .newBuilder()
                            .setName(name)
                            .setExecutionDate(executionDate)
                            .build()
            );
        }

        if (taskCommonResponse == null) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob: %s", name, "pipeline service exception"));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_STOP_FAIL, "离线任务停止失败: pipeline service exception");
        }

        if (taskCommonResponse.getCode() == 2) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob[code: %d]: %s", name, taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_STOP_FAIL, "离线任务停止失败，程序内部有Exception:" + taskCommonResponse.getMessage());
        }

        if (taskCommonResponse.getCode() == 1) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob[code: %d]: %s", name, taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_STOP_FAIL, "离线任务停止失败，业务逻辑方面的判断有错误:" + taskCommonResponse.getMessage());
        }
        deleteDeployments(flinkUrl);

        return taskCommonResponse.getData();
    }

    private void deleteDeployments(String flinkUrl) {
        if (StringUtils.isEmpty(flinkUrl)) {
            return;
        }

        String[] flinkUrlArray = flinkUrl.split(",");
        List<String> list = Arrays.asList(flinkUrlArray);
        for (String url : list) {
            deleteDeployment(url);
        }
    }

    private void deleteDeployment(String flinkUrl) {
        String[] array = flinkUrl.split("-rest");
        checkArrayLength(array, flinkUrl);


        String[] arr1 = array[0].split("//");
        checkArrayLength(arr1, flinkUrl);

        String[] arr2 = array[1].split("\\.");
        checkArrayLength(arr2, flinkUrl);

        String taskInstanceName = arr1[1];
        String context = arr2[1];
        String namespace = getNamespace(context);
        scmpUtil.deleteK8sDeployment(context, namespace, taskInstanceName);
    }


    /**
     * 停止并触发保存点
     *
     * @param taskId
     */
    @Override
    public void stopWithSavepoint(Integer taskId) {
        TaskInstance taskInstance = getAliveJobs(taskId);

        taskSnapshotService.stopWithSavepoint(taskInstance);
        taskInstance.setStatusCode(JobStatus.SUSPENDED.name())
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        log.info("任务id:" + taskId + ",stopWithSavepoint:往数据库更新时间:" + new Timestamp(System.currentTimeMillis()));
        super.update(taskInstance);
    }


    /**
     * 获取部署对应的活跃的所有作业
     *
     * @param appId
     * @return
     */
    @Override
    public TaskInstance getAliveJobs(Integer appId) {
        List<String> statusList = Arrays.stream(JobStatus.values())
                .filter(x -> !x.isTerminalState())
                .map(JobStatus::name)
                .collect(Collectors.toList());
        List<TaskInstance> jobs = taskInstanceMapper.queryByAppIdAndStatus(appId, statusList);
        if (jobs == null || jobs.size() == 0 || StringUtils.isEmpty(jobs.get(0).getEngineInstanceId())) {
            throw new ServiceException(BaseResponseCodeEnum.NO_STARTED_JOB);
        }
        return jobs.get(0);
    }


    /**
     * 根据部署id获取最新的job
     *
     * @param appId
     * @return
     */
    @Override
    public TaskInstance getLatestJobByTaskId(Integer appId) {
        List<TaskInstance> jobs = taskInstanceMapper.queryByAppIdAndStatus(appId, null);
        if (jobs == null || jobs.size() == 0) {
            return null;
        }

        for (TaskInstance job : jobs) {
            String engineInstanceId = job.getEngineInstanceId();
            if (StringUtils.isNotEmpty(engineInstanceId)) {
                return job;
            }
        }

        return null;
    }

    @Override
    public TaskInstance getOnlyLatestJobByTaskId(Integer appId) {
        List<TaskInstance> jobs = taskInstanceMapper.queryByAppIdAndStatus(appId, null);
        if (jobs != null && jobs.size() > 0) {
            return jobs.get(0);
        }
        return null;
    }

    /**
     * 重跑
     *
     * @param taskName
     */
    @Override
    public void clear(String taskName, String[] executionDate, Boolean isCheckUpstream) {
        Task task = new Task();
        task.setName(taskName);
        task.setDeleteStatus(0);
        Task taskRes = taskService.selectOne(task);
        if (taskRes != null) {
            String message = String.format("对%s的实例进行重跑", StringUtils.join(executionDate, ","));
            AuditlogUtil.auditlog(DsTaskConstant.TASK, taskRes.getId(), BaseActionCodeEnum.CLEAR, message);
        }
        String executionDates = StringUtils.join(executionDate, ",");
        boolean CheckUpstream = isCheckUpstream == null ? false : isCheckUpstream;
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = RetryBlockingStub.executeWithRetry(() ->taskSchedulerRpcApiBlockingStub.clear(
                TaskSchedulerApi.ClearTaskRequest
                        .newBuilder()
                        .setTaskName(taskName)
                        .setExecutionDate(executionDates)
                        .setIsCheckUpstream(CheckUpstream)
                        .build()
        ));
        if (taskCommonResponse == null) {
            log.error(String.format("[%s]failed to pipeline clear task: %s", taskName, "pipeline service exception"));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, "pipeline service exception");
        }
        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("[%s]failed to pipeline clear task[code: %d]: %s", taskName, taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }
    }

    @Override
    public void clear(List<ClearStopParam> clearStopParamList) {
        Map<String, List<ClearStopParam>> parmMaps = clearStopParamList.stream().collect(Collectors.groupingBy(ClearStopParam::getName));
        List<String> listNames = clearStopParamList.stream().map(ClearStopParam::getName).collect(Collectors.toList());
        if (listNames.isEmpty()) {
            return;
        }

        Example example = new Example(Task.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        criteria.andIn("name", listNames);
        List<Task> tasksList = taskService.listByExample(example);

        if (tasksList.isEmpty()) {
            return;
        }

        for (Task ta : tasksList) {
            List<ClearStopParam> clearStopParams = parmMaps.get(ta.getName());
            String mesg = clearStopParams.stream().map(ClearStopParam::getExecutionDate).collect(Collectors.joining(","));
            String message = String.format("对%s的实例进行重跑", mesg);
            AuditlogUtil.auditlog(DsTaskConstant.TASK, ta.getId(), BaseActionCodeEnum.CLEAR, message);
        }

        TaskSchedulerApi.BatchHandleRequest.Builder builder = TaskSchedulerApi.BatchHandleRequest.newBuilder();
        List<ClearStopParam> collect = clearStopParamList.stream().map(data -> {
            TaskSchedulerApi.Handle build = TaskSchedulerApi.Handle
                    .newBuilder()
                    .setName(data.getName())
                    .setExecutionDate(data.getExecutionDate())
                    .setIsCheckUpstream(data.getIsCheckUpstream() != null && data.getIsCheckUpstream())
                    .build();
            builder.addBatches(build);
            return data;
        }).collect(Collectors.toList());
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = RetryBlockingStub.executeWithRetry(() -> taskSchedulerRpcApiBlockingStub.batchClear(
                builder.build()));

        if (taskCommonResponse == null) {
            log.error(String.format("[%s]failed to pipeline batch clear task: %s", String.join(",", listNames), "pipeline service exception"));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, "pipeline service exception");
        }
        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("[%s]failed to pipeline batch clear task[code: %d]: %s", String.join(",", listNames), taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }
    }

    @Override
    public String stopEtlJob(List<ClearStopParam> clearStopParamList) {
        Map<String, List<ClearStopParam>> parmMaps = clearStopParamList.stream().collect(Collectors.groupingBy(ClearStopParam::getName));
        List<String> listNames = clearStopParamList.stream().map(ClearStopParam::getName).collect(Collectors.toList());
        if (listNames.isEmpty()) {
            return null;
        }
        Example example = new Example(Task.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        criteria.andIn("name", listNames);
        List<Task> tasksList = taskService.listByExample(example);

        if (tasksList.isEmpty()) {
            return null;
        }
        for (Task ta : tasksList) {
            List<ClearStopParam> clearStopParams = parmMaps.get(ta.getName());
            String mesg = clearStopParams.stream().map(ClearStopParam::getExecutionDate).collect(Collectors.joining(","));
            String message = String.format("对%s的实例进行停止", mesg);
            AuditlogUtil.auditlog(DsTaskConstant.TASK, ta.getId(), BaseActionCodeEnum.STOP, message);
        }
        Map<String, List<ClearStopParam>> statusMaps = clearStopParamList.stream().collect(Collectors.groupingBy(ClearStopParam::getStatus));

        //1 是成功  失败是0
        boolean status = statusMaps.containsKey("1");
        TaskSchedulerApi.BatchHandleRequest.Builder builder = TaskSchedulerApi.BatchHandleRequest.newBuilder();

        List<ClearStopParam> collect = clearStopParamList.stream().map(data -> {
            TaskSchedulerApi.Handle build = TaskSchedulerApi.Handle
                    .newBuilder()
                    .setName(data.getName())
                    .setExecutionDate(data.getExecutionDate())
                    .setIsCheckUpstream(data.getIsCheckUpstream() != null && data.getIsCheckUpstream())
                    .build();
            builder.addBatches(build);
            return data;
        }).collect(Collectors.toList());

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse;
        if (status) {
            taskCommonResponse = RetryBlockingStub.executeWithRetry(() -> taskSchedulerRpcApiBlockingStub.batchSetSuccess(
                    builder.build()));
        } else {
            taskCommonResponse = taskSchedulerRpcApiBlockingStub.batchSetFailed(
                    builder.build());
        }

        if (taskCommonResponse == null) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob: %s", String.join(",", listNames), "pipeline service exception"));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_BATCH_STOP_FAIL, "离线任务批量停止失败: pipeline service exception");
        }
        if (taskCommonResponse.getCode() == 2) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob[code: %d]: %s", String.join(",", listNames), taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_BATCH_STOP_FAIL, "离线任务批量停止失败，程序内部有Exception:" + taskCommonResponse.getMessage());
        }
        if (taskCommonResponse.getCode() == 1) {
            log.error(String.format("[%s]failed to pipeline stopEtlJob[code: %d]: %s", String.join(",", listNames), taskCommonResponse.getCode(), taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.TASK_INSTANCE_BATCH_STOP_FAIL, "离线任务批量停止失败，业务逻辑方面的判断有错误:" + taskCommonResponse.getMessage());
        }
        return taskCommonResponse.getData();
    }


    /**
     * 获取工件运行实例列表 /page
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param paramMap 查询參數
     * @return
     */
    @Override
    public Map<String, Object> page(int pageNum, int pageSize, Map<String, String> paramMap) {
        Map<String, Object> result = new HashMap<>(4);
        Task task = null;
        Integer taskId = CommonUtil.getIdFromMap(paramMap, "taskId");
        // id不存在，name存在
        if (taskId == null) {
            // 获取离线任务
            task = taskService.getByName(paramMap.get("name"));
            result.put("taskId", task.getId().toString());
            result.put("workflowId", task.getWorkflowId());
            result.put("templateCode", task.getTemplateCode());
            result.put("isOwnerOrCollaborator", isOwnerOrCollaborator(task));
            result.put("online", online(task));
            result.put("isScanSqlCmd", TemplateEnum.isOfflineSqlTemplate(task.getTemplateCode()));  // 预览sql或预览执行命令
            result.put("isScanKibana", TemplateEnum.isScanKibana(task.getTemplateCode()));  // 打开kibana日志
            result.put("isScanSparkUI", TemplateEnum.isScanSparkUI(task.getTemplateCode()));  // 打开spark ui(genie)日志
            result.put("result", getOfflineTaskInstances(task, pageNum, pageSize, paramMap));
            return result;
        }

        // id存在，name存在? -> name不存在
        task = taskService.checkExist(taskId);
        String name = paramMap.computeIfPresent("name", (k, v) -> v);
        if (!StringUtils.isEmpty(name)) {
            paramMap.remove("name");
        }
        //TODO 实时任务：数据库分页查询；离线任务：从pipeline查询运行历史
        if (taskService.isStreaming(task)) {
            paramMap.put("taskId", taskId.toString());
            PageHelper.startPage(pageNum, pageSize);
            List<TaskInstance> pageRecord = getBaseMapper().listByMap(paramMap);
            padJobInfos(pageRecord);
            result.put("isScanSqlCmd", false);
            result.put("isScanKibana", false);
            result.put("isScanSparkUI", false);
            result.put("result", new PageInfo<>(pageRecord));
            result.put("workflowId", task.getWorkflowId());
            return result;
        }
        result.put("templateCode", task.getTemplateCode());
        result.put("taskId", taskId.toString());
        result.put("workflowId", task.getWorkflowId());
        result.put("isOwnerOrCollaborator", isOwnerOrCollaborator(task));
        result.put("online", online(task));
        result.put("isScanSqlCmd", TemplateEnum.isOfflineSqlTemplate(task.getTemplateCode()));
        result.put("isScanKibana", TemplateEnum.isScanKibana(task.getTemplateCode()));
        result.put("isScanSparkUI", TemplateEnum.isScanSparkUI(task.getTemplateCode()));
        result.put("result", getOfflineTaskInstances(task, pageNum, pageSize, paramMap));
        return result;

    }

    private Boolean isSparkTask(String templateCode) {
        if (!"PythonShell".equals(templateCode) && !"Hive2Redshift".equals(templateCode)) {
            return true;
        }
        return false;
    }


    private Boolean online(Task task) {
        Integer online = task.getOnline();
        return online == 1 ? true : false;
    }

    private Boolean isOwnerOrCollaborator(Task task) {
        String userGroup = InfTraceContextHolder.get().getUuid();
        if (StringUtils.isNotEmpty(task.getUserGroup()) && userGroup.equals(task.getUserGroup())) {
            return true;
        }
        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        String owner = runtimeConfig.getAdvancedParameters().getOwner();
        String collaborators = task.getCollaborators();
        String current = InfTraceContextHolder.get().getUserName();

        if (owner.equals(current)) {
            return true;
        }
        String[] collaboratorsArray = collaborators.split(",");
        List<String> collaboratorsList = Arrays.asList(collaboratorsArray);
        for (String collaborator : collaboratorsList) {
            if (collaborator.equals(current)) {
                return true;
            }
        }

        String dsGroups = runtimeConfig.getAdvancedParameters().getDsGroups();
        if (StringUtils.isEmpty(dsGroups) || "[]".equalsIgnoreCase(dsGroups)) {
            return false;
        }

        List<Integer> groupIds = JSON.parseArray(dsGroups, Integer.class);
        List<AccessUser> accessUsers = accessUserService.selectByGroupIds(groupIds);
        boolean contains = accessUsers.stream().map(AccessUser::getName).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())).contains(current);
        if (contains) {
            return true;
        }
        return false;
    }

    private PageInfo<TaskInstance> getOfflineTaskInstances(Task task, int pageNum, int pageSize, Map<String, String> paramMap) {
        paramMap.put("name", task.getName());
        paramMap.put("page", String.valueOf(pageNum));
        paramMap.put("size", String.valueOf(pageSize));
        Map<String,String> headers = new HashMap<>();
        headers.put(CommonConstant.CURRENT_LOGIN_USER,JSONObject.toJSONString(InfTraceContextHolder.get().getUserInfo()));

        BaseResponse response = HttpUtil.get(DataCakeConfigUtil.getDataCakeServiceConfig().getPipelineHost() + "/pipeline/dagruns/latest", paramMap, headers);
        if (response.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", response.getData()));
        }
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            String msg = response.getMessage();
            log.error(String.format("[%d]运维页获取任务实例信息失败，code: %d, message: %s", task.getId(),code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("获取任务状态失败，code: %d, message: %s", code, msg));
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            Integer count = data.getInteger("count");
            List<TaskInstance> taskInfos = JSON.parseArray(data.getString("task_info"), TaskInstance.class);
            if (TemplateEnum.valueOf(task.getTemplateCode()).isStreamingTemplate()) {
                taskInfos.stream().forEach(taskInstance -> taskInstance.setGenieJobId("00000000000000000000000000000000").setGenieJobUrl(taskInstance.getFlinkUrl()));
            }

            Page<TaskInstance> instancePage = new Page<>(pageNum, pageSize);
            instancePage.setTotal(count);
            instancePage.addAll(taskInfos);
            return new PageInfo<>(instancePage);
        }
    }

    private void padJobInfos(List<TaskInstance> list) {
        list.stream().forEach(job -> {
            FlinkCluster cluster = flinkClusterService.getById(job.getClusterId());
            if (cluster != null) {
                job.setClusterName(cluster.getName());
            }
            Integer snapshotId = job.getSnapshotId();
            if (snapshotId != null && snapshotId > 0) {
                TaskSnapshot savepoint = taskSnapshotService.getById(snapshotId);
                if (savepoint != null) {
                    job.setSnapshotName(savepoint.getName());
                }
            }
        });
    }

    @Override
    public void checkJobIsAlive(Integer taskId) {
        List<String> statusList = Arrays.stream(JobStatus.values())
                .filter(x -> !x.isTerminalState())
                .map(JobStatus::name)
                .collect(Collectors.toList());
        List<TaskInstance> jobs = taskInstanceMapper.queryByAppIdAndStatus(taskId, statusList);
        //检查数据库job表中job 状态
        if (jobs != null && jobs.size() > 0) {
            throw new ServiceException(BaseResponseCodeEnum.APP_HAS_ALIVE_JOB, "数据库中存在活跃的作业，请先终止应用");
        }
    }

    @Override
    public TaskInstanceDiagnosis diagnose(String taskName, String executionDate, String state) {
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.getTaskDiagnose(
                TaskSchedulerApi.DiagnoseParamRequest
                        .newBuilder()
                        .setName(taskName)
                        .setExecutionDate(executionDate)
                        .setState(state)
                        .build()
        );
        if (taskCommonResponse.getCode() != 0) {
            log.error(String.format("[%s]failed to pipeline task diagnose[code: %d]: %s", taskName, taskCommonResponse.getCode(), taskCommonResponse.getMessage()));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
        }

        JSONObject diagnoseData = JSON.parseObject(taskCommonResponse.getData());
        TaskInstanceDiagnosis taskInstanceDiagnosis = new TaskInstanceDiagnosis();
        taskInstanceDiagnosis.setInstance(new ArrayList<TaskInstanceNode>());
        taskInstanceDiagnosis.setRelation(new ArrayList<TaskInstanceRelation>());

        taskInstanceDiagnosis.setCoreTaskId(diagnoseData.getString("coreTaskId"));
        JSONArray instanceNodes = diagnoseData.getJSONArray("instance");
        loadTaskInstanceNodeData(taskInstanceDiagnosis.getInstance(), instanceNodes);

        JSONArray temp_relation = diagnoseData.getJSONArray("relation");
        loadTaskInstanceNodeRelation(taskInstanceDiagnosis.getRelation(), temp_relation);

        return taskInstanceDiagnosis;
    }

    private void loadTaskInstanceNodeRelation(List<TaskInstanceRelation> nodeList, JSONArray nodeJsonArr) {
        nodeJsonArr.forEach(
                node -> {
                    JSONObject nodeJsOb = (JSONObject) node;
                    TaskInstanceRelation tiNode = new TaskInstanceRelation();
                    tiNode.setSource(nodeJsOb.getString("source"));
                    tiNode.setTarget(nodeJsOb.getString("target"));
                    tiNode.setType(nodeJsOb.getBoolean("type"));
                    nodeList.add(tiNode);
                }
        );
    }

    private void loadTaskInstanceNodeData(List<TaskInstanceNode> nodeList, JSONArray nodeJsonArr) {
        nodeJsonArr.forEach(
                node -> {
                    JSONObject nodeJsOb = (JSONObject) node;
                    TaskInstanceNode tiNode = new TaskInstanceNode();
                    tiNode.setDagId(nodeJsOb.getString("dagId"));
                    tiNode.setDownDagId(nodeJsOb.getString("downDagId"));
                    tiNode.setCheckPath(nodeJsOb.getString("checkPath"));

                    tiNode.setMetadataId(nodeJsOb.getString("metadataId"));
                    tiNode.setExecutionDate(nodeJsOb.getString("executionDate"));
                    tiNode.setReady(nodeJsOb.getBoolean("ready"));
                    tiNode.setNodeId(nodeJsOb.getString("nodeId"));
                    if (nodeJsOb.containsKey("isExternal")) {
                        tiNode.setExternal(nodeJsOb.getBoolean("isExternal"));
                    }
                    if (nodeJsOb.containsKey("owner")) {
                        tiNode.setOwner(nodeJsOb.getString("owner"));
                    }
                    if (nodeJsOb.containsKey("successDate")) {
                        tiNode.setSuccessDate(nodeJsOb.getString("successDate"));
                    }
                    if (nodeJsOb.containsKey("recursion")) {
                        tiNode.setRecursion(nodeJsOb.getBoolean("recursion"));
                    }
                    if (nodeJsOb.containsKey("start_date")) {
                        tiNode.setStart_date(nodeJsOb.getString("start_date"));
                    }
                    if (nodeJsOb.containsKey("end_date")) {
                        tiNode.setEnd_date(nodeJsOb.getString("end_date"));
                    }
                    if (nodeJsOb.containsKey("state")) {
                        tiNode.setState(nodeJsOb.getString("state"));
                    }
                    Task task = new Task();
                    task.setName(nodeJsOb.getString("dagId"));
                    task.setDeleteStatus(0);
                    Task taskRes = taskService.selectOne(task);
                    Integer taskId = 0;
                    if (taskRes != null) {
                        taskId = taskRes.getId();
                    }
                    tiNode.setTaskId(taskId);
                    tiNode.setOnline(nodeJsOb.getBoolean("online"));
                    nodeList.add(tiNode);
                }
        );
    }


    /**
     * 异监控flink平台中属于自动伸缩的任务
     */
    @MultiTenant
    @DisLock(key = "monitorAutoScaleJob", expiredSeconds = 110, isRelease = false)
    public void monitorAutoScaleJob() {
        if (count++ % DsTaskConstant.LOG_PERCENT == 0) {
            log.info("定时监控所有flink集群中running的job并且开启了自动伸缩的任务");
        }
        //查找所有部署running job并且是开启自动伸缩
        List<String> runningList = new ArrayList<>();
        runningList.add(JobStatus.RUNNING.name());
        List<TaskInstance> taskInstances = taskInstanceMapper.queryByAppIdAndStatus(null, runningList);
        List<Integer> idList = new ArrayList<>();
        taskInstances.stream().filter(job -> job.getEngineInstanceId() != null).forEach(data -> idList.add(data.getTaskId()));
        if (idList.isEmpty()) {
            return;
        }
        List<Task> tasks = taskService.queryByIds(idList);
        tasks.stream().filter(task -> {
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            return runtimeConfig.getAdvancedParameters().getIsAutoScaleMode() && task.getDeleteStatus() == 0;
        }).forEach(data -> {
            AutoScaleExecutor executor = new AutoScaleExecutor();
            executor.checkRunningJobPar(this, data);
        });
    }


    @Override
    public BaseResponse deleteDeploy(String name, String context) {
        String namespace = getNamespace(context);
        scmpUtil.deleteK8sDeployment(context, namespace, name);
        return BaseResponse.success();
    }

    private String getNamespace(String context) {
        if (StringUtils.isEmpty(context)) {
            log.error("can not get namespace!");
            throw new ServiceException(BaseResponseCodeEnum.CLUSTER_ADDRESS_NOT_MATCH);
        }

        if (context.equalsIgnoreCase("shareit-cce-test")) {
            return "cbs-flink";
        }

        return context.startsWith("cbs-") ? "cbs-flink" : "bdp-flink";
    }

    private void checkArrayLength(String[] array, String flinkUrl) {
        if (array.length <= 1) {
            log.error("flink url " + flinkUrl + " is wrong!");
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "flink url " + flinkUrl + " is wrong!");
        }
    }

    @Override
    public BaseResponse getExeSql(int taskId, String executionDate, Integer version) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return BaseResponse.error(BaseResponseCodeEnum.TASK_NOT_EXISTS);
        }
        if (!TemplateEnum.isOfflineSqlTemplate(task.getTemplateCode())) {
            return BaseResponse.error(BaseResponseCodeEnum.NOT_SQL_TYPE_TAS);
        }
        try {
            String sql = "";
            if (task.getTemplateCode().equals("SPARKJAR")) {
                sql = task.getMainClassArgs();
                if (version != null && version > 0) {
                    TaskVersion taskVersion = taskVersionService
                            .selectOne(TaskVersion.builder().taskId(task.getId()).version(version).build());
                    if (taskVersion != null) {
                        sql = taskVersion.getMainClassArgs();
                    }
                }
            } else if (task.getTemplateCode().equals("PythonShell")) {
                RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
//                sql = ((JSONObject) JSON.parse(task.getRuntimeConfig())).getString("cmds");
                sql = runtimeConfig.getAdvancedParameters().getCmds();
                if (version != null && version > 0) {
                    TaskVersion taskVersion = taskVersionService.
                            selectOne(TaskVersion.builder().taskId(task.getId()).version(version).build());
                    if (taskVersion != null) {
                        sql = DataCakeTaskConfig.paseRuntimeConfig(taskVersion.getRuntimeConfig()).getAdvancedParameters().getCmds();
                    }
                }
            } else {
                String content = task.getContent();
                if (version != null && version > 0) {
                    TaskVersion taskVersion = taskVersionService
                            .selectOne(TaskVersion.builder().taskId(task.getId()).version(version).build());
                    if (taskVersion != null) {
                        content = taskVersion.getContent();
                    }
                }
                sql = URLDecoder.decode(new String(Base64.getDecoder().decode(content.getBytes())), "UTF-8");
            }

            String renderSql = schedulerService.render(sql, executionDate,task.getName());
            return  BaseResponse.success(renderSql);
        }catch (ServiceException e){
            return  BaseResponse.error(e.getCodeStr(), e.getMessage());
        }catch (Exception e){
            return  BaseResponse.error(BaseResponseCodeEnum.SYS_ERR,e.getMessage());
        }
    }
    @Override
    public String getStateByUid(String uuid){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("uuid", uuid);
        Map<String,String> headers = new HashMap<>();
        headers.put(CommonConstant.CURRENT_LOGIN_USER,JSONObject.toJSONString(InfTraceContextHolder.get().getUserInfo()));
        BaseResponse response = HttpUtil.get(DataCakeConfigUtil.getDataCakeServiceConfig().pipelineHost + "/pipeline/taskInstance/uuid/get", paramMap, headers);
        if (response.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", response.getData()));
        }
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            String msg = jsonObject.getString("msg");
            log.error(String.format("获取状态失败，code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("获取状态失败,%s", msg));
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            return data.getString("ti_state");
        }
    }

    @Override
    public PageInfo<PiplineTaskInstance> offlineTaskInstancePages(int pageNum, int pageSize, Map<String, String> paramMap) {
        //先从调度获取到实例  我在库中查到对应任务进行包装其他操作
        PageInfo<PiplineTaskInstance> allOfflineTaskInstances = getAllOfflineTaskInstances(pageNum, pageSize, paramMap);
        List<String> list = allOfflineTaskInstances.getList().stream().map(PiplineTaskInstance::getName).collect(Collectors.toList());
        if (list == null || list.isEmpty()) {
            return allOfflineTaskInstances;
        }
        Example example = new Example(Task.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        criteria.andIn("name", list);
        List<Task> tasksList = taskService.listByExample(example);
        Map<String, List<Task>> maps = tasksList.stream().collect(Collectors.groupingBy(Task::getName));
        List<PiplineTaskInstance> result = allOfflineTaskInstances.getList().stream().map(data -> {
            List<Task> tasks = maps.get(data.getName());
            if (tasks == null || tasks.isEmpty()) {
                return data;
            }
            Task task = tasks.stream().findFirst().get();
            data.setTaskId(task.getId());
            data.setTemplate_code(task.getTemplateCode());
            data.setWorkflowId(task.getWorkflowId());
            data.setIsOwnerOrCollaborator(isOwnerOrCollaborator(task));
            data.setOnline(online(task));
            data.setIsScanSqlCmd(TemplateEnum.isOfflineSqlTemplate(task.getTemplateCode()));
            data.setIsScanKibana(TemplateEnum.isScanKibana(task.getTemplateCode()));
            data.setIsScanSparkUI(TemplateEnum.isScanSparkUI(task.getTemplateCode()));
            return data;

        }).collect(Collectors.toList());
        return allOfflineTaskInstances;
    }


    private PageInfo<PiplineTaskInstance> getAllOfflineTaskInstances(int pageNum, int pageSize, Map<String, String> paramMap) {
        paramMap.put("page", String.valueOf(pageNum));
        paramMap.put("size", String.valueOf(pageSize));
        paramMap.put("group_uuid", InfTraceContextHolder.get().getUuid());
        log.info("request pipline param is :" + JSONObject.toJSONString(paramMap));
        Map<String, String> headers = new HashMap<>();
        headers.put(CommonConstant.CURRENT_LOGIN_USER, JSONObject.toJSONString(InfTraceContextHolder.get().getUserInfo()));
        BaseResponse response = HttpUtil.get(DataCakeConfigUtil.getDataCakeServiceConfig().getPipelineHost() + "/pipeline/ti/page", paramMap, headers);
        if (response.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", response.getData()));

        }
        log.info("response code is :" + response.getCode());
        log.info("response data is :" + response.getData());
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        Integer code = jsonObject.getInteger("code");

        List<PiplineTaskInstance> piplineTaskInstances = new ArrayList<>();
        Integer count = 0;
        if (code != 0) {
            String msg = response.getMessage();
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("获取任务状态失败，code: %d, message: %s", code, msg));
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            count = data.getInteger("count");
            piplineTaskInstances = JSON.parseArray(data.getString("task_info"), PiplineTaskInstance.class);

        }
        Page<PiplineTaskInstance> instancePage = new Page<>(pageNum, pageSize);
        instancePage.setTotal(count);
        instancePage.addAll(piplineTaskInstances);
        return new PageInfo<>(instancePage);
    }

}
