package com.ushareit.dstask.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冻结状态
 *
 * @author fengxiao
 * @date 2022/12/27
 */
@Getter
@AllArgsConstructor
public enum FreezeStatus {

    /**
     * 活跃
     */
    ACTIVE(0),

    /**
     * 已冻结
     */
    FREEZE(1);

    private final int type;

}
