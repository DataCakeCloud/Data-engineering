package com.ushareit.engine.seatunnel.adapter.sink;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.seatunnel.adapter.Adapter;
import com.ushareit.engine.param.RuntimeConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class ConsoleSinkAdapter implements Adapter {
    private final static String SINK_TYPE = "Console";
    public static Map adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map desc = new HashMap();
        desc.put("parallelism",1);

        Map result = new HashMap();
        result.put(SINK_TYPE,desc);
        return result;
    }
}
