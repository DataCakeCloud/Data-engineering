package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.WorkflowVersion;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author fengxiao
 * @date 2022/11/11
 */
public interface WorkflowVersionService extends BaseService<WorkflowVersion> {

    /**
     * 获取工作流的最新版本
     *
     * @param workflowId 工作流ID
     * @return 最新版本信息
     */
    Optional<WorkflowVersion> getLatestVersionById(Integer workflowId);

    /**
     * 获取指定ID的工作流版本信息
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本在DB中的自增ID
     */
    Optional<WorkflowVersion> getVersionById(Integer workflowId, Integer workflowVersionId);

    /**
     * 获取工作流版本信息
     *
     * @param workflowId 工作流ID
     */
    Optional<WorkflowVersion> getCurrentVersion(Integer workflowId);

    /**
     * 获取指定ID的工作流版本信息
     *
     * @param workflowId 工作流ID
     * @param version    版本号
     */
    Optional<WorkflowVersion> getByVersion(Integer workflowId, Integer version);

    /**
     * 将当前某版本的工作流标记为下线
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本在DB中的自增ID，如果为空，则下线正在处于上线状态的版本
     */
    void offline(Integer workflowId, Integer workflowVersionId);

    /**
     * 上线指定的工作流版本
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本在DB中的自增ID
     */
    void online(Integer workflowId, Integer workflowVersionId);

    /**
     * 通过工作流ID获取工作流最新版本信息
     *
     * @param idVersionPairs 工作流ID和版本集合
     * @return 最新工作流版本信息 <taskId, workflowVersion>
     */
    Map<Integer, Optional<WorkflowVersion>> getWorkflowVersionList(Collection<Pair<Integer, Integer>> idVersionPairs);

}
