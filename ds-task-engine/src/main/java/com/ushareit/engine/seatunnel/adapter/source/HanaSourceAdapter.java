package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class HanaSourceAdapter extends JDBCSourceAdapter{

    private final static String DRIVER = "com.sap.db.jdbc.Driver";
    public HanaSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);

        desc.put("driver", DRIVER);
        desc.put("connection_check_timeout_sec", 100);
        desc.put("url", sourceJson.getString("jdbc_url_params"));
        desc.put("user", sourceJson.getString("username"));
        desc.put("password", sourceJson.getString("password"));
        desc.put("result_table_name", table.getSourceTable());
        if (table.getSourceTable().equals("empty")) {
            desc.put("query",String.format("select * from %s.*",sourceJson.getString("database")));
        }else{
            desc.put("query",String.format("select * from %s",table.getSourceTable()));
        }


        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }


}
