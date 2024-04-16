package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.constant.TurnType;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface TaskVersionService extends BaseService<TaskVersion> {
    /**
     * 根据应用ID查询最大版本号
     *
     * @param id
     * @return
     */
    int getMaxVersionById(Integer id);


    /**
     * 根据应用ID查询最大版本号
     *
     * @param id
     * @return
     */
    TaskVersion selectByIdAndVersion(Integer id, Integer version);


    /**
     * task版本切换
     */
    void verionSwitch(TaskVersion taskVersion);

    void setMaxVersions(List<Task> tasks);

    /**
     * 获取指定 taskId 集合的最新版本
     *
     * @param taskIds 任务ID集合
     * @return 任务最新版本集合
     */
    Map<Integer, TaskVersion> getLatestVersion(Collection<Integer> taskIds);

    /**
     * 替换任务最新版本 event_depends 的 taskKey 信息
     *
     * @param taskKeyIdPairs     taskId & taskKey 对应关系
     * @param eventDependsParser eventDepends 转换函数
     */
    void swapTaskKeys(List<Pair<Optional<String>, Integer>> taskKeyIdPairs,
                      BiFunction<String, Map<String, Integer>, List<EventDepend>> eventDependsParser);

    /**
     * 批量上下线任务的版本
     *
     * @param idVersionPairs 任务版本集合
     */
    void batchTurnTaskVersions(Collection<Pair<Integer, Integer>> idVersionPairs, TurnType turnType);

    List<TaskVersion> list(TaskVersion taskVersion);
}
