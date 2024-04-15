package com.ushareit.engine.seatunnel.adapter.sink;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;

import java.util.Map;


/**
 * author: xuebtao
 * date: 2023-06-30
 */
public class MysqlSinkAdapter extends JDBCSinkdapter {


    public MysqlSinkAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        super(runtimeConfig, sourceJson);
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        return super.adapter(runtimeConfig, sourceJson);
    }
}
