package com.ushareit.dstask.common.param;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.common.action.Create;
import com.ushareit.dstask.common.action.Update;
import com.ushareit.dstask.common.function.TaskFunctions;
import com.ushareit.dstask.common.module.CronConfig;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/14
 */
@Data
public class WorkFlowParam {

    @NotNull(message = "工作流ID不能为空", groups = Update.class)
    @Null(message = "工作流ID应为空", groups = Create.class)
    private Integer id;

    @NotNull(message = "编辑来源的工作流版本ID不能为空", groups = Update.class)
    private Integer originWorkflowVersionId;

    @NotBlank(message = "工作流名称不能为空", groups = {Create.class})
    private String name;

    @Pattern(regexp = "new|history", message = "不支持的工作流来源取值", groups = {Create.class})
    @NotBlank(message = "工作来源不能为空", groups = {Create.class})
    private String source;

    @NotBlank(message = "粒度不能为空", groups = {Create.class})
    private String granularity;

    @NotBlank(message = "负责人不能为空", groups = {Create.class})
    private String owner;

    @Size(max = 5, message = "协作者最多不能超过5人", groups = {Create.class, Update.class})
    private List<String> collaborators;

    private String description;

    @NotNull(message = "调度配置不能为空", groups = {Create.class})
    private CronConfig cronConfig;

    private List<Integer> groupList;

    private Boolean notify;

    private List<TaskParam> taskList;

    public Workflow toAddWorkflow() {
        if (!StringUtils.equalsIgnoreCase(getOwner(), InfTraceContextHolder.get().getUserName())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "新建工作流负责人必须是创建人");
        }

        Workflow workflow = new Workflow();
        workflow.setSource(source);
        workflow.setName(name);
        workflow.setStatus(WorkflowStatus.ONLINE.getType());
        workflow.setGranularity(granularity);
        workflow.setCronConfig(JSONObject.toJSONString(cronConfig));
        workflow.setDescription(description);

        workflow.setUserGroup(InfTraceContextHolder.get().getGroupId());

