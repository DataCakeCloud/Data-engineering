package com.ushareit.dstask.constant;

import com.ushareit.dstask.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengxiao
 * @date 2022/11/16
 */
@Getter
@AllArgsConstructor
public enum TurnType {

    ON(1),
    OFF(0);

    private final int type;

    public static void validate(String code) {
        if (of(code) == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "不支持的上线/下线取值");
        }
    }

    public static TurnType of(String code) {
        for (TurnType type : TurnType.values()) {
            if (StringUtils.equalsIgnoreCase(type.name(), code)) {
                return type;
            }
        }
        return null;
    }

}
