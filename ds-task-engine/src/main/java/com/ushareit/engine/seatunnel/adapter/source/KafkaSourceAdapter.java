package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;

import java.util.HashMap;
import java.util.Map;


public class KafkaSourceAdapter extends AbstractSourceAdapter{
    private static final String SOURCE_TYPE = "Kafka";
    private String sourceType;

    public KafkaSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson, String sourceType) {
        super(runtimeConfig, sourceJson);
        this.sourceType = sourceType;
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> desc = new HashMap();
        desc.put("bootstrap.servers",sourceJson.getString("bootstrap_servers"));
        desc.put("topic", sourceJson.getString("topic"));
        Map<String, Object> result = new HashMap();
        result.put(SOURCE_TYPE, desc);
        return result;
    }
}
