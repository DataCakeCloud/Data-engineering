package com.ushareit.dstask.web.utils;


import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.shaded.guava18.com.google.common.collect.Maps;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author xuebotao
 * 2021-01-12
 * 钉钉报警消息发送
 */
@Slf4j
public class DingUtil {

    private static String signature = "HmacSHA256";

    /**
     * 给钉钉群发送消息方法
     *
     * @param content 消息内容
     */
    public static void sendMsg(String secret, String webhook, String content) {
        try {
            if (StringUtils.isBlank(secret) || StringUtils.isBlank(webhook)) {
                throw new Exception("secret and webhook is Can't be empty");
            }
            //获取系统时间戳
            long timestamp = Instant.now().toEpochMilli();
            //拼接
            String stringToSign = timestamp + "\n" + secret;
            //使用HmacSHA256算法计算签名
            Mac mac = Mac.getInstance(signature);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), signature));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            //进行Base64 encode 得到最后的sign，可以拼接进url里
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            //钉钉机器人地址（配置机器人的webhook）
            StringBuilder durl = new StringBuilder();
            durl.append(webhook).append("&timestamp=").append(timestamp).append("&sign=").append(sign);
            BaseResponse response = HttpUtil.postWithJson(durl.toString(), content);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("钉钉推送消息出现异常 {}", e);
        }
    }

    /**
     * @param content    内容
     * @param isAtAll    是否@所有人 如果写true mobileList失效
     * @param mobileList @人的手机号
     */
    public static void buildRequest(String content,
                                    String secret, String webhook,
                                    boolean isAtAll,
                                    List<String> mobileList,
                                    List<String> userIds) {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);
        atMap.put("atUserIds", userIds);
        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);
        String contens = JSON.toJSONString(reqMap);
        DingUtil.sendMsg(secret, webhook, contens);
    }

    /**
     * @author wuyan
     * @param content
     * @param secret
     * @param webhook
     * @param isAtAll
     * @param mobileList
     */
    public static void buildRequest(String content,
                                    String secret, String webhook,
                                    boolean isAtAll,
                                    List<String> mobileList) {
        buildRequest(content, secret, webhook, isAtAll, mobileList, new ArrayList<>());
    }

}
