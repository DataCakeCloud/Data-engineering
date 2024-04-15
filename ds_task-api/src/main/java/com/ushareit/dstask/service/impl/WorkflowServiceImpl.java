package com.ushareit.dstask.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ushareit.dstask.api.workflow.WorkflowRpcServiceGrpc;
import com.ushareit.dstask.api.workflow.WorkflowServiceApi;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.module.CronConfig;
import com.ushareit.dstask.common.module.WorkflowInfo;
import com.ushareit.dstask.common.param.TaskParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TurnType;
import com.ushareit.dstask.entity.CommonResponseOuterClass;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.mapper.WorkflowMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkflowServiceImpl extends AbstractBaseServiceImpl<Workflow> implements WorkflowService {

    private static final String DELETE_NOTIFY_MESSAGE = "任务【%s】被用户【%s】删除，请确认是否对您的任务【%s】有影响";
    private static final String OFFLINE_NOTIFY_MESSAGE = "任务【%s】被用户【%s】下线，请确认是否对您的任务【%s】有影响";
    @Resource
    private WorkflowMapper workflowMapper;
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private DingDingService dingDingService;
    @Resource
    private WorkflowTaskService workflowTaskService;
    @Resource
    private WorkflowVersionService workflowVersionService;
    @Resource
    private TaskService taskService;
    @Resource
    private TaskVersionService taskVersionService;
    @GrpcClient("pipeline-server")
    private WorkflowRpcServiceGrpc.WorkflowRpcServiceBlockingStub workflowRpcServiceBlockingStub;

    @Override
    public CrudMapper<Workflow> getBaseMapper() {
        return workflowMapper;
    }

    @Override
    public WorkflowInfo getCurrentVersionInfo(Integer workflowId) {
        return getWorkflowInfo(workflowId, w -> workflowVersionService.getByVersion(workflowId, w.getCurrentVersion()));
    }

    @Override
    public WorkflowInfo getInfoByVersion(Integer workflowId, Integer version) {
        return getWorkflowInfo(workflowId, w -> workflowVersionService.getByVersion(workflowId, version));
    }

    @Override
    public WorkflowInfo getInfo(Integer workflowId, Integer workflowVersionId) {
        return getWorkflowInfo(workflowId, w -> workflowVersionService.getVersionById(workflowId, workflowVersionId));
    }

    @Override
    @Transactional
    public void addOne(Workflow workflow, WorkflowVersion workflowVersion, List<TaskParam> taskList) {
        save(workflow.setCurrentVersion(workflowVersion.getVersion()));
        Set<Integer> taskIds = saveOrUpdateTaskList(taskList, workflow.getId());

        workflowVersionService.save(workflowVersion.setWorkflowId(workflow.getId()));
        Map<Integer, TaskVersion> taskVersionMap = taskVersionService.getLatestVersion(taskIds);

        workflowTaskService.addList(workflow.getId(), workflowVersion.getId(), taskVersionMap.values());
        turnOn(workflow.getId(), workflowVersion.getId(), false);
    }

    @Override
    @Transactional
    public void updateOne(Workflow workflow, WorkflowVersion workflowVersion, List<TaskParam> taskList,
                          WorkflowInfo originWorkflowInfo, Boolean notify) {
        update(workflow);
        // 新建或更新任务版本
        Set<Integer> taskIds = saveOrUpdateTaskList(taskList, workflow.getId());

        workflowVersionService.save(workflowVersion);
        Map<Integer, TaskVersion> taskVersionMap = taskVersionService.getLatestVersion(taskIds);
        workflowTaskService.addList(workflow.getId(), workflowVersion.getId(), taskVersionMap.values());

        // 上线最新版本
        turnOn(workflow.getId(), workflowVersion.getId(), notify);
    }

    @Override
    public List<Workflow> searchByName(String name) {
        Example example = new Example(Workflow.class);
        example.or()
                .andEqualTo("name", name)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return workflowMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public void deleteAndNotify(WorkflowInfo workflowInfo, boolean notify) {
        Workflow toDeleteParam = new Workflow();
        toDeleteParam.setId(workflowInfo.getId());
        toDeleteParam.setDeleteStatus(DeleteEntity.DELETE);
        update(toDeleteParam);

        // 通知调度系统删除工作流
        CommonResponseOuterClass.CommonResponse response = workflowRpcServiceBlockingStub
                .deleteWorkflow(WorkflowServiceApi.DeleteWorkflowRequest.newBuilder()
                        .setWorkflowId(workflowInfo.getId())
                        .build());
        if (response.getCode() != NumberUtils.INTEGER_ZERO) {
            log.error("invoke delete for workflow {} failed, response code is {} message is {}", workflowInfo.getId(),
                    response.getCode(), response.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getMessage());
        }

        // 通知任务删除
        if (notify) {
            getDownTaskList(workflowInfo.getTaskIds()).forEach(item -> dingDingService.notify(
                    Collections.singletonList(item.getValue().getCreateBy()),
                    String.format(DELETE_NOTIFY_MESSAGE, item.getKey().getName(),
                            InfTraceContextHolder.get().getUserName(), item.getValue().getName())));
        }

        // 真正删除历史所有版本内的任务
        workflowTaskService.getHistoryTaskIds(workflowInfo.getId()).forEach(item -> {
            Task toUpdateParam = new Task();
            toUpdateParam.setId(item);
            toUpdateParam.setDeleteStatus(DeleteEntity.DELETE);
            toUpdateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
            taskMapper.updateByPrimaryKeySelective(toUpdateParam);
        });
    }

    @Override
    @Transactional
    public void turnOn(Integer workflowId, Integer toOnlineWorkflowVersionId, boolean notify) {
        Optional<WorkflowVersion> currentWorkflowVersionOptional = workflowVersionService.getCurrentVersion(workflowId);
        if (!currentWorkflowVersionOptional.isPresent()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流当前版本不存在");
        }

        Integer currentWorkflowVersionId = currentWorkflowVersionOptional.get().getId();
        // 要上线版本不是当前版本，当前版本调整为下线
        if (currentWorkflowVersionId != toOnlineWorkflowVersionId.intValue()) {
            workflowVersionService.offline(workflowId, currentWorkflowVersionId);

            // 通知下游任务下线
            if (notify) {
                List<Integer> offlineTaskIds = workflowTaskService.getSubsetByWorkflowVersionIds(workflowId,
                        currentWorkflowVersionId, toOnlineWorkflowVersionId);
                log.info("diff task ids for last workflowVersionId {} to new workflowVersionID {} is {}",
                        currentWorkflowVersionId, toOnlineWorkflowVersionId, offlineTaskIds);

                getDownTaskList(offlineTaskIds).forEach(item -> dingDingService.notify(
                        Collections.singletonList(item.getValue().getCreateBy()),
                        String.format(OFFLINE_NOTIFY_MESSAGE, item.getKey().getName(),
                                InfTraceContextHolder.get().getUserName(), item.getValue().getName())));
            }
        }

        // 新版本状态置为上线
        workflowVersionService.online(workflowId, toOnlineWorkflowVersionId);

        // 新版本任务置为上线
        List<Pair<Integer, Integer>> toOnlineTaskVersionPairs = workflowTaskService.getTaskIdVersionPairs(workflowId,
                toOnlineWorkflowVersionId);
        taskVersionService.batchTurnTaskVersions(toOnlineTaskVersionPairs, TurnType.ON);

        // 获取即将上线的工作流信息
        Workflow toOnlineWorkflow = workflowMapper.selectByPrimaryKey(workflowId);

        // 调用 rpc 上线接口
        CommonResponseOuterClass.CommonResponse response = workflowRpcServiceBlockingStub.online(
                WorkflowServiceApi.OnlineRequest.newBuilder()
                        .setWorkflowId(workflowId)
                        .setWorkflowName(toOnlineWorkflow.getName())
                        .setCrontab(CronConfig.parseToExpression(toOnlineWorkflow.getGranularity(),
                                toOnlineWorkflow.getCronConfig()))
                        .setGranularity(toOnlineWorkflow.getGranularity())
                        .setVersion(toOnlineWorkflow.getCurrentVersion())
                        .addAllJobs(taskService.toScheduleJobByIds(toOnlineTaskVersionPairs, true))
                        .build());
        if (response.getCode() != NumberUtils.INTEGER_ZERO) {
            log.error("invoke online for workflow {} failed, response code is {} message is {}", workflowId,
                    response.getCode(), response.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getMessage());
        }
    }

    @Override
    @Transactional
    public void turnOff(WorkflowInfo workflowInfo, boolean notify) {
        workflowVersionService.offline(workflowInfo.getId(), workflowInfo.getWorkflowVersion().getId());
        taskVersionService.batchTurnTaskVersions(workflowInfo.getTaskVersionList(), TurnType.OFF);

        // 调用 rpc 下线接口
        CommonResponseOuterClass.CommonResponse response = workflowRpcServiceBlockingStub.offline(
                WorkflowServiceApi.OfflineRequest.newBuilder()
                        .setWorkflowId(workflowInfo.getId())
                        .build());
        if (response.getCode() != NumberUtils.INTEGER_ZERO) {
            log.error("invoke offline for workflow {} failed, response code is {} message is {}", workflowInfo.getId(),
                    response.getCode(), response.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getMessage());
        }

        // 钉钉通知下有任务负责人
        if (notify) {
            getDownTaskList(workflowInfo.getTaskIds()).forEach(item -> dingDingService.notify(
                    Collections.singletonList(item.getValue().getCreateBy()),
                    String.format(OFFLINE_NOTIFY_MESSAGE, item.getKey().getName(),
                            InfTraceContextHolder.get().getUserName(), item.getValue().getName())));
        }
    }

    @Override
    public void debugTaskList(String username, String chatId, List<Task> taskList) {
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "调试工作流，需至少包含一个任务");
        }

        WorkflowServiceApi.DebugTaskRequest request = WorkflowServiceApi.DebugTaskRequest.newBuilder()
                .setUserName(username)
                .setChatId(chatId)
                .addAllScheduleJob(taskList.stream()
                        .map(item -> taskService.toScheduleJob(item, true))
                        .collect(Collectors.toList()))
                .build();

        // 调用 rpc 调试接口
        CommonResponseOuterClass.CommonResponse response = workflowRpcServiceBlockingStub.debugTask(request);
        if (response.getCode() != NumberUtils.INTEGER_ZERO) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getMessage());
        }
    }

    @Override
    public void stopDebugTask(String username, String chatId, Collection<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return;
        }

        CommonResponseOuterClass.CommonResponse response = workflowRpcServiceBlockingStub.stopTask(
                WorkflowServiceApi.StopTaskRequest.newBuilder()
                        .setUserName(username)
                        .setChatId(chatId)
                        .addAllTaskId(taskIds)
                        .build());
        if (response.getCode() != NumberUtils.INTEGER_ZERO) {
            log.error("invoke stop taskIds {} for chat {} failed, response code is {} message is {}", taskIds, chatId,
                    response.getCode(), response.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getMessage());
        }
    }

    @Override
    public List<Pair<Task, Task>> getDownTaskList(Collection<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }

        return taskIds.stream().map(item -> {
                    Task currentTask = taskMapper.selectByPrimaryKey(item);
                    List<Task> tasks = taskMapper.selectChildrenByEventDepends(item).stream()
                            .filter(one -> !taskIds.contains(one.getId()))
                            .collect(Collectors.toList());
                    return Pair.create(currentTask, tasks);
                }).flatMap(item -> item.getValue().stream().map(one -> Pair.create(item.getKey(), one)))
                .collect(Collectors.toList());
    }

    private Set<Integer> saveOrUpdateTaskList(List<TaskParam> taskList, Integer workflowId) {
        List<Pair<Optional<String>, Integer>> keyTaskIdPairs = taskList.stream()
                .map(item -> resolveTask(item, workflowId))
                .collect(Collectors.toList());

        taskService.swapTaskKeys(keyTaskIdPairs, getEventDependsParser());
        taskVersionService.swapTaskKeys(keyTaskIdPairs, getEventDependsParser());

        return keyTaskIdPairs.stream().map(Pair::getValue).collect(Collectors.toSet());
    }

    /**
     * 获取工作流详情
     *
     * @param workflowId     工作流ID
     * @param versionFetcher 工作流版本提供函数
     * @return 工作流详情
     */
    private WorkflowInfo getWorkflowInfo(Integer workflowId, Function<Workflow, Optional<WorkflowVersion>> versionFetcher) {
        Workflow workflow = workflowMapper.selectByPrimaryKey(workflowId);
        if (workflow == null || workflow.getDeleteStatus() == DeleteEntity.DELETE.intValue()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流不存在，请核实");
        }

        Optional<WorkflowVersion> workflowVersionOptional = versionFetcher.apply(workflow);
        if (!workflowVersionOptional.isPresent()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流版本不存在，请核实");
        }

        List<WorkflowTask> workflowTaskList = workflowTaskService.getTaskList(workflowId,
                workflowVersionOptional.get().getId());
        if (CollectionUtils.isEmpty(workflowTaskList)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流要至少包含一个任务");
        }

        return new WorkflowInfo(workflow, workflowVersionOptional.get(), workflowTaskList);
    }

    private Pair<Optional<String>, Integer> resolveTask(TaskParam t, Integer workflowId) {
        if (workflowId == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "工作流ID不能为空");
        }
        t.setWorkflowId(workflowId);

        if (t.getId() == null) {
            Task task = (Task) taskService.save(t);
            return Pair.create(Optional.of(t.getTaskKey()), task.getId());
        }

        taskService.update(t);
        return Pair.create(Optional.empty(), t.getId());
    }

    private BiFunction<String, Map<String, Integer>, List<EventDepend>> getEventDependsParser() {
        return (eventString, keyToIdMap) -> Jsons.deserialize(eventString, new TypeReference<List<EventDepend>>() {
        }).stream().peek(one -> {
            if (one.getTaskId() != null) {
                return;
            }

            if (StringUtils.isBlank(one.getTaskKey())) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "依赖任务的 taskKey 不能为空");
            }

            if (!keyToIdMap.containsKey(one.getTaskKey())) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "不存在的 taskKey");
            }

            one.setTaskId(String.valueOf(keyToIdMap.get(one.getTaskKey())));
            one.setTaskKey(null);
        }).collect(Collectors.toList());
    }
}
