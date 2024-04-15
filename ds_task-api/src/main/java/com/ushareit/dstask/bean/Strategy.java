package com.ushareit.dstask.bean;

import lombok.Data;
import java.util.List;


/**
 * @author xuebotao
 * @date 2022-01-05
 */
@Data
public class Strategy {

    private String name;

    //kafka project
    private String kafkaCluster;

    private String topic;

    private String consumerGroup;

    //如果是操作算子策略 建议并行度至少超过这个指标扩充  容忍度
    private double minimumPercentage;

    //如果是kafka策略 延时时长
    private Integer delayTime;

    //如果是kafka策略 最大堆积数量 扩容
    private Long maxDelayCount = 0L;

    //如果是kafka策略 最小堆积数量 缩容
    private Long minDelayCount = 0L;

    //如果是kafka策略 扩充比例
    private double expandProportion;

    public Integer coolingTime;

    public List<PeriodRule> periodicList;

    public Integer minPar;

    public Integer maxPar;

    @Data
    public class PeriodRule {
        public String startTime;
        public String endTime;
        public Integer par;

        public PeriodRule() {

        }

        public PeriodRule(String startTime, String endTime, Integer par) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.par = par;
        }
    }

}
