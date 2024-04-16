package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.bean.WorkflowTask;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WorkflowTaskService extends BaseService<WorkflowTask> {

    /**
     * 获取工作流某个版本下的任务集合
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本ID
     * @return 任务集合
     */
    List<WorkflowTask> getTaskList(Integer workflowId, Integer workflowVersionId);

    /**
     * 添加工作流与任务关联关系
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本自增ID
     * @param taskVersionList   任务版本列表
     */
    void addList(Integer workflowId, Integer workflowVersionId, Collection<TaskVersion> taskVersionList);

    /**
     * 获取工作流的历史任务ID列表
     *
     * @param workflowId 工作流ID
     * @return 历史任务ID集合
     */
    Set<Integer> getHistoryTaskIds(Integer workflowId);

    /**
     * 获取工作流的任务及其版本集合
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本自增ID
     * @return 任务及其版本集合
     */
    List<Pair<Integer, Integer>> getTaskIdVersionPairs(Integer workflowId, Integer workflowVersionId);

    /**
     * 获取工作流特定版本下的任务集合
     *
     * @param workflowVersionIds 工作流版本自增ID集合
     * @return 任务集合
     */
    Map<Integer, List<WorkflowTask>> getWorkflowVersionTaskMap(Collection<Integer> workflowVersionIds);

    /**
     * 获取工作流不同版本任务ID的差集
     *
     * @param workflowId            工作流ID
     * @param lastWorkflowVersionId 工作流上一个版本的 DB ID
     * @param newWorkflowVersionId  工作流下一个版本的 DB ID
     * @return ID 差集
     */
    List<Integer> getSubsetByWorkflowVersionIds(Integer workflowId, Integer lastWorkflowVersionId, Integer newWorkflowVersionId);
}
