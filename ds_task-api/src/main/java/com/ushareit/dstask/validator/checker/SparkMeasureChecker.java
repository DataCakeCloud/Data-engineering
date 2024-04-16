package com.ushareit.dstask.validator.checker;


import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.ParamStrategy;
import com.ushareit.dstask.bean.SparkParamRestrict;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.SparkParamRestrictService;
import com.ushareit.dstask.validator.CheckFor;
import com.ushareit.dstask.validator.ItemChecker;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.impl.SparkAdvancedParamValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Component
@CheckFor(value = SparkAdvancedParamValidator.class, desc = "校验参数的类型/格式/单位", mandatory = true, order = 100)
public class SparkMeasureChecker implements ItemChecker {

    public static String MEASUREMENT = "([0-9]+)([A-Za-z]+)";

    public static String MUM_PATTERN = "([0-9]+)";

    @Resource
    public SparkParamRestrictService sparkParamRestrictService;

    public static String getMatcher(String key, String value, AtomicReference<Integer> ex, Set<String> exKey) {
        Pattern r = Pattern.compile(MEASUREMENT);
        Matcher matcher = r.matcher(value);

        Pattern numPattern = Pattern.compile(MUM_PATTERN);
        Matcher numMatcher = numPattern.matcher(value);
        if (!matcher.matches() && !numMatcher.matches()) {
            ex.getAndSet(ex.get() + 1);
            exKey.add(key);
            return "";
//            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：取值不规范");
        }

        if (matcher.matches()) {
            return matcher.group(2).trim();
        } else {
            return numMatcher.group(1).trim();
        }
    }

    @Override
    public void checkImpl(Task task, TaskContext context) {
        if (context.getTaskSparkParam() == null) {
            return;
        }
        Map<String, String> taskSparkParam = context.getTaskSparkParam();

        //单位格式校验
        List<SparkParamRestrict> allVauleCheck = sparkParamRestrictService.getAllVauleCheck();
        HashMap<String, SparkParamRestrict> collect = allVauleCheck.stream()
                .collect(Collectors.groupingBy(SparkParamRestrict::getName))
                .entrySet().stream()
                .collect(HashMap::new, (m, pair) -> m.put(pair.getKey().trim(), pair.getValue().stream().findFirst().get()), HashMap::putAll);

        AtomicReference<Integer> ex = new AtomicReference<>(0);
        Set<String> exKey = new HashSet<>();

        List<Map.Entry<String, String>> resCollect = taskSparkParam.entrySet().stream().map(data -> {
            String key = data.getKey().trim();
            String value = data.getValue().trim();
            SparkParamRestrict sparkParamRestrict = collect.get(key);
            if (sparkParamRestrict == null) {
                return data;
            }
            ParamStrategy paramStrategy = JSON.parseObject(sparkParamRestrict.getParamStrategy(), ParamStrategy.class);

            //参数的类型
            typeCheck(key, value, sparkParamRestrict.getType(), ex, exKey);

            //单位
            Boolean isIgnoreCase = paramStrategy.getIsIgnoreCase();
            String[] units = paramStrategy.getUnits();

            String type = sparkParamRestrict.getType();

            if (StringUtils.isEmpty(type) || !type.equalsIgnoreCase("string")) {
                return data;
            }

            unitCheck(key, value, units, isIgnoreCase, ex, exKey);
            return data;
        }).collect(Collectors.toList());

        context.setDbSparkParamRestrict(collect);

        if (ex.get() != 0) {
            StringBuilder exStr = new StringBuilder(" ");
            exKey.stream().map(data -> {
                exStr.append(data).append("，");
                return data;
            }).collect(Collectors.toList());
            exStr.deleteCharAt(exStr.length() - 1);
            exStr.append(" : 取值格式有误。");
            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), exStr.toString());
        }
    }

    /**
     * 类型校验
     */
    public void typeCheck(String key, String value, String type, AtomicReference<Integer> ex, Set<String> exKey) {
        if (StringUtils.isEmpty(type)) {
            return;
        }

        if (type.equals("int")) {
            if (!StringUtils.isNumeric(value)) {
                ex.getAndSet(ex.get() + 1);
                exKey.add(key);
//                throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：取值不规范");
            }
        }
        if (type.equals("string")) {
            return;
        }
        if (type.equals("boolean")) {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                ex.getAndSet(ex.get() + 1);
                exKey.add(key);
//                throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：类型校验失败");
            }
        }
    }

    /**
     * 单位校验
     */
    public void unitCheck(String key, String value, String[] units, Boolean isIgnoreCase, AtomicReference<Integer> ex, Set<String> exKey) {
        if (units == null || units.length < 1) {
            return;
        }
        String matcher = getMatcher(key, value, ex, exKey);

        //如果值是数字就返回
        if (StringUtils.isEmpty(matcher) || StringUtils.isNumeric(matcher)) {
            return;
        }

        int exption = 0;
        if (!isIgnoreCase && !ArrayUtil.containsIgnoreCase(units, matcher)) {
            exption++;
        } else if (isIgnoreCase && !ArrayUtil.contains(units, matcher)) {
            exption++;
        }

        if (exption > 0) {
            ex.getAndSet(ex.get() + 1);
            exKey.add(key);
//            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：单位格式校验失败");
        }
    }

//    public static void main(String[] args) {
//        String ss = "11111";
//        String TENANT_NAME_PATTERN = "([0-9]+)([A-Za-z]+)";
//        String match = getMatcher(ss, ss);
//        System.out.println(match);
////        System.out.println(ss.toLowerCase());
//
//    }

}
