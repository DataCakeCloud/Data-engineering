package com.ushareit.dstask.web.ddl;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Data
public class SqlDdl implements IDdl{


    public static Map<String, String> dataTypeMap = new HashMap();
    static {
        dataTypeMap.put("CHAR","STRING");
        dataTypeMap.put("VARCHAR","STRING");
        dataTypeMap.put("TINYBLOB","STRING");
        dataTypeMap.put("TINYTEXT","STRING");
        dataTypeMap.put("BLOB","STRING");
        dataTypeMap.put("TEXT","STRING");
        dataTypeMap.put("MEDIUMBLOB","STRING");
        dataTypeMap.put("MEDIUMTEXT","STRING");
        dataTypeMap.put("LONGBLOB","STRING");
        dataTypeMap.put("DATE","STRING");
        dataTypeMap.put("TIME","STRING");
        dataTypeMap.put("YEAR","STRING");

        dataTypeMap.put("INT","bigint");
        dataTypeMap.put("INT UNSIGNED","bigint");
        dataTypeMap.put("BIGINT UNSIGNED","decimal(20)");
        dataTypeMap.put("DATETIME","timestamp(3)");
        dataTypeMap.put("timestamp","timestamp(3)");
    }

    protected Table table;

    private final String DDL_DEMO =
            "CREATE TABLE IF NOT EXISTS %s (\n" +
                    "%s \n" +
                    ") %s WITH (\n" +
                    "%s \n" +
                    ");\n";

    public SqlDdl(Table table) {
        this.table = table;
    }

    @Override
    public boolean verify() throws Exception {
        return false;
    }

    public RuntimeConfig runtimeConfig;

    @Override
    public String getDdl(RuntimeConfig runtimeConfig) throws Exception {
        this.runtimeConfig = runtimeConfig;
        if (verify()){
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        String ddl = String.format(DDL_DEMO, getName(), getSchema(), getPartition(), getInfo());
        log.info("\n表：" + getName() + "组装的DDL为：" + ddl);
        return ddl;
    }

    @Override
    public String getName() throws Exception {
        String replace = table.getName().replace("-", "_");
        StringBuilder tableName = new StringBuilder();
        String[] split = replace.split("\\.");
        for (int i = 0; i < split.length; i++) {
            tableName.append("`").append(split[i]).append("`");
            if (i != split.length - 1) {
                tableName.append(".");
            }
        }
        return tableName.toString();
    }

    @Override
    public String getSchema() throws Exception {
        StringBuffer sb = new StringBuffer();
        List<Column> columns = table.getColumns();
        if (columns == null || columns.size() == 0) {
            return "";
        }
        columns.stream().forEach(column -> sb.append("   `" + column.getName() + "` " + transformDataType(column.getType() == null?column.getColumnType():column.getType()) + ",\n"));
        return sb.substring(0, sb.length() - 2);
    }

    public String getDisplaySqlSchema() {
        StringBuffer sb = new StringBuffer();
        List<Column> columns = table.getColumns();
        if (columns == null || columns.size() == 0) {
            return "";
        }

        columns.stream().forEach(column -> sb.append("   " + column.getName() + " " + transformDataType(column.getType() == null?column.getColumnType():column.getType()) + ",\n"));
        return sb.substring(0, sb.length() - 2);
    }

    public List<Column> getColumns(){
        return table.getColumns();
    }

    /**
     * Schema类型转换，不同数据源映射关系不同
     *
     * @param type
     * @return
     */
    protected String transformDataType(String type) {
        if (dataTypeMap.containsKey(type.toUpperCase())){
            return dataTypeMap.get(type.toUpperCase());
        } else {
            return type.toUpperCase();
        }
    }

    @Override
    public String getPartition() throws Exception {
        return "";
    }

    /**
     * 组装with属性，需根据数据类型重写
     * @return
     * @throws Exception
     */
    @Override
    public String getInfo() throws Exception {
        StringBuffer sb = new StringBuffer();
        if (table.getParameters() == null) {
            return "";
        }
        Map<String, String> map =  table.getParameters();
        for (Map.Entry<String, String> entry:map.entrySet()) {
            sb.append("'").append(entry.getKey()).append("' = '")
                    .append(entry.getValue()).append("',\n");
        }

        return sb.substring(0, sb.length() - 2);
    }

    public String getDisplayTableName()  throws Exception {
        return null;
    }

    public Object getDisplaySchema(Boolean isSql) {
        if (isSql) {
            return StringUtils.isEmpty(getDisplaySqlSchema()) ? null : getDisplaySqlSchema() + ",\n";
        }
        return table.getColumns();
    }
}
