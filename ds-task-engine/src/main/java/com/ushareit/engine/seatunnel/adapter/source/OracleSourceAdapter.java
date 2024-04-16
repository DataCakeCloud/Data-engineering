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
public class OracleSourceAdapter extends JDBCSourceAdapter {

    private final static String URL_MODE = "jdbc:oracle:thin:@{0}:{1}:{2}";
    private final static String RAC_URL_MODE = "jdbc:oracle:thin:@//{0}:{1}/{2}";

    public final static String DRIVER = "oracle.jdbc.driver.OracleDriver";

    public OracleSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);

        desc.put("driver", DRIVER);
        desc.put("connection_check_timeout_sec", 100);
//        desc.put("url", MessageFormat.format(URL_MODE,
//                sourceJson.getString("host"),
//                sourceJson.getString("port"),
//                sourceJson.getString("sid")));
        desc.put("url", sourceJson.getString("jdbc_url_params"));
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
