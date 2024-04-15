package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class PostgreSourceAdapter extends JDBCSourceAdapter{
    private final static String URL_MODE = "jdbc:postgresql://{0}:{1}/{2}?{3}";

    private final static String DRIVER = "org.postgresql.Driver";

    public PostgreSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);
        desc.put("driver", DRIVER);
        desc.put("connection_check_timeout_sec", 100);
        desc.put("url", MessageFormat.format(URL_MODE,
                sourceJson.getString("host"),
                sourceJson.getString("port"),
                sourceJson.getString("database"),
                StringUtils.isNotEmpty(sourceJson.getString("jdbc_url_params")) ? sourceJson.getString("jdbc_url_params") : ""));
        desc.put("user", sourceJson.getString("username"));
        desc.put("password", sourceJson.getString("password"));

        if (StringUtils.isEmpty(runtimeConfig.getCreateTableSql())) {
            desc.put("query", assembleSql(table));
        } else {
            desc.put("query", runtimeConfig.getCreateTableSql());
        }

        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }
}
