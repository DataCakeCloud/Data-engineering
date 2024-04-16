package com.ushareit.dstask.third.airbyte.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
public enum StatusEnum {

    SUCCEEDED(String.valueOf("succeeded")), FAILED(String.valueOf("failed"));

    private String value;

    StatusEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

}
