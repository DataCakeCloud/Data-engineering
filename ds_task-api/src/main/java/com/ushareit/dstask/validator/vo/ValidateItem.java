package com.ushareit.dstask.validator.vo;

import com.ushareit.dstask.validator.CheckFor;
import com.ushareit.dstask.validator.ValidStatus;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
@Data
public class ValidateItem {

    private String itemName;
    private String itemStatus;
    private String successMessage;
    private String errorMessage;

    public static ValidateItem ok(String itemName) {
        return ok(itemName, null);
    }

    public static ValidateItem ok(String itemName, String successMessage) {
        ValidateItem validateItem = new ValidateItem();
        validateItem.setItemName(itemName);
        validateItem.setItemStatus(ValidStatus.PASS.showName());
        validateItem.setSuccessMessage(successMessage);
        return validateItem;
    }

    public static ValidateItem fail(CheckFor checkfor, String errorMessage) {
        ValidateItem validateItem = new ValidateItem();
        validateItem.setItemName(checkfor.desc());
        validateItem.setItemStatus(checkfor.mandatory() ? ValidStatus.FORBID.showName() : ValidStatus.WARN.showName());
        validateItem.setErrorMessage(errorMessage);
        return validateItem;
    }
}
