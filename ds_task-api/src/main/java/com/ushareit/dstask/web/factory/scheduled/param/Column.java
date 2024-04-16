package com.ushareit.dstask.web.factory.scheduled.param;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    @ApiModelProperty("目标表字段名")
    private String columnName;

    @ApiModelProperty("目标表字段类型")
    private String columnType;

    @ApiModelProperty("目标表字段注释")
    private String comment;

    private String value;

    @ApiModelProperty("字段转换函数集合")
    private List<Function> funcs;

    @ApiModelProperty("来源表字段名")
    private String name;

    @ApiModelProperty("来源表字段类型")
    private String dataType;

    @Data
    public static class Function{
        String funcName;
        List<String> funcParams;
    }

    public static List<Column> distinctColumns(List<Column> cols) {
        List<Column> distinctCols = cols.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getColumnName() + ";" + p.getName()))),
                ArrayList::new));
        return distinctCols;
    }

    @Override
    public boolean equals(Object o) {
        Column column = (Column) o;
        if (this.columnName.equals(column.getColumnName())) {
            return true;
        }
        return false;
    }

    public static List<Column> distinctColumnsByColumnName(List<Column> cols) {
        List<Column> distinctCols = cols.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getColumnName()))),
                ArrayList::new));
        return distinctCols;
    }
}
