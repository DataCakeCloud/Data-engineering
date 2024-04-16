package com.ushareit.dstask.third.airbyte.common.vo;

import com.ushareit.dstask.third.airbyte.common.enums.StatusEnum;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class CheckConnectionRead {

    private StatusEnum status;
    private String message;

    public CheckConnectionRead(boolean check, String message) {
        this.status = check ? StatusEnum.SUCCEEDED : StatusEnum.FAILED;
        this.message = message;
    }
}
