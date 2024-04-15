package com.ushareit.engine.seatunnel.adapter.source;


import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.adapter.bean.IcebergConfig;

import java.util.HashMap;
import java.util.Map;

import static com.ushareit.engine.constant.SourceEnum.Iceberg;


/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class IcebergSourceAdapter extends AbstractSourceAdapter {

    private static final String SOURCE_TYPE = Iceberg.name();

    public IcebergSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();

        Table table = runtimeConfig.getCatalog().getTables().get(0);
        IcebergConfig catalog_config = sourceJson.getObject("catalog_config", IcebergConfig.class);

        desc.put("catalog_name", "seatunnel");
        desc.put("catalog_type", "hive");
        desc.put("uri", catalog_config.getHiveThriftUri());
        desc.put("warehouse", table.getLocation());
        desc.put("namespace", runtimeConfig.getCatalog().getSourceDb());
        desc.put("table", table.getSourceTable());
        desc.put("result_table_name", table.getSourceTable());

        desc.put("query", assembleSql(table));

        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }


}
