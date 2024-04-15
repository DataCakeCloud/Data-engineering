package com.ushareit.dstask.web.autoscale.factory;


import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TaskScaleStrategyConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.autoscale.Context;
import com.ushareit.dstask.web.autoscale.strategy.AbstractStrategy;
import com.ushareit.dstask.web.autoscale.strategy.KafkaLagStrategy;
import com.ushareit.dstask.web.autoscale.strategy.OperatorProcessStrategy;
import com.ushareit.dstask.web.autoscale.strategy.PeriodicStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 策略工厂
 *
 * @author: xuebotao
 * @create: 2022-01-04
 */
@Slf4j
public class StrategyFactory {

    /**
     * 获取任务伸缩具体实现策略实体
     *
     * @param context
     * @param taskScaleStrategy
     * @return
     */
    public static AbstractStrategy getTaskStrategy(TaskScaleStrategy taskScaleStrategy,
                                                   Context context) {
        switch (taskScaleStrategy.getName()) {
            case TaskScaleStrategyConstant.KAFKA_LAG_STRATEGY:
                return new KafkaLagStrategy(taskScaleStrategy, context);
            case TaskScaleStrategyConstant.OPERATOR_PROCESS_STRATEGY:
                return new OperatorProcessStrategy(taskScaleStrategy, context);
            case TaskScaleStrategyConstant.PERIODIC_STRATEGY:
                return new PeriodicStrategy(taskScaleStrategy, context);
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this scale strategy:" + taskScaleStrategy.getName());
        }
    }

}
