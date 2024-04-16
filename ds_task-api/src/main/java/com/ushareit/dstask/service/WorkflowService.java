package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.common.module.WorkflowInfo;
import com.ushareit.dstask.common.param.TaskParam;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.List;


public interface WorkflowService extends BaseService<Workflow> {

    /**
     * 获取当前版本的工作流全部信息
     *
     * @param workflowId 工作流ID
     */
    WorkflowInfo getCurrentVersionInfo(Integer workflowId);

    /**
     * 获取当前版本的工作流全部信息
     *
     * @param workflowId 工作流ID
     * @param version    版本
     */
    WorkflowInfo getInfoByVersion(Integer workflowId, Integer version);

    /**
     * 获取指定版本的工作流全部信息
     *
     * @param workflowId        工作流ID
     * @param workflowVersionId 工作流版本的DB自增ID
     */
    WorkflowInfo getInfo(Integer workflowId, Integer workflowVersionId);

    /**
     * 添加工作流
     *
     * @param workflow        工作流信息
     * @param workflowVersion 工作流详情信息
     * @param taskList        工作流关联任务集合
     */
    void addOne(Workflow workflow, WorkflowVersion workflowVersion, List<TaskParam> taskList);

    /**
     * 更新工作流
     *
     * @param workflow           工作流信息
     * @param workflowVersion    工作流详情信息
     * @param taskList           工作流关联任务集合
     * @param originWorkflowInfo 工作流上个版本的全部信息
     * @param notify             是否通知已下线任务的下游任务负责人
     */
    void updateOne(Workflow workflow, WorkflowVersion workflowVersion, List<TaskParam> taskList,
                   WorkflowInfo originWorkflowInfo, Boolean notify);

    /**
     * 按名字查找工作流
     *
     * @param name 工作流名字
     * @return 工作流列表
     */
    List<Workflow> searchByName(String name);

    /**
     * 删除工作流并通知下游任务负责人及协作者
     *
     * @param workflowInfo 最新版本工作流信息
     * @param status       工作流状态
     * @param notify       是否通知下游任务
     */
    void deleteAndNotify(WorkflowInfo workflowInfo, boolean notify);

    /**
     * 上线工作流
     *
     * @param workflowId                工作流ID
     * @param toOnlineWorkflowVersionId 即将上线的工作流版本ID
     * @param notify                    是否提醒切到新版本后不存在的任务
     */
    void turnOn(Integer workflowId, Integer toOnlineWorkflowVersionId, boolean notify);

    /**
     * 下线工作流
     *
     * @param workflowInfo 工作流信息
     * @param notify       通知下游任务 owner
     */
    void turnOff(WorkflowInfo workflowInfo, boolean notify);

    /**
     * 批量调试任务
     *
     * @param username 用户名
     * @param chatId   会话ID
     * @param taskList 任务信息
     */
    void debugTaskList(String username, String chatId, List<Task> taskList);

    /**
     * 批量关闭调试任务
     *
     * @param username 用户名
     * @param chatId   会话ID
     * @param taskIds  任务ID列表
     */
    void stopDebugTask(String username, String chatId, Collection<Integer> taskIds);

    /**
     * 获取当前任务的下游任务列表
     *
     * @param taskIds 任务信息
     * @return 下游任务列表
     */
    List<Pair<Task, Task>> getDownTaskList(Collection<Integer> taskIds);

}
