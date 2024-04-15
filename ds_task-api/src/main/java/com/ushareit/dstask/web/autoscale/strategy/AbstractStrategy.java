package com.ushareit.dstask.web.autoscale.strategy;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.HawKeyeResult;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskParChange;
import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TaskScaleStrategyConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskInstanceServiceImpl;
import com.ushareit.dstask.web.autoscale.Context;
import com.ushareit.dstask.web.autoscale.Strategy;
import com.ushareit.dstask.web.utils.DingUtil;
import com.ushareit.dstask.web.utils.HawkeyeUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Data
public abstract class AbstractStrategy implements Strategy {
    public Integer MIN_PAR = 1;
    public Integer MAX_PAR = 512;
    public Integer advicePar = 0;
    public Integer currentPar = 0;
    public Integer resultPar = 0;

    public Task task;
    public String name;
    public Context context;
    public Boolean isColling;
    public TaskScaleStrategy taskScaleStrategy;
    public com.ushareit.dstask.bean.Strategy strategy;
    public TaskInstanceServiceImpl taskInstanceServiceImpl;


    public AbstractStrategy(TaskScaleStrategy taskScaleStrategy, Context context) {
        this.taskScaleStrategy = taskScaleStrategy;
        this.context = context;
        this.task = context.getTask();
        this.name = taskScaleStrategy.getName();
        this.taskInstanceServiceImpl = context.getTaskInstanceServiceImpl();
        strategy = JSON.parseObject(taskScaleStrategy.getSpecificStrategy(), com.ushareit.dstask.bean.Strategy.class);
        this.isColling = System.currentTimeMillis() <= context.getTask().getRestartTime().getTime() + taskScaleStrategy.getCoolingTime() * 1000;
        if (strategy.getMinPar() != null && strategy.getMinPar() != 0) {
            MIN_PAR = strategy.getMinPar();
        }
        if (strategy.getMaxPar() != null && strategy.getMaxPar() != 0) {
            MAX_PAR = strategy.getMaxPar();
        }
    }

    @Override
    public Integer getCurrentPar() {
        Integer currentPar = context.getTaskInstanceServiceImpl().taskService.getAutoScaleTaskParal(context.getTask().getId());
        if (currentPar == null || currentPar <= 0) {
            throw new ServiceException(BaseResponseCodeEnum.APP_IS_DELETE);
        }
        context.setCurrentPar(currentPar);
        this.currentPar = currentPar;
        return currentPar;
    }


    @Override
    public Integer getAdvicePar() {
        return this.getCurrentPar();
    }

    @Override
    public Integer getMaxPar() {
        return MAX_PAR;
    }

    @Override
    public Integer getMinPar() {
        return MIN_PAR;
    }

    public Integer getTargetPar() throws Exception {
        Integer kafkaPartition = context.getKafkaPartion();
        if (kafkaPartition == null || kafkaPartition == 0) {
            kafkaPartition = getKafkaPartition();
        }
        log.info("kafkaPartition is {}", kafkaPartition);
        resultPar = advicePar;
        if (currentPar != 0 && advicePar != 0 && !currentPar.equals(advicePar)) {
            if (kafkaPartition != 0) {
                resultPar = advicePar > kafkaPartition ? kafkaPartition : advicePar;
            }
        }
        //最大最小判断
        if (resultPar != 0) {
            if (resultPar < getMinPar()) {
                resultPar = getMinPar();
            }
            if (resultPar > getMaxPar()) {
                resultPar = getMaxPar();
            }
        }
        return resultPar;
    }

    @Override
    public final void doStrategy() throws Exception {
        if (this.isColling) {
            return;
        }
        //获取当前并行度
        if (context.getCurrentPar() == 0) {
            this.getCurrentPar();
        }
        //获取建议并行度和最终并行度并且赋值给上下文
        advicePar = getAdvicePar();
        resultPar = getTargetPar();
        log.info("advicePar is {}", advicePar);
        log.info("resultPar is {}", resultPar);
    }


    @Override
    public void execute() {
        //伸缩
        taskInstanceServiceImpl.taskService.autoScaleTm(task.getId(), context.getTargetPar());
        //写入变化表
        insertChangeRecord();
        //告警
        warnByDingding();
        log.info("task id is {} scale par is  {}", task.getId(), context.getTargetPar());
    }

