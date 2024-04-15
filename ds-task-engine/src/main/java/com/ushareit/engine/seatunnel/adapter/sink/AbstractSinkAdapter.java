package com.ushareit.engine.seatunnel.adapter.sink;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.seatunnel.adapter.Adapter;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;

import java.text.MessageFormat;
import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */

public abstract class AbstractSinkAdapter implements Adapter {

    public String INSERT_SQL = "insert into {0}({1}) values({2})";

    public RuntimeConfig runtimeConfig;
    public JSONObject sinkJson;

    public AbstractSinkAdapter(RuntimeConfig runtimeConfig, JSONObject sinkJson) {
        this.runtimeConfig = runtimeConfig;
        this.sinkJson = sinkJson;
    }

    public Map<String, Object> adapter() {
        return adapter(runtimeConfig, sinkJson);
    }


    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sinkJson) {
        return null;
    }

    public String assembleSql(Table table) {
        if (table == null || table.getColumns() == null) {
            return "";
        }
        StringBuilder stringBuffer = new StringBuilder();
        StringBuilder station = new StringBuilder();
        for (Column column : table.getColumns()) {
            stringBuffer.append(column.getName()).append(",");
            station.append("?").append(",");
        }
        return MessageFormat.format(INSERT_SQL, table.getTargetTable(),
                stringBuffer.substring(0, stringBuffer.lastIndexOf(",")),
                station.substring(0, station.lastIndexOf(",")));
    }
}
