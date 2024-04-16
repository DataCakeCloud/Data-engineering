package com.ushareit.engine.seatunnel.adapter;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;

import java.util.Map;

public interface Adapter {
    static Map<String, String> adapter(RuntimeConfig runtimeConfig, JSONObject json){ return null;};
}
