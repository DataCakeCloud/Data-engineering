package com.ushareit.dstask.web.autoscale;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskInstanceServiceImpl;
import com.ushareit.dstask.web.autoscale.strategy.AbstractStrategy;
import lombok.Data;

import java.util.Set;

@Data
public class Context {
    public String topic;

    private Integer currentPar = 0;

    public AbstractStrategy targetAbstractStrategy;

    private Integer targetPar;

    private Task task;

    public String resultStrategy;

    public Set<String> brokerSet;

    public Integer kafkaPartion;

    private TaskInstanceServiceImpl taskInstanceServiceImpl;

    public com.ushareit.dstask.web.autoscale.factory.StrategyChain StrategyChain;

    public Context(Task task, TaskInstanceServiceImpl taskInstanceServiceImpl) {
        this.task = task;
        this.taskInstanceServiceImpl = taskInstanceServiceImpl;
    }

}
