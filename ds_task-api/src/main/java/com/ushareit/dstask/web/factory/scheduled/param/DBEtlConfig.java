package com.ushareit.dstask.web.factory.scheduled.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DBEtlConfig {
    private String connectionUrl;
    private String dbUser;
    private String dbPassword;
    private String sourceTable;
    private String targetTable;
    private String sql;
    private String dbType;
    private String location;
    private List<Column> columns;
    private List<Column> addColumns;
    private List<Column> orderByColumns;
    private String partitions;
    private String cluster;
    private String targetTablePart;
    private boolean existTargetTable = true;
    private String primaryKey;

}
