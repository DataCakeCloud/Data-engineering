package com.ushareit.dstask.constant;

/**
 * @author xuebotao
 * @date 2021-12-13
 */
public enum DsIndicatorsEnum {

    PV("PV"),
    UV("UV"),
    NEW_TASK_COUNT("新增任务数"),
    ACC_TASK_COUNT("累计任务数"),
    ACC_USER_COUNT("累计用户数"),
    ACC_DEPARTURE_USER_COUNT("累计离职用户数"),
    NEW_ARTIFACT_COUNT("新增工件数"),
    ACC_ARTIFACT_COUNT("累计工件数"),
    INTERFACE_AGG_RESPONSE_TIME("接口平均响应时长"),
    FAULT_COUNT("故障数");


    private String describe;

    DsIndicatorsEnum(String describe) {
        this.describe = describe;
    }

    public String getDesc() {
        return this.describe;
    }

}
