package com.ushareit.dstask.validator;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengxiao
 * @date 2023/3/4
 */
public enum ValidStatus {

    PASS,
    WARN,
    FORBID;

    public static ValidStatus of(String name) {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(BaseResponseCodeEnum.ENUM_TYPE_NOT_EXIST_ERROR.name(), "校验结果类别不能为空");
        }

        for (ValidStatus status : ValidStatus.values()) {
            if (StringUtils.equalsIgnoreCase(status.name(), name)) {
                return status;
            }
        }

        throw new ServiceException(BaseResponseCodeEnum.ENUM_TYPE_NOT_EXIST_ERROR.name(),
                String.format("校验结果类别 %s 不存在", name));
    }

    public String showName() {
        return this.name().toLowerCase();
    }


}
