package com.ushareit.engine;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;

import java.util.HashMap;
import java.util.Map;

public class HoconUtil {
    public static void main(String[] args) {
       /* String hoconString = "# hardcoded value\n" +
                "        \"key1\" : \"value1\",\n" +
                "        # hardcoded value\n" +
                "        \"key2\" : \"value2\",\n" +
                "        # hardcoded value\n" +
                "        \"key3\" : \"value3\"";

        // 将HOCON字符串解析为Config对象
        Config config = ConfigFactory.parseString(hoconString);

        // 将Config对象转换为MyConfig对象
        MyConfig myConfig = new MyConfig();
        myConfig.setKey1(config.getString("key1"));
        myConfig.setKey2(config.getString("key2"));
        myConfig.setKey3(config.getString("key3"));

        // 打印转换后的对象
        System.out.println(myConfig.getKey1());
        System.out.println(myConfig.getKey2());
        System.out.println(myConfig.getKey3());

        // 创建一个Map来存储对象属性
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        configMap.put("key2", "value2");
        configMap.put("key3", "value3");

        // 将Map转换为ConfigObject
        ConfigObject configObject = ConfigFactory.parseMap(configMap).root();

        // 将ConfigObject转换为Config对象
        Config config1 = ConfigFactory.empty().withValue("root", configObject);

        // 定义渲染选项，去除最外层大括号和行尾逗号
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults()
                .setOriginComments(false)
                .setComments(false)
                .setFormatted(true)
                .setJson(false)
                .setOriginComments(false)
                .setFormatted(true);

        // 将Config对象转换为HOCON字符串
        String hoconString1 = config1.root().render(renderOptions);

        // 打印转换后的HOCON字符串
        System.out.println(hoconString1);
        */
        String jsonString = "{\n" +
                "    \"job\": {\n" +
                "        \"setting\": {\n" +
                "            \"speed\": {\n" +
                "                \"channel\":1\n" +
                "            },\n" +
                "            \"errorLimit\": {\n" +
                "                \"record\": 0,\n" +
                "                \"percentage\": 0.02\n" +
                "            }\n" +
                "        },\n" +
                "        \"content\": [\n" +
                "            {\n" +
                "                \"reader\": {\n" +
                "                    \"name\": \"streamreader\",\n" +
                "                    \"parameter\": {\n" +
                "                        \"column\" : [\n" +
                "                            {\n" +
                "                                \"value\": \"DataX\",\n" +
                "                                \"type\": \"string\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"value\": 19890604,\n" +
                "                                \"type\": \"long\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"value\": \"1989-06-04 00:00:00\",\n" +
                "                                \"type\": \"date\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"value\": true,\n" +
                "                                \"type\": \"bool\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"value\": \"test\",\n" +
                "                                \"type\": \"bytes\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"sliceRecordCount\": 100000\n" +
                "                    }\n" +
                "                },\n" +
                "                \"writer\": {\n" +
                "                    \"name\": \"streamwriter\",\n" +
                "                    \"parameter\": {\n" +
                "                        \"print\": false,\n" +
                "                        \"encoding\": \"UTF-8\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // 将 JSON 字符串转换为 Config 对象
        Config config = ConfigFactory.parseString(jsonString);

        // 定义渲染选项，去除最外层大括号和行尾逗号
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults()
                .setOriginComments(false)
                .setComments(false)
                .setFormatted(true)
                .setJson(false)
                .setOriginComments(false)
                .setFormatted(true);

        // 将 Config 对象转换为 HOCON 字符串
        String hoconString = config.root().render(renderOptions);

        // 打印转换后的 HOCON 字符串
        System.out.println(hoconString);

    }
}
class MyConfig {
    String key1;
    String key2;
    String key3;

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }
}