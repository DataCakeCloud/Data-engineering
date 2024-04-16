package com.ushareit.dstask.service;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.web.vo.BaseResponse;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface TaskInstanceService extends BaseService<TaskInstance> {
    /**
     * 取消flink中的作业，并实时更新job表中job的状态 部署表中部署的当前状态
     *
     * @param task
     * @param job
     */
    void cancelJob(Task task, TaskInstance job);

    String stopEtlJob(String name, String status, String executionDate, String flinkUrl);

    /**
     * 根据部署id获取最新的job
     *
     * @param taskId
     * @return
     */
    TaskInstance getLatestJobByTaskId(Integer taskId);

    /**
     * 根据部署id获取最新的job
     *
     * @param taskId
     * @return
     */
    TaskInstance getOnlyLatestJobByTaskId(Integer taskId);

    /**
     * 获取部署对应的活跃的所有作业
     *
     * @param taskId
     * @return
     */
    TaskInstance getAliveJobs(Integer taskId);

    /**
     * 停止并触发保存点
     *
     * @param taskId
     */
    void stopWithSavepoint(Integer taskId);

    /**
     * 重跑
     *
     * @param taskName
     * @param executionDate
     */
    void clear(String taskName, String[] executionDate, Boolean isCheckUpstream);


    /**
     * 批量重跑
     */
    void clear(List<ClearStopParam> clearStopParamList);

    /**
     * 批量停止
     */
    String stopEtlJob(List<ClearStopParam> clearStopParamList);


    /**
     * 检查部署中是否有活跃的job以及活跃的job的数量
     * 如果有running的状态，报错
     *
     * @param appId
     * @return
     */
    void checkJobIsAlive(Integer appId);

    /**
     * 检测实例异常
     * @param taskName
     * @param executionDate
     */
    TaskInstanceDiagnosis diagnose(String taskName, String executionDate, String state);

    BaseResponse deleteDeploy(String name, String context);

    Map<String, Object> page(int pageNum, int pageSize, Map<String, String> paramMap);

    BaseResponse getExeSql(int taskId, String executionDate, Integer version);

    void monitorNonTerminalJob();

    void monitorAutoScaleJob();

    String getStateByUid(String uuid);

    PageInfo<PiplineTaskInstance> offlineTaskInstancePages(int pageNum, int pageSize, Map<String, String> paramMap);


}
