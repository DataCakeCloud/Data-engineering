package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: wuyan
 * @create: 2020-05-10 10:42
 **/
public class UrlUtil {
    /**
     * 将一个URL路径字符串转成file路径字符串
     * 典型的URL和File路径的比较：
     * URL：file:/D:/my%20java/URL&FILE/%e5%9b%be%e7%89%87/tongji.jpg
     * File：D:/my java/URL&FILE/图片/tongji.jpg
     *
     * @param url
     * @return
     */
    public static String urlToAbsPathString(String url) {
        try {
            URL url2 = new URL(url);
            try {
                File file = new File(url2.toURI());
                String absolutePath = file.getAbsolutePath();
                return absolutePath;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Matcher getMatcher(String url, String pattern) {
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher matcher = r.matcher(url);
        if (!matcher.find()) {
            throw new ServiceException(BaseResponseCodeEnum.UNKOWN);
        }
        return matcher;
    }

    public static String[] getBucketFile(String url) {
        String[] result = new String[2];
        if (url.startsWith("s3://")) {
            url = url.replace("s3://", "");
            result = url.split("/", 2);
        } else if (url.startsWith("obs://")) {
            url = url.replace("obs://", "");
            result = url.split("/", 2);
        } else if (url.startsWith("gs://")) {
            url = url.replace("gs://", "");
            result = url.split("/", 2);
        } else if (url.startsWith("ks3://")) {
            url = url.replace("ks3://", "");
            result = url.split("/", 2);
        }
        return result;
    }

    public static String getStorageLocation(String location) {
        String[] split = location.split(File.separator);
        split = Arrays.copyOfRange(split, 3, split.length);
        return File.separator + String.join(File.separator, split);
    }

    public static String assembleUri(String uri) {
        String[] split = uri.split("/");
        int length = split.length;
        if (length > 2) {
            StringBuilder resultBuilder = new StringBuilder();
            for (int i = 2; i < split.length; i++) {
                resultBuilder.append("/").append(split[i]);
            }
            return resultBuilder.toString();
        } else {
            return uri;
        }
    }
}
