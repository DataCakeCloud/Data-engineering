package com.ushareit.engine.param;

import lombok.Data;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class Column {
    private String columnType;
    private String name;
    private String data_type;
    private String columnName;
    private String columnComment;
    private String comment;
    private String securityLevel;
    private Integer index;

    private List<Function> funcs;

    @Data
    public static class Function{
        private String funcName;
        private List<String> funcParams;
    }
}
