package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.bean.WorkflowTask;
import com.ushareit.dstask.mapper.WorkflowTaskMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.WorkflowTaskService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkflowTaskServiceImpl extends AbstractBaseServiceImpl<WorkflowTask> implements WorkflowTaskService {

    @Resource
    private WorkflowTaskMapper workflowTaskMapper;

    @Override
    public CrudMapper<WorkflowTask> getBaseMapper() {
        return workflowTaskMapper;
    }

    @Override
    public List<WorkflowTask> getTaskList(@NotNull Integer workflowId, @NotNull Integer workflowVersionId) {
        Example example = new Example(WorkflowTask.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("workflowVersionId", workflowVersionId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        return workflowTaskMapper.selectByExample(example);
    }

    @Override
    public void addList(Integer workflowId, Integer workflowVersionId, Collection<TaskVersion> taskVersionList) {
        taskVersionList.stream().map(item -> {
            WorkflowTask workflowTask = new WorkflowTask()
                    .setTaskId(item.getTaskId())
                    .setTaskVersion(item.getVersion())
                    .setWorkflowId(workflowId)
                    .setWorkflowVersionId(workflowVersionId);
            workflowTask.setCreateBy(InfTraceContextHolder.get().getUserName());
            workflowTask.setUpdateBy(InfTraceContextHolder.get().getUserName());
            return workflowTask;
        }).forEach(this::save);
    }

    @Override
    public Set<Integer> getHistoryTaskIds(Integer workflowId) {
        Example example = new Example(WorkflowTask.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        return workflowTaskMapper.selectByExample(example).stream()
                .map(WorkflowTask::getTaskId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Pair<Integer, Integer>> getTaskIdVersionPairs(Integer workflowId, Integer workflowVersionId) {
        Example example = new Example(WorkflowTask.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("workflowVersionId", workflowVersionId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        return workflowTaskMapper.selectByExample(example).stream()
                .map(item -> Pair.create(item.getTaskId(), item.getTaskVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<WorkflowTask>> getWorkflowVersionTaskMap(Collection<Integer> workflowVersionIds) {
        if (CollectionUtils.isEmpty(workflowVersionIds)) {
            return Collections.emptyMap();
        }
        
        Example example = new Example(WorkflowTask.class);
        example.or()
                .andIn("workflowVersionId", workflowVersionIds)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        return workflowTaskMapper.selectByExample(example).stream()
                .collect(Collectors.groupingBy(WorkflowTask::getWorkflowVersionId));
    }

    @Override
    public List<Integer> getSubsetByWorkflowVersionIds(Integer workflowId, Integer lastWorkflowVersionId, Integer newWorkflowVersionId) {
        List<Integer> lastTaskIds = getTaskList(workflowId, lastWorkflowVersionId).stream()
                .map(WorkflowTask::getTaskId).collect(Collectors.toList());
        List<Integer> newTaskIds = getTaskList(workflowId, newWorkflowVersionId).stream()
                .map(WorkflowTask::getTaskId).collect(Collectors.toList());
        return ListUtils.subtract(lastTaskIds, newTaskIds);
    }
}
