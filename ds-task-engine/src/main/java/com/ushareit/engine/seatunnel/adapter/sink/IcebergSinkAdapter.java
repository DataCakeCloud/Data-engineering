package com.ushareit.engine.seatunnel.adapter.sink;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.adapter.Adapter;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.seatunnel.adapter.bean.IcebergConfig;
import com.ushareit.engine.seatunnel.adapter.bean.Schema;
import com.ushareit.engine.seatunnel.adapter.source.AbstractSourceAdapter;

import java.util.HashMap;
import java.util.Map;

import static com.ushareit.engine.constant.SourceEnum.Iceberg;


/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class IcebergSinkAdapter extends AbstractSourceAdapter {

    private static String SINk_TYPE = Iceberg.name();

    public IcebergSinkAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Catalog catalog = runtimeConfig.getCatalog();
        Table table = runtimeConfig.getCatalog().getTables().get(0);
        IcebergConfig catalog_config = sourceJson.getObject("catalog_config", IcebergConfig.class);

        desc.put("uri", catalog_config.getHiveThriftUri());
//        desc.put("warehouse", table.getLocation());
        desc.put("namespace", catalog.getTargetDb());
        desc.put("table", catalog.getTargetDb() + "." + table.getTargetTable());
        desc.put("saveMode", catalog.getSync_mode());
        desc.put("schema", new Schema(runtimeConfig));

        desc.put("source_table_name", table.getTargetTable());


//        desc.put("query", assembleSql(table));

        Map<String, Object> result = new HashMap();
        result.put(SINk_TYPE, desc);
        return result;
    }

}
