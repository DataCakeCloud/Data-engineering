package com.ushareit.dstask.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tianxu
 * @date 2023/12/27
 **/
public class Module {
    private Map<String, String> module;

    public Module() {
        module = new HashMap<>();
        module.put("ds_task", "数据开发");
        module.put("metadata", "元数据");
        module.put("pipeline", "调度");
        module.put("qe", "SQL查询");
        module.put("cluster-service", "云资源管理");
    }

    public String getValue(String key) {
        return module.get(key);
    }
}
