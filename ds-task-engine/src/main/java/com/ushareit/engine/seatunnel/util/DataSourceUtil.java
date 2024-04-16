package com.ushareit.engine.seatunnel.util;


import com.alibaba.fastjson.JSON;
import com.ushareit.engine.Context;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

import static com.ushareit.engine.constant.SourceEnum.Console;

/**
 * aurthor:xuebotao
 * date: 2023-07-04
 */
@Slf4j
public class DataSourceUtil {
    public static String getSourceTableCommand(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        Context context = new Context();
        String toLowerCase = type.toLowerCase();
        String genuineType = toLowerCase.substring(0, 1).toUpperCase() + toLowerCase.substring(1);

        context.setRuntimeConfigStr(runtimeConfig);
        context.setSourceType(genuineType);
        context.setSourceConfigStr(sourceConfigStr);

        context.setSinkType(Console.toString());

        String jobInfo = SeaTunnelParser.getJobInfo(context);

        log.info("jobInfo is :" + jobInfo);
        return jobInfo;
    }


    /**
     * 获取table列表
     */
    public static List<Map<String, Object>> getTables(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        String jobInfo = getSourceTableCommand(sourceName, runtimeConfig, type, sourceConfigStr);
        List<Map<String, Object>> tables = SeaTunnelActionUtil.getTables(jobInfo);
        log.info("tables:" + JSON.toJSONString(tables));
        return tables;
    }

    /**
     * 获取schema
     */
    public static List<Map<String, String>> getSchema(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        long startTime = System.currentTimeMillis();
        String jobInfo = getSourceTableCommand(sourceName, runtimeConfig, type, sourceConfigStr);
        List<Map<String, String>> schema = SeaTunnelActionUtil.getSourceSchema(jobInfo);
        log.info("schema:" + JSON.toJSONString(schema.toString()));
        log.info("Key operation completed. Time taken: " + (System.currentTimeMillis() - startTime) + " ms");
        return schema;
    }

    /**
     * 获取sample
     */
    public static List<Map<String, String>> getSample(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        String jobInfo = getSourceTableCommand(sourceName, runtimeConfig, type, sourceConfigStr);
        List<Map<String, String>> sample = SeaTunnelActionUtil.getSourceSample(jobInfo);
        log.info("sample:" + JSON.toJSONString(sample.toString()));
        return sample;
    }

    /**
     * 校验连接性
     */
    public static Boolean checkConnection(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        String jobInfo = getSourceTableCommand(sourceName, runtimeConfig, type, sourceConfigStr);
        return SeaTunnelActionUtil.check(jobInfo);
    }

    /**
     * 创建DB表
     */
    public static Boolean createTable(String sourceName, String runtimeConfig, String type, String sourceConfigStr) {
        String jobInfo = getSourceTableCommand(sourceName, runtimeConfig, type, sourceConfigStr);
        return SeaTunnelActionUtil.createTable(jobInfo);
    }


}
