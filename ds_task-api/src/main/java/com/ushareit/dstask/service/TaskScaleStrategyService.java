package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskScaleStrategy;

import java.util.List;

/**
 * @author: xuebotao
 * @create: 2022-01-04
 */
public interface TaskScaleStrategyService extends BaseService<TaskScaleStrategy> {


    List<TaskScaleStrategy> selectByTaskId(Integer id);

    void updateStrategy(Task task);

}
