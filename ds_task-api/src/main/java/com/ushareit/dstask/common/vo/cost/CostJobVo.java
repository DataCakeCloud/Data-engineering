package com.ushareit.dstask.common.vo.cost;

import lombok.Data;

@Data
public class CostJobVo {

    private String jobId;
    private String departmentName;//部门名称
    private String puName;//
    private String jobName;
    private String owner;
    private String productName;
    private Double jobNameQuantity;//用量
}
