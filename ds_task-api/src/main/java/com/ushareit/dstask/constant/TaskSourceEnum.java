package com.ushareit.dstask.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fengxiao
 * @date 2022/12/8
 */
@Getter
@AllArgsConstructor
public enum TaskSourceEnum {

    TASK(0),
    WORKFLOW(1);

    private final int type;

}
