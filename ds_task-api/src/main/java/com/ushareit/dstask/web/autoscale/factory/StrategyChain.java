package com.ushareit.dstask.web.autoscale.factory;

import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.web.autoscale.Context;
import com.ushareit.dstask.web.autoscale.strategy.AbstractStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * author xuebotao
 * date 2021-12-30
 * 策略链条
 */
@Slf4j
public class StrategyChain {

    public List<AbstractStrategy> strategyList = new ArrayList<>();

    public StrategyChain(List<TaskScaleStrategy> autoScaleStrategyList, Context context) {
        context.setStrategyChain(this);
        for (TaskScaleStrategy autoScaleStrategy : autoScaleStrategyList) {
            strategyList.add(StrategyFactory.getTaskStrategy(autoScaleStrategy, context));
        }
    }

    /**
     * 链条策略执行
     *
     * @param
     */
    public void doStrategy() {
        for (AbstractStrategy strategy : strategyList) {
            try {
                strategy.doStrategy();
            } catch (Exception e) {
                log.error("Task {} Strategy {} error:{}", strategy.getTask().getId(), strategy.getName(), e.getMessage(), e);
            }
        }
    }

}