        workflow.setCreateBy(InfTraceContextHolder.get().getUserName());
        workflow.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return workflow;
    }

    public Workflow toUpdateWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setId(id);
        if (StringUtils.isNotBlank(name)) {
            workflow.setName(name);
        }

        if (StringUtils.isNotBlank(description)) {
            workflow.setDescription(description);
        }

        if (StringUtils.isNotBlank(granularity)) {
            workflow.setGranularity(granularity);
        }

        workflow.setUserGroup(InfTraceContextHolder.get().getGroupId());

        workflow.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return workflow;
    }

    public WorkflowVersion toAddWorkflowVersion() {
        WorkflowVersion workflowVersion = new WorkflowVersion();
        workflowVersion.setGranularity(granularity);
        workflowVersion.setOwner(owner);
        workflowVersion.setName(name);

        if (CollectionUtils.isNotEmpty(collaborators)) {
            workflowVersion.setCollaborators(String.join(SymbolEnum.COMMA.getSymbol(), collaborators));
        }

        if (groupList != null) {
            workflowVersion.setUserGroup(InfTraceContextHolder.get().getGroupId());
        }

        workflowVersion.setStatus(WorkflowStatus.ONLINE.getType());
        workflowVersion.setVersion(NumberUtils.INTEGER_ONE);
        workflowVersion.setDescription(description);
        workflowVersion.setStatus(WorkflowStatus.CREATED.getType());
        workflowVersion.setCronConfig(JSONObject.toJSONString(cronConfig));
        workflowVersion.setCreateBy(InfTraceContextHolder.get().getUserName());
        workflowVersion.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return workflowVersion;
    }

    /**
     * 如果用户没传，则用上一个版本的信息替代
     *
     * @param originWorkflowVersion 来源工作流版本的信息
     */
    public WorkflowVersion toUpdateWorkflowVersion(WorkflowVersion originWorkflowVersion) {
        WorkflowVersion workflowVersion = new WorkflowVersion();
        workflowVersion.setWorkflowId(this.id);
        workflowVersion.setGranularity(StringUtils.defaultIfBlank(this.granularity, originWorkflowVersion.getGranularity()));
        workflowVersion.setName(StringUtils.defaultIfBlank(this.name, originWorkflowVersion.getName()));
        workflowVersion.setOwner(StringUtils.defaultIfBlank(this.getOwner(), originWorkflowVersion.getOwner()));

        if (collaborators != null) {
            workflowVersion.setCollaborators(String.join(SymbolEnum.COMMA.getSymbol(), collaborators));
        } else {
            workflowVersion.setCollaborators(originWorkflowVersion.getCollaborators());
        }

        if (cronConfig != null) {
            workflowVersion.setCronConfig(JSONObject.toJSONString(cronConfig));
        } else {
            workflowVersion.setCronConfig(originWorkflowVersion.getCronConfig());
        }

//        if (groupList != null) {
//            workflowVersion.setUserGroup(InfTraceContextHolder.get().getGroupId());
//        } else {
//            workflowVersion.setUserGroup(originWorkflowVersion.getUserGroup());
//        }
        workflowVersion.setUserGroup(InfTraceContextHolder.get().getGroupId());

        if (StringUtils.isNotBlank(description)) {
            workflowVersion.setDescription(description);
        }

        workflowVersion.setCreateBy(InfTraceContextHolder.get().getUserName());
        workflowVersion.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return workflowVersion;
    }

    // 新任务时调用
    public WorkFlowParam validate(TaskService taskService) {
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "工作流至少要包含一个任务");
        }

        if (cronConfig != null) {
            GranularityEnum.of(granularity.toUpperCase()).getValidate().apply(cronConfig).ifPresent(exception -> {
                throw exception;
            });
        }

        taskList = ListUtils.emptyIfNull(taskList);
        if (taskList.stream().anyMatch(item -> item.getId() == null && StringUtils.isBlank(item.getTaskKey()))) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "创建任务，taskKey不能为空");
        }

        if (taskList.stream().anyMatch(item -> item.getId() != null && StringUtils.isNotBlank(item.getTaskKey()))) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "历史任务，taskKey不能有值");
        }

        // 新建任务
        if (id == null) {
            if (WorkflowFromEnum.valueOf(source.toUpperCase()) == WorkflowFromEnum.NEW &&
                    taskList.stream().anyMatch(item -> item.getId() != null)) {
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "全新创建的工作流，任务ID必须为空");
            }

            if (WorkflowFromEnum.valueOf(source.toUpperCase()) == WorkflowFromEnum.HISTORY) {
                if (taskList.stream().anyMatch(item -> item.getId() == null)) {
                    throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "历史任务创建的工作流，不能存在新任务");
                }

                if (taskList.stream()
                        .map(one -> TaskFunctions.idToTask(taskService).apply(one.getId()))
                        .map(TaskFunctions.entityToGranularity())
                        .anyMatch(item -> !StringUtils.equalsIgnoreCase(item, this.granularity))) {
                    throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "历史任务粒度需与工作流粒度一致");
                }
            }
        }
        return this;
    }

    // 补充任务的要素
    public List<TaskParam> decoratedTaskList() {
        String username = InfTraceContextHolder.get().getUserName();
        String groupName = InfTraceContextHolder.get().getNewCode();
//        if (StringUtils.isBlank(groupName)) {
//            throw new ServiceException(BaseResponseCodeEnum.NO_GROUP);
//        }
//        String tenancyCode = groupName.split(SymbolEnum.COMMA.getSymbol())[NumberUtils.INTEGER_ZERO];

        return taskList.stream().peek(task -> {
            if (task.getId() == null) {
//                task.setTenancyCode(tenancyCode);
                task.setCreateBy(username);
                task.setCreateTime(new Timestamp(System.currentTimeMillis()));
                task.setUpdateBy(username);
                task.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                task.setOnline(TurnType.ON.getType());
                task.setAuditStatus(BaseActionCodeEnum.CREATE.name());
                task.setTriggerParam(Jsons.serialize(cronConfig.toTriggerParam(this.granularity)));
                task.setEventDepends(formatGranularityForEventDepends(task.getEventDepends()));
                task.setSource(TaskSourceEnum.WORKFLOW.getType());
            } else {
                task.setAuditStatus(BaseActionCodeEnum.UPDATE.name());
                task.setOnline(TurnType.ON.getType());
                task.setUpdateBy(username);
                task.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                task.setEventDepends(formatGranularityForEventDepends(task.getEventDepends()));

                if (this.cronConfig != null) {
                    task.setTriggerParam(Jsons.serialize(cronConfig.toTriggerParam(this.granularity)));
                }
            }
        }).collect(Collectors.toList());
    }

    private String formatGranularityForEventDepends(String eventDepends) {
        if (StringUtils.isBlank(eventDepends)) {
            return eventDepends;
        }

        List<EventDepend> eventDependList = Jsons.deserialize(eventDepends, new TypeReference<List<EventDepend>>() {
        });

        if (CollectionUtils.isEmpty(eventDependList)) {
            return eventDepends;
        }

        List<EventDepend> convertedList = eventDependList.stream().peek(item -> {
            if (item.getTaskId() == null && StringUtils.isNotBlank(item.getTaskKey())) {
                item.setGranularity(this.granularity);
            }
        }).collect(Collectors.toList());
        return Jsons.serialize(convertedList);
    }
}
