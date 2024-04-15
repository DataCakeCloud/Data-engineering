package com.ushareit.engine;

import com.ushareit.engine.seatunnel.util.SeaTunnelActionUtil;

import java.util.List;
import java.util.Map;

public class SeaTunnelActionUtilExample {
    public static void main(String[] args)  {
        String jobInfo = "env {\n" +
                "    \"checkpoint.interval\"=10000\n" +
                "    \"execution.parallelism\"=1\n" +
                "    \"job.mode\"=BATCH\n" +
                "}\n" +
                "sink {\n" +
                "    Console {\n" +
                "        parallelism=1\n" +
                "    }\n" +
                "}\n" +
                "source {\n" +
                "    Jdbc {\n" +
                "        \"connection_check_timeout_sec\"=100\n" +
                "        driver=\"com.mysql.cj.jdbc.Driver\"\n" +
                "        password=\"123456\"\n" +
                "        query=\"select engine_key,id from engine\"\n" +
                "        url=\"jdbc:mysql://127.0.0.1:3306/query_editor?useUnicode=true&characterEncoding=UTF-8&useSSL=false\"\n" +
                "        user=\"query_editor\"\n" +
                "    }\n" +
                "}";
        Boolean checkResult = SeaTunnelActionUtil.check(jobInfo);
        System.out.println("checkResult:" + checkResult);

        List<Map<String, String>> schema = SeaTunnelActionUtil.getSourceSchema(jobInfo);
        System.out.println("schema:" + schema.toString());

        List<Map<String, String>> sample = SeaTunnelActionUtil.getSourceSample(jobInfo);
        System.out.println("sample:"+com.alibaba.fastjson.JSON.toJSONString(sample));

        List<Map<String, Object>> tables = SeaTunnelActionUtil.getTables(jobInfo);
        System.out.println("tables:"+com.alibaba.fastjson.JSON.toJSONString(tables));

    }
}
