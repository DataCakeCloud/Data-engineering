package com.ushareit.dstask.constant;

import com.ushareit.dstask.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fengxiao
 * @date 2022/8/29
 */
@Getter
@AllArgsConstructor
public enum IsOpenEnum {

    OPEN(1),
    CLOSE(0);

    private int status;

    public static void validate(Integer status) {
        if (status == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "是否开启不能为空");
        }

        for (IsOpenEnum isOpenEnum : IsOpenEnum.values()) {
            if (isOpenEnum.status == status) {
                return;
            }
        }

        throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "是否开启取值不支持");
    }


}
