package com.ushareit.dstask.web.autoscale.strategy;


import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.web.autoscale.Context;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * author xuebotao
 * date 2021-12-30
 * 算子操作策略
 */
@Slf4j
public class OperatorProcessStrategy extends AbstractStrategy {

    public FlinkCluster cluster;

    public OperatorProcessStrategy(TaskScaleStrategy taskScaleStrategy, Context context) {
        super(taskScaleStrategy, context);
    }

    /**
     * 获取建议并行度
     *
     * @return
     */
    @Override
    public Integer getAdvicePar() {
        cluster = taskInstanceServiceImpl.flinkClusterService.getById(task.getFlinkClusterId());
        String topic = context.getTopic();
        if (topic == null || StringUtils.isEmpty(topic)) {
            topic = getTopic(task);
            context.setTopic(topic);
        }
        Set<String> kafkaBrokerSet = context.getBrokerSet();
        if (kafkaBrokerSet == null || kafkaBrokerSet.isEmpty()) {
            kafkaBrokerSet = getKafkaBroker();
        }
        if (kafkaBrokerSet.isEmpty() || topic == null) {
            return 0;
        }
        List<String> kafkaBrokerList = new ArrayList(kafkaBrokerSet);
        String adviceWhere = String.format("sum(rate(kafka_topic_partition_current_offset{project='%s', topic='%s', instance=~'%s' }[5m])) " +
                        "/ min(1000000 / (avg(flink_taskmanager_job_task_operator_recordDeserializeTime {app='%s',cluster='%s' ,quantile='0.99'} " +
                        "+ flink_taskmanager_job_task_operator_recordProcessTime{app='%s',cluster='%s',quantile='0.99'}) by (operator_name))) ",
                strategy.getKafkaCluster(), topic, kafkaBrokerList.stream().findFirst().orElse(null),
                task.getName(), cluster.getAddress(), task.getName(), cluster.getAddress());
        log.info("getAdvicePar request hawkeye query key is :{}", adviceWhere);
        HawKeyeResult hawKeyeResult = getHawkeyeRes(adviceWhere);
        List<HawKeyeResult.Data.CoreResult> resultList = hawKeyeResult.getData().getResult();
        if (resultList != null && !resultList.isEmpty()) {
            double par = Double.parseDouble(resultList.stream().findFirst().orElse(null).getValue()[1]);
            return (int) Math.ceil(par);
        }
        return 0;
    }


    @Override
    public Integer getTargetPar() throws Exception {
        resultPar = super.getTargetPar();
        double res = 0.0;
        BigDecimal poorBigDecimal = new BigDecimal(Math.abs(advicePar - currentPar));
        BigDecimal nowBigDecimal = new BigDecimal(currentPar);
        if (nowBigDecimal.intValue() != 0) {
            res = poorBigDecimal.divide(nowBigDecimal, 2, RoundingMode.HALF_UP).doubleValue();
        }
        if (!(res > strategy.getMinimumPercentage())) {
            resultPar = 0;
        }
        return resultPar;
    }

    /**
     * 获取topic
     *
     * @param task
     * @return
     */
    public String getTopic(Task task) {
        String topicWhere = String.format("(flink_taskmanager_job_task_operator_KafkaConsumer_topic_partition_committedOffsets{app='%s'},topoic)", task.getName());
        log.info("getTopic request hawkeye query key is :{}", topicWhere);
        HawKeyeResult hawKeyeResult = getHawkeyeRes(topicWhere);
        List<HawKeyeResult.Data.CoreResult> resultList = hawKeyeResult.getData().getResult();
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.stream().findFirst().orElse(null).getMetric().getTopic();
        }
        return null;
    }

}
