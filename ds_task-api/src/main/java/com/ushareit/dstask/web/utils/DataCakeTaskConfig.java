package com.ushareit.dstask.web.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * 模板任务配置工具
 * author:xuebotao
 * date:2023-07-14
 */
public class DataCakeTaskConfig {

    public static final String NEW_ADVANCE_PARAM = "advancedParameters";

    public static String getStringConfigValue(String runtimeConfig, String key) {
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        String value = runtimeConfigObject.getString(key);
        if (value == null) {
            JSONObject advancedParameters = JSON.parseObject(runtimeConfigObject.getString(NEW_ADVANCE_PARAM));
            if (advancedParameters != null) {
                value = advancedParameters.getString(key);
            }
        }
        return value;
    }


    public static Integer getInterConfigValue(String runtimeConfig, String key) {
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        Integer value = runtimeConfigObject.getInteger(key);
        if (value == null) {
            JSONObject advancedParameters = JSON.parseObject(runtimeConfigObject.getString(NEW_ADVANCE_PARAM));
            if (advancedParameters != null) {
                value = advancedParameters.getInteger(key);
            }
        }
        return value;
    }

    public static Boolean getBooleanConfigValue(String runtimeConfig, String key) {
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        Boolean value = runtimeConfigObject.getBoolean(key);
        if (value == null) {
            JSONObject advancedParameters = JSON.parseObject(runtimeConfigObject.getString(NEW_ADVANCE_PARAM));
            if (advancedParameters != null) {
                value = advancedParameters.getBoolean(key);
            }
        }
        return value;
    }


    public static <T> T getObjectConfigValue(String runtimeConfig, String key, Class<T> clazz) {
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        T object = runtimeConfigObject.getObject(key, clazz);
        if (object == null) {
            JSONObject advancedParameters = JSON.parseObject(runtimeConfigObject.getString(NEW_ADVANCE_PARAM));
            if (advancedParameters != null) {
                object = advancedParameters.getObject(key, clazz);
            }
        }
        return object;
    }

    //全按照新的格式取值
    public static RuntimeConfig paseRuntimeConfig(String runtimeConfigJson) {
        RuntimeConfig runtimeConfig;
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);

        JSONObject advancedParameters = JSON.parseObject(runtimeConfigObject.getString(NEW_ADVANCE_PARAM));
        if (advancedParameters != null) {
            runtimeConfig = JSON.parseObject(runtimeConfigJson, RuntimeConfig.class);
            if (StringUtils.isNotEmpty(runtimeConfig.getSourceId()) && !runtimeConfig.getSourceType().equals("iceberg")) {

            }
            if (StringUtils.isNotEmpty(runtimeConfig.getDestinationId()) && !runtimeConfig.getSourceType().equals("iceberg")) {

            }
        } else {
            runtimeConfig = RuntimeConfig.convert(runtimeConfigJson);

        }
        return runtimeConfig;
    }


}
