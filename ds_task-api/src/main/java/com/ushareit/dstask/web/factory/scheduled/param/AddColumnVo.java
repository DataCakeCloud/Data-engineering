package com.ushareit.dstask.web.factory.scheduled.param;

import lombok.Data;

import java.util.List;

@Data
public class AddColumnVo {
    private String catalog;
    private String dbName;
    private String name;
    List<ColumnDataGrade> colsGrade;
}
