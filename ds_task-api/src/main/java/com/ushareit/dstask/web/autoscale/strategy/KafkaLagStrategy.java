package com.ushareit.dstask.web.autoscale.strategy;


import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.bean.HawKeyeResult;
import com.ushareit.dstask.web.autoscale.Context;
import com.ushareit.dstask.web.utils.HawkeyeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * author xuebotao
 * date 2022-01-04
 * kafka lag策略
 */
@Slf4j
public class KafkaLagStrategy extends AbstractStrategy {

    public KafkaLagStrategy(TaskScaleStrategy taskScaleStrategy, Context context) {
        super(taskScaleStrategy, context);
    }

    /**
     * 获取建议并行度
     * 获取5分钟之内kafka lag是否都超过某值来判断
     *
     * @return
     */
    @Override
    public Integer getAdvicePar() {
        currentPar = context.getCurrentPar();
        context.setTopic(strategy.getTopic());
        if (currentPar == 0) {
            return 0;
        }
        //要获取broker
        Set<String> kafkaBrokerSet =context.getBrokerSet();
        if (context.brokerSet == null || context.brokerSet.isEmpty()) {
            kafkaBrokerSet = getKafkaBroker();
        }
        List<String> kafkaBrokerList = new ArrayList(kafkaBrokerSet);
        if (kafkaBrokerList.isEmpty()) {
            log.info("get kafka brokerList is null !!!");
            return 0;
        }
        //获取kafka lag
        String kafkaLagWhere = String.format("sum((kafka_consumergroup_lag{project='%s', topic='%s',consumergroup='%s', instance=~'%s'})) by (topic, consumergroup)",
                strategy.getKafkaCluster(), strategy.getTopic(), strategy.getConsumerGroup(),
                kafkaBrokerList.stream().findFirst().orElse(null));
        log.info("get kafkalag request hawkeye query key is :{}", kafkaLagWhere);
        HashMap<String, String> requestMap = new HashMap<>();
        long time = new Date().getTime();
        Integer nowTimestamp = Integer.valueOf(String.valueOf(time / 1000));
        requestMap.put(HawkeyeUtil.QUERY, kafkaLagWhere);
        int earliestTimestamp = nowTimestamp - strategy.getDelayTime();
        requestMap.put("start", Integer.toString(earliestTimestamp));
        requestMap.put("end", nowTimestamp.toString());
        HawKeyeResult hawKeyeResult = HawkeyeUtil.requestHawkeye(HawkeyeUtil.RANGE_URL, requestMap);
        List<HawKeyeResult.Data.CoreResult> resultList = hawKeyeResult.getData().getResult();
        if (resultList != null && !resultList.isEmpty()) {
            String[][] resLag = resultList.stream().findFirst().orElse(null).getValues();
            Integer judgeFlag = judgeIsScale(resLag);
            if (judgeFlag != 0) {
                return judgeFlag == 1 ? (int) Math.round(currentPar * strategy.getExpandProportion())
                        : (int) Math.round(currentPar / strategy.getExpandProportion());
            }
        }
        return 0;
    }


    /**
     * 判断是扩容 还是缩容
     *
     * @param resLag
     * @return
     */
    public Integer judgeIsScale(String[][] resLag) {
        if (strategy.getMaxDelayCount() <= strategy.getMinDelayCount()) {
            return 0;
        }
        boolean expansion = true;
        boolean shrink = true;
        for (String[] value : resLag) {
            if (Integer.parseInt(value[1]) < strategy.getMaxDelayCount()) {
                expansion = false;
            }
            if (Integer.parseInt(value[1]) > strategy.getMinDelayCount()) {
                shrink = false;
            }
        }
        if (expansion) {
            return 1;
        }
        if (shrink) {
            return -1;
        }
        return 0;
    }


}
