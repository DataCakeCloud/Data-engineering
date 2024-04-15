package com.ushareit.engine.seatunnel.adapter.sink;

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
public class ClickhouseSinkAdapter extends AbstractSinkAdapter {

    public String SOURCE_TYPE = Clickhouse.name();

    public ClickhouseSinkAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sinkJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);

        desc.put("host", sinkJson.getString("host") + ":" + sinkJson.getString("port"));
        desc.put("database", sinkJson.getString("database"));
        desc.put("table", table.getTargetTable());
        desc.put("username", sinkJson.getString("username"));
        desc.put("password", sinkJson.getString("password"));
        desc.put("source_table_name", table.getTargetTable());

        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }
}
