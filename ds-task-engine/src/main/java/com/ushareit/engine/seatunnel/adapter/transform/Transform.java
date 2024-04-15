package com.ushareit.engine.seatunnel.adapter.transform;


import com.ushareit.engine.Context;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.adapter.Adapter;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class Transform implements Adapter {

    public String SQL_MODE = "select {0} from {1} {2}";

    private Map sql = new HashMap<>();

    public Transform(Context context) {
        RuntimeConfig runtimeConfig = context.getRuntimeConfig();
        Table table = runtimeConfig.getCatalog().getTables().get(0);
        if (table == null || table.getColumns() == null) {
            return;
        }
        Map<String, Object> transform = new HashMap();
        transform.put("source_table_name", table.getSourceTable());
        transform.put("result_table_name", table.getTargetTable());
        transform.put("query", assembleSql(table));
        sql.put("sql", transform);
    }


    public Map getTransformSql() {
        return sql;
    }


    public String assembleSql(Table table) {
        StringBuilder stringBuffer = new StringBuilder();
        for (Column column : table.getColumns()) {
            stringBuffer.append(column.getName()).append(",");
        }

        String whereStr = "";
        if (StringUtils.isNotEmpty(table.getFilterStr())) {
            whereStr = " where " + table.getFilterStr();
        }
        return MessageFormat.format(SQL_MODE,
                stringBuffer.substring(0, stringBuffer.lastIndexOf(",")),
                table.getSourceTable(), whereStr);
    }


}
