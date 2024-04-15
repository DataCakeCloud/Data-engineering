package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MongoDbAdapter extends AbstractSourceAdapter{
    public String SOURCE_TYPE = "mongodb";

    public String URI = "mongodb://{0}:{1}@{2}/{3}?retryWrites=true&writeConcern=majority";
    public MongoDbAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {

        Map<String, Object> desc = new HashMap();
        Table table = runtimeConfig.getCatalog().getTables().get(0);
        String database = sourceJson.getString("database");
        desc.put("database", database);
        desc.put("collection", table.getSourceTable());
        String username = sourceJson.getString("user");
        String password = sourceJson.getString("password");
        JSONObject instanceType = sourceJson.getJSONObject("instance_type");
        String serverAddresses = instanceType.getString("server_addresses");
        desc.put("uri",MessageFormat.format(URI,username,password,serverAddresses,database));
        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }
}