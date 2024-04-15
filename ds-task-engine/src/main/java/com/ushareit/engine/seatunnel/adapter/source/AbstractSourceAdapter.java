package com.ushareit.engine.seatunnel.adapter.source;

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

public abstract class AbstractSourceAdapter implements Adapter {

    public String SQL_MODE = "select {0} from {1}";

    public RuntimeConfig runtimeConfig;
    public JSONObject sourceJson;

    public AbstractSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        this.runtimeConfig = runtimeConfig;
        this.sourceJson = sourceJson;
    }

    public Map<String, Object> adapter() {
        return adapter(runtimeConfig, sourceJson);
    }


    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        return null;
    }

    public String assembleSql(Table table) {
        if (table == null) {
            return "";
        }
        if (table.getSourceTable().equals("empty")) {
            return "select 1";
        }
        if (table.getColumns() == null) {
            return MessageFormat.format(SQL_MODE, "*", table.getSourceTable());
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (Column column : table.getColumns()) {
            stringBuffer.append(column.getName()).append(",");
        }
        return MessageFormat.format(SQL_MODE, stringBuffer.substring(0, stringBuffer.lastIndexOf(",")), table.getSourceTable());
    }
}
