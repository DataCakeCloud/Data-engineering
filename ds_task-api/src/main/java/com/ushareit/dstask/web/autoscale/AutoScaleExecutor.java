package com.ushareit.dstask.web.autoscale;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.constant.TaskScaleStrategyConstant;
import com.ushareit.dstask.service.impl.TaskInstanceServiceImpl;
import com.ushareit.dstask.web.autoscale.factory.StrategyChain;
import com.ushareit.dstask.web.autoscale.strategy.AbstractStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * author xuebotao
 * date 2022-01-20
 */

@Slf4j
public class AutoScaleExecutor {

    public Task task;

    /**
     * @param taskInstanceService
     * @param task
     */
    public void checkRunningJobPar(TaskInstanceServiceImpl taskInstanceService, Task task) {
        try {
            List<TaskScaleStrategy> strategyList = taskInstanceService.taskScaleStrategyService.selectByTaskId(task.getId());
            Context context = new Context(task, taskInstanceService);
            StrategyChain strategyChain = new StrategyChain(strategyList, context);
            strategyChain.doStrategy();
            doAutoScale(context);
        } catch (Exception e) {
            log.error("task id:" + task.getId() + " 的任务执行自动伸缩策略失败;" + e.getMessage(), e);
        }
    }

    /**
     * 获取相应要改变的策略去执行
     */
    public void doAutoScale(Context context) {
        List<AbstractStrategy> strategyList = context.getStrategyChain().strategyList;
        if (strategyList.isEmpty() || context.getCurrentPar() == 0) {
            return;
        }
        //获取伸缩并行度并且交给固定策略执行后续
        getScalePar(context, strategyList);
        log.info("res par is {}", context.getTargetPar());
        if (context.getTargetPar() == Integer.MAX_VALUE || context.getTargetAbstractStrategy() == null || context.getTargetPar() <= 0
                || Objects.equals(context.getTargetPar(), context.getCurrentPar())) {
            return;
        }
        context.getTargetAbstractStrategy().execute();
    }


    /**
     * 最后获取伸缩的并行度
     */
    public void getScalePar(Context context, List<AbstractStrategy> strategyList) {
        Integer appropriatePar = context.getCurrentPar();
        int minValue = Integer.MAX_VALUE;
        for (AbstractStrategy abstractStrategy : strategyList) {
            if (abstractStrategy.name.equals(TaskScaleStrategyConstant.PERIODIC_STRATEGY)) {
                context.setTargetAbstractStrategy(abstractStrategy);
                context.setTargetPar(abstractStrategy.getResultPar());
                return;
            }
            int abs = Math.abs(abstractStrategy.getResultPar() - context.getCurrentPar());
            if (abstractStrategy.getResultPar() != 0 && abs <= minValue) {
                if (abs == minValue && context.getTargetAbstractStrategy() != null
                        && context.getTargetAbstractStrategy().getResultPar() > abstractStrategy.getResultPar()) {
                    continue;
                }
                context.setTargetAbstractStrategy(abstractStrategy);
                minValue = abs;
                appropriatePar = abstractStrategy.getResultPar();
            }
        }
        context.setTargetPar(appropriatePar);
    }
}
