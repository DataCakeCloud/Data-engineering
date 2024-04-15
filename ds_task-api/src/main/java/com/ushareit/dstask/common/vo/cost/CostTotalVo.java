package com.ushareit.dstask.common.vo.cost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CostTotalVo {
    private int totalJob;//任务数
    private int totalOwner;//owner数
    private int dataCycle;//数据周期
    private Double totalJobNameQuantity;//任务总用量
    private Double averageDayJobNameQuantity;//平均任务日用量
    private Double totalCost;//任务总用量
    private Double averageDayCost;//平均任务日用量
}
