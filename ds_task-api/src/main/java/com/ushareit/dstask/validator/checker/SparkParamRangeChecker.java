package com.ushareit.dstask.validator.checker;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.ParamStrategy;
import com.ushareit.dstask.bean.SparkParamRestrict;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.CheckFor;
import com.ushareit.dstask.validator.ItemChecker;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.impl.SparkAdvancedParamValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
@Component
@CheckFor(value = SparkAdvancedParamValidator.class, desc = "校验 Spark 参数取值范围", mandatory = true, order = 200)
public class SparkParamRangeChecker implements ItemChecker {

    public static String MUM_PATTERN = "([0-9]+)";

    public static String MEASUREMENT = "([0-9]+)([A-Za-z]+)";

    public static void constantCheck(String key, String value, ParamStrategy paramStrategy, AtomicReference<Integer> ex, Set<String> exKey) {
        String[] fixedValue = paramStrategy.getFixedValue();
        Boolean isIgnoreCase = paramStrategy.getIsIgnoreCase();

        int exption = 0;
        if (!isIgnoreCase && !ArrayUtil.containsIgnoreCase(fixedValue, value)) {
            exption++;
        } else if (isIgnoreCase && !ArrayUtil.contains(fixedValue, value)) {
            exption++;
        }

        if (exption > 0) {
            ex.getAndSet(ex.get() + 1);
            exKey.add(key);
//            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：取值范围校验失败");
        }
    }

    public static Matcher getMatcher(String key, String value, String type, String[] units, AtomicReference<Integer> ex, Set<String> exKey) {
        Pattern r;
        if (type.equals("int") || (StringUtils.isNumeric(value) && units != null && units.length > 0)) {
            r = Pattern.compile(MUM_PATTERN);
        } else {
            r = Pattern.compile(MEASUREMENT);
        }
        Matcher matcher = r.matcher(value);
        if (!matcher.matches()) {
            ex.getAndSet(ex.get() + 1);
            exKey.add(key);
            return null;
//            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：取值不规范");
        }
        return matcher;
    }

    @Override
    public void checkImpl(Task task, TaskContext context) {
        //校验取值范围
        Map<String, String> taskSparkParam = context.getTaskSparkParam();
        HashMap<String, SparkParamRestrict> dbSparkParamRestrict = context.getDbSparkParamRestrict();
        if (taskSparkParam == null || dbSparkParamRestrict == null) {
            return;
        }

        AtomicReference<Integer> ex = new AtomicReference<>(0);
        Set<String> exKey = new HashSet<>();

        List<Map.Entry<String, String>> collect = taskSparkParam.entrySet().stream().map(data -> {
            String key = data.getKey().trim();
            String value = data.getValue().trim();
            SparkParamRestrict sparkParamRestrict = dbSparkParamRestrict.get(key);

            if (sparkParamRestrict == null) {
                return data;
            }
            ParamStrategy paramStrategy = JSON.parseObject(sparkParamRestrict.getParamStrategy(), ParamStrategy.class);

            String type = sparkParamRestrict.getType();
            if (StringUtils.isNotEmpty(type) && type.equals("boolean")) {
                return data;
            }
            String[] fixedValue = paramStrategy.getFixedValue();
            if (fixedValue != null && fixedValue.length > 0) {
                constantCheck(key, value, paramStrategy, ex, exKey);
                return data;
            }

            String[] units = paramStrategy.getUnits();

            Matcher matcher = getMatcher(key, value, type, units, ex, exKey);
            if (matcher == null) {
                return data;
            }
            String paramValue = matcher.group(1).trim();
            int parseInt = Integer.parseInt(paramValue);
            if (parseInt < paramStrategy.getValueRange().getMin() ||
                    parseInt > paramStrategy.getValueRange().getMax()) {
                ex.getAndSet(ex.get() + 1);
                exKey.add(key);
//                throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), key + "：取值范围校验失败");
            }

            return data;
        }).collect(Collectors.toList());

        if (ex.get() != 0) {
            StringBuilder exStr = new StringBuilder(" ");
            exKey.stream().map(data -> {
                exStr.append(data).append("，");
                return data;
            }).collect(Collectors.toList());
            exStr.deleteCharAt(exStr.length() - 1);
            exStr.append(" : 取值范围有误。");
            throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), exStr.toString());
        }
    }

//    public static void main(String[] args) throws Exception {
//        String ss = "1123121";
//        String TENANT_NAME_PATTERN = "([0-9]+)([A-Za-z]+)";
//
//        getMatcher(null, ss, null);
//        System.out.println(ss.toLowerCase());
//
//        String ss1 = "c2VsZWN0JTBBJTIwJTIwKiUwQWZyb20lMEElMjAlMjBpY2ViZXJnLmJkX2R3ZC5iZXlsYV9jbWRfZXZlbnRzJTBBd2hlcmUlMEElMjAlMjBzZXJ2ZXJfZGF0ZV95bWQlMjAlM0UlM0QlMjAnMjAyMTA3MDgnJTBBJTIwJTIwYW5kJTIwYXBwX3Rva2VuJTIwaW4lMjAoJ1dBVENISVQnKSUwQSUyMCUyMGFuZCUyMHB2ZV9jdXIlMjBsaWtlJTIwJyUyNSUyRnNlYXJjaF9zdGFydCUyRnN1Z19saXN0JTJGJTI1Jw==";
//
//        String secret = DigestUtils.md5DigestAsHex(ss1.getBytes());
//        String encrypt = EncryptUtil.encrypt(ss1, DsTaskConstant.METADATA_PASSWDKEY);
//
//        System.out.println(encrypt);
//        System.out.println(secret);
//    }


}
