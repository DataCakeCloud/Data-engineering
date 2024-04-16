package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;


import java.util.HashMap;
import java.util.Map;

import static com.ushareit.engine.constant.SourceEnum.Clickhouse;



/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class ClickhouseSourceAdapter extends AbstractSourceAdapter {

    public String SOURCE_TYPE = Clickhouse.name();

    public ClickhouseSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);

        desc.put("host ", sourceJson.getString("host") + ":" + sourceJson.getString("port"));
        desc.put("database", sourceJson.getString("database"));
        desc.put("result_table_name", table.getSourceTable());
        desc.put("username ", sourceJson.getString("username"));
        desc.put("password", sourceJson.getString("password"));

        desc.put("sql", assembleSql(table));

        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }

}
