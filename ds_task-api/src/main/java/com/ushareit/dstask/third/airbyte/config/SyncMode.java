
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum SyncMode {

    FULL_REFRESH("full_refresh"),
    INCREMENTAL("incremental");
    private final String value;
    private final static Map<String, SyncMode> CONSTANTS = new HashMap<String, SyncMode>();

    static {
        for (SyncMode c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private SyncMode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static SyncMode fromValue(String value) {
        SyncMode constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
