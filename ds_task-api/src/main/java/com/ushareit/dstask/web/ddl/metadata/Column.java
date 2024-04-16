package com.ushareit.dstask.web.ddl.metadata;

import com.ushareit.dstask.web.factory.flink.param.Func;
import lombok.Data;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class Column {
    private String owner;
    private String type;
    private String name;
    private String displayName;
    private Integer position;
    private String qualifiedName;
    private String description;
    private String comment;
    private String userDescription;
    private Integer size;

    private String columnType;
    private String data_type;
    private String columnComment;
    private List<Func> funcs;
    private String funcParams;
    private String columnName;
    private Boolean isPK = false;
}
