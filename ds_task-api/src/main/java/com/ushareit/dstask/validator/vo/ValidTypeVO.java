package com.ushareit.dstask.validator.vo;

import com.ushareit.dstask.validator.ValidType;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
@Data
public class ValidTypeVO {

    private String validType;
    private Integer step;
    private String description;

    public ValidTypeVO(ValidType validType) {
        this.validType = validType.name();
        this.step = validType.getStep();
        this.description = validType.getDesc();
    }
}
