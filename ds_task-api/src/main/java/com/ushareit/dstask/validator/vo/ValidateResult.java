package com.ushareit.dstask.validator.vo;

import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
@Data
public class ValidateResult {

    private String status;
    private Integer step;
    private String message;
    private List<ValidateItem> itemList = new ArrayList<>();

    public static ValidateResult ok() {
        ValidateResult validateResult = new ValidateResult();
        validateResult.status = ValidStatus.PASS.showName();
        return validateResult;
    }

    public static ValidateResult fail(ValidFor validFor, String message) {
        ValidateResult validateResult = new ValidateResult();
        validateResult.status = validFor.type().isMandatory() ? ValidStatus.FORBID.showName() : ValidStatus.WARN.showName();
        validateResult.message = message;
        validateResult.step = validFor.type().getStep();
        return validateResult;
    }

}
