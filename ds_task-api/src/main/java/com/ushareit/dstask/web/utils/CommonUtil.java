package com.ushareit.dstask.web.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2021/4/9
 */
public class CommonUtil {
    public static String printStackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace( new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    public static Boolean isNumber(String keyWord) {
        String regEx = "^[0-9]*[1-9][0-9]*$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(keyWord);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static Integer getIdFromMap(Map<String, String> paramMap, String keyName) {
        Integer id = null;
        String value = paramMap.computeIfPresent(keyName, (k, v) -> v);
        if (StringUtils.isNoneEmpty(value) && isNumber(value)) {
            id = Integer.parseInt(value);
            paramMap.remove(keyName);
        }
        return id;
    }


    public static boolean isEquals(List<Integer> list1, List<Integer> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }
        //Only one of them is null
        else if (list1 == null || list2 == null) {
            return false;
        } else if (list1.size() != list2.size()) {
            return false;
        }

        //copying to avoid rearranging original lists
        list1 = new ArrayList<Integer>(list1);
        list2 = new ArrayList<Integer>(list2);

        Collections.sort(list1);
        Collections.sort(list2);

        return list1.equals(list2);
    }


    /**
     * 差集(基于java8新特性)
     * 求List1中有的但是List2中没有的元素
     */
    public static List<Integer> diffList(List<Integer> list1, List<Integer> list2) {
        Map<Integer, Integer> tempMap = list2.parallelStream().collect(Collectors.toMap(Function.identity(), Function.identity(), (oldData, newData) -> newData));
        return list1.parallelStream().filter(str -> {
            return !tempMap.containsKey(str);
        }).collect(Collectors.toList());
    }

    public static String convertSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long tb = gb * 1024;

        if (size >= tb) {
            return String.format("%.1f TB", (float) size / tb);
        } else if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    public static String convertSize(double size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long tb = gb * 1024;

        if (size == 0.0) {
            return 0.0 + " KB";
        }

        if (size >= tb) {
            return String.format("%.1f TB", (float) size / tb);
        } else if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%f B", size);
        }
    }
}
