package com.ushareit.engine.seatunnel.adapter.sink;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */

public class SqlServerSinkAdapter extends JDBCSinkdapter {
    private String URL_MODE = "jdbc:sqlserver://{0}:{1};databaseName={2};encrypt=true;trustServerCertificate=true";

    private String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public SqlServerSinkAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        desc.put("driver", DRIVER);
        desc.put("max_retries", 0);
        desc.put("url", MessageFormat.format(URL_MODE,
                sourceJson.getString("host"),
                sourceJson.getString("port"),
                sourceJson.getString("database")));
        desc.put("user", sourceJson.getString("username"));
        desc.put("password", sourceJson.getString("password"));

        Table table = runtimeConfig.getCatalog().getTables().get(0);
        desc.put("source_table_name", table.getTargetTable());

//        desc.put("query", assembleSql(table));

        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }
}
