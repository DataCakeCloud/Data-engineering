package com.ushareit.dstask.web.metadata.airbyte.streams;


import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xuebotao
 * @create: 2022-08-05
 */
@Data
public class MysqlStreams {

    public static Map<String, String> dataTypeMap = new HashMap();

    static {
        dataTypeMap.put("CHAR", "STRING");
        dataTypeMap.put("VARCHAR", "STRING");
        dataTypeMap.put("STRING", "VARCHAR");
        dataTypeMap.put("BIGINT", "NUMBER");
        dataTypeMap.put("NUMBER", "BIGINT");
    }

    public List<Table> streams;

    @Data
    public static class Table {
        public String name;
        public JsonSchema jsonSchema;

        @Data
        public static class JsonSchema {
            public String type;
            public Map<String, Map<String, String>> properties;
        }
    }

    public static String transformDataType(String type) {
        if (dataTypeMap.containsKey(type.toUpperCase())) {
            return dataTypeMap.get(type.toUpperCase());
        } else {
            return type.toUpperCase();
        }
    }



}
