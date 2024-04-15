package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */

public class JDBCSourceAdapter extends AbstractSourceAdapter {
    public String SOURCE_TYPE = "Jdbc";

    public String URL_MODE = "jdbc:mysql://{0}:{1}/{2}?{3}";

//    public String DRIVER = "com.mysql.jdbc.Driver";
    public String DRIVER = "com.mysql.cj.jdbc.Driver";

    public JDBCSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    public Map<String, Object> adapter() {
        return adapter(runtimeConfig, sourceJson);
    }

    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);

        desc.put("driver", DRIVER);
        desc.put("connection_check_timeout_sec", 100);
        desc.put("url", MessageFormat.format(URL_MODE,
                sourceJson.getString("host"),
                sourceJson.getString("port"),
                sourceJson.getString("database"),
                sourceJson.getString("jdbc_url_params")));
        desc.put("user", sourceJson.getString("username"));
        desc.put("password", sourceJson.getString("password"));
        desc.put("result_table_name", table.getSourceTable());

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
