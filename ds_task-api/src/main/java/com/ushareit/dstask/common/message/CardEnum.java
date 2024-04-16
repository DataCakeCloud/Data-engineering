package com.ushareit.dstask.common.message;

import org.apache.commons.lang3.StringUtils;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
public enum CardEnum {

    DEBUG,
    SHUTDOWN,
    RUN_INFO,
    STOP_INFO,
    STATUS_INFO,
    TASK_MESSAGE,
    SYSTEM_MESSAGE;

    public static CardEnum of(String type) {
        for (CardEnum card : CardEnum.values()) {
            if (StringUtils.equalsIgnoreCase(card.name(), type)) {
                return card;
            }
        }
        return null;
    }

}
