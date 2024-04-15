package com.ushareit.dstask.web.utils;


import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseParamUtil {
    public static String formatParam(String params) {
        Pattern delimiterPattern = Pattern.compile("\\\\|;");
        Matcher delimiterMatcher = delimiterPattern.matcher(params);
        //替换掉 (, \ ;) 三种特殊字符
        String undelimiterParam = delimiterMatcher.replaceAll("");
        Pattern equalPattern = Pattern.compile("\\s*=\\s*");
        Matcher equalMatcher = equalPattern.matcher(undelimiterParam);
        //修正一下等号左右两边有空格的情况
        //将连续多个空格格式化为一个空格
        return equalMatcher.replaceAll("=").replaceAll("\\s+", " ").replaceAll("\\u00A0", " ");
    }

    public static Set<String> getOpt(String params) {
        Set<String> optSet = new HashSet<>();
        Pattern paramPattern = Pattern.compile("((\\s+|^)-+[^=\\s]+)");
        Matcher paramMatcher = paramPattern.matcher(params);
        while (paramMatcher.find()) {
            optSet.add(paramMatcher.group(1).trim());
        }
        return optSet;
    }

    /**
     * 解析参数
     *
     * @param params
     * @return
     */
    public static Map<String, String> formatParamNew(String params) {
        Pattern delimiterPattern = Pattern.compile("\\\\|;");
        Matcher delimiterMatcher = delimiterPattern.matcher(params);
        //替换掉 (, \ ;) 三种特殊字符
        String undelimiterParam = delimiterMatcher.replaceAll("");
        Pattern equalPattern = Pattern.compile("\\s*=\\s*");
        Matcher equalMatcher = equalPattern.matcher(undelimiterParam);
        //修正一下等号左右两边有空格的情况
        //将连续多个空格格式化为一个空格
        String parm = equalMatcher.replaceAll("=").replaceAll("\\s+", " ").trim();
        String[] args = parm.split(" --");
        final Map<String, String> map = new HashMap<>(args.length / 2);
        int i = 0;

        AtomicReference<Integer> ex = new AtomicReference<>(0);
        List<String> exKey = new ArrayList<>();
        while (i < args.length) {
            final String key;
            String trim = args[i].trim();
            try {
                if (trim.startsWith("conf")) {
                    key = trim.substring(5);
                } else if (trim.startsWith("--conf")) {
                    key = trim.substring(7);
                } else if (trim.startsWith("--jars")) {
                    i++;
                    continue;
                } else if (trim.startsWith("jars")) {
                    i++;
                    continue;
                } else if (trim.startsWith("--")) {
                    key = trim.substring(2);
                } else if (trim.startsWith("-")) {
                    key = trim.substring(1);
                } else {
                    key = trim;
                }

                //如果不满足这个 就是参数格式不对 抛出异常
                if (key.contains("=")) {
                    String[] keys = key.split("=");
                    map.put(keys[0].trim(), keys[1].trim());
                } else {
                    String[] keys = key.split(" ");
//                    if (keys.length != 2) {
//                        throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + ": 参数格式有误");
//                    }
                    if (keys.length == 2) {
                        map.put(keys[0].trim(), keys[1].trim());
                    }
                }
            } catch (Exception e) {
                ex.getAndSet(ex.get() + 1);
                exKey.add(trim);
            }
            i++;
        }
        if (ex.get() != 0) {
            StringBuilder exStr = new StringBuilder(" ");
            exKey.stream().map(data -> {
                exStr.append(data).append("，");
                return data;
            }).collect(Collectors.toList());
            exStr.deleteCharAt(exStr.length() - 1);
            exStr.append(" : 格式有误。");
            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), exStr.toString());
        }
        return map;
    }
}
