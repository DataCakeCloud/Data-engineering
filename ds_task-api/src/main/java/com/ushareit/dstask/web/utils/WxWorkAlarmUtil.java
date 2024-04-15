package com.ushareit.dstask.web.utils;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.WxMessageBean;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import io.prestosql.jdbc.$internal.okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信报警工具类
 *
 * @author xuebotao
 * date:2023-07-19
 */
@Slf4j
public class WxWorkAlarmUtil {


    private static void sendPost(String webworkUrl, String content) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(contentType, content);
        Request request =
                new Request.Builder().url(webworkUrl).post(body).addHeader("cache-control", "no-cache").build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            throw new ServiceException(BaseResponseCodeEnum.QIWEI_MESSAGE_SEND_FAIL);
        }
    }

    public static void sendMessage(List<String> nameList, String topic, String message, Map<String, String> config) {
        Set<String> nameSet =new HashSet<>(nameList);
        if (nameList.isEmpty()) {
            return;
        }
        String[] noticer = nameSet.toArray(new String[nameSet.size()]);
        WxMessageBean wxMessageBean = new WxMessageBean();
        Map<String, Object> map = new HashMap<>();
        map.put("content", topic + message);
        map.put("mentioned_list", noticer);
        wxMessageBean.setText(map);
        String content = JSONObject.toJSONString(wxMessageBean);
        sendPost(config.get("URL"), content);
    }
}
