package com.ushareit.engine;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class kerberosExample {
    public static void main(String[] args) throws IOException {
        ClassLoader loader= FileUtil.class.getClassLoader();
        InputStream stream=loader.getResourceAsStream("kerberos.json");
        String text = IOUtils.toString(stream,"utf8");
        JSONObject jsonObject = JSON.parseObject(text);
        String grouppqawbct5 = jsonObject.getString("grouppqawbct5");
        JSONObject jsonObject1 = JSON.parseObject(grouppqawbct5);
        System.out.println(jsonObject1.getString("principal"));
        System.out.println(jsonObject1.getString("keytab"));
    }
}
