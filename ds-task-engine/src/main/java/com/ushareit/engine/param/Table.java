package com.ushareit.engine.param;

import lombok.Data;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class Table {
    private String sourceTable;
    private String partitions;
    private String targetTable;//employee
    private String targetTablePart;
    private String primaryKey;

    private List<Column> columns;

    private List<Column> addColumns;
    private Boolean existTargetTable;

    private String filterStr;
    private String location;

    private String targetPartition;

    private String fileFormat;

    private String delimiter;
    private String compress;
    private String splitPk;

    private List<Column> SourceTableColumn;

    private Boolean autoCreateTable=false;
}
