package com.ushareit.dstask.constant;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@AllArgsConstructor
public enum FeedbackStatusEnum {

    UN_SOLVED("未解决"),
    UN_ACCEPT("待接单"),
    ACCEPTED("处理中"),
    SOLVED("已完成"),
    SCORED("已打分");

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static FeedbackStatusEnum of(String status) {
        for (FeedbackStatusEnum statusEnum : FeedbackStatusEnum.values()) {
            if (StringUtils.equalsIgnoreCase(statusEnum.name(), status)) {
                return statusEnum;
            }
        }
        return null;
    }


}