    /**
     * 将变化插入任务变化记录表
     *
     * @param
     */
    private void insertChangeRecord() {
        TaskParChange taskParChange = new TaskParChange();
        taskParChange.setTaskId(task.getId());
        taskParChange.setStrategyName(name);
        taskParChange.setOriginalPar(context.getCurrentPar());
        taskParChange.setUpdatePar(context.getTargetPar());
        taskInstanceServiceImpl.taskParChangeService.save(taskParChange);
        task.setRestartTime(new Timestamp(System.currentTimeMillis()));
        taskInstanceServiceImpl.taskService.update(task);
    }

    private void warnByDingding() {
        List<AbstractStrategy> strategyList = context.getStrategyChain().strategyList;
        if (context.getCurrentPar() == 0) {
            return;
        }
        boolean isWaring = false;
        String format = String.format("任务ID:{%s},当前并行度:%s,", task.getId(), context.getCurrentPar());
        StringBuilder stringBuilder = new StringBuilder(format);
        for (AbstractStrategy abstractStrategy : strategyList) {
            if (abstractStrategy.name.equals(TaskScaleStrategyConstant.OPERATOR_PROCESS_STRATEGY) &&
                    abstractStrategy.getResultPar() != 0) {
                isWaring = true;
                stringBuilder.append("算子操作策略建议并行度:")
                        .append(abstractStrategy.getResultPar())
                        .append(";");
            }
            if (abstractStrategy.name.equals(TaskScaleStrategyConstant.KAFKA_LAG_STRATEGY) &&
                    abstractStrategy.getResultPar() != 0) {
                isWaring = true;
                stringBuilder.append("Kafka延迟建议并行度:")
                        .append(abstractStrategy.getResultPar())
                        .append(";");
            }
            if (abstractStrategy.name.equals(TaskScaleStrategyConstant.PERIODIC_STRATEGY) &&
                    abstractStrategy.getResultPar() != 0) {
                isWaring = true;
                stringBuilder.append("周期性策略建议并行度:")
                        .append(abstractStrategy.getResultPar())
                        .append(";");
            }
        }
        //钉钉通知
        if (isWaring) {
            ArrayList<String> objects = new ArrayList<>();
            DingUtil.buildRequest(stringBuilder.toString(), DsTaskConstant.DINGDING_SECRET, DsTaskConstant.DINGDING_WEBHOOK, false, objects);
        }
    }


    /**
     * 获取kafka分区数
     *
     * @return
     */
    public Integer getKafkaPartition() {
        int partition = 0;
        if (context.getTopic() == null || StringUtils.isEmpty(context.getTopic())) {
            return partition;
        }
        String partitonWhere = String.format("count(sum(kafka_log_log_size{project='%s',topic='%s'}) by (partition))",
                strategy.getKafkaCluster(), context.getTopic());
        log.info("getKafkaPartition request hawkeye query key is :{}", partitonWhere);
        HawKeyeResult hawKeyeResult = getHawkeyeRes(partitonWhere);
        List<HawKeyeResult.Data.CoreResult> resultList = hawKeyeResult.getData().getResult();
        if (resultList != null && !resultList.isEmpty()) {
            String par = resultList.stream().findFirst().orElse(null).getValue()[1];
            partition = Integer.valueOf(par);
        }
        context.setKafkaPartion(partition);
        return partition;
    }


    /**
     * 获取kafka_broker
     *
     * @return
     */
    public Set<String> getKafkaBroker() {
        String brokerWhere = String.format("(kafka_brokers{project='%s'},instance)", strategy.getKafkaCluster());
        Set<String> brokerList = new HashSet<>();
        log.info("getKafkaBroker request hawkeye query key is :{}", brokerWhere);
        HawKeyeResult hawKeyeResult = getHawkeyeRes(brokerWhere);
        List<HawKeyeResult.Data.CoreResult> resultList = hawKeyeResult.getData().getResult();
        if (resultList != null && !resultList.isEmpty()) {
            resultList.stream().forEach(data -> {
                if (data.getMetric() != null && data.getMetric().getInstance() != null &&
                        StringUtils.isNotEmpty(data.getMetric().getInstance())) {
                    brokerList.add(data.getMetric().getInstance());
                }
            });
        }
        context.setBrokerSet(brokerList);
        return brokerList;
    }

    public HawKeyeResult getHawkeyeRes(String value) {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put(HawkeyeUtil.QUERY, value);
        return HawkeyeUtil.requestHawkeye(requestMap);
    }

}
