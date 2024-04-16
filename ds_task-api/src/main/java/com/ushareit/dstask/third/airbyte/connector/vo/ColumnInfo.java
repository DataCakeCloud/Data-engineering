package com.ushareit.dstask.third.airbyte.connector.vo;

import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/8/17
 */
@Data
public class ColumnInfo {

    private String name;
    private String type;
    private Integer length;
    private String comment;
    private Boolean isPK;

    public ColumnInfo(String name, String type, Integer length, String comment, Boolean isPK) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.comment = comment;
        this.isPK = isPK;
    }
}
