package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SparkSubmitOptionEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.dstask.web.utils.ParseParamUtil;
import com.ushareit.engine.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <a href="https://shimo.im/sheets/B1Aw1bgjbvUMG6qm/MODOC">参数文档</a>
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.SPARK_ADVANCED_PARAMS)
public class SparkAdvancedParamValidator implements Validator {

    @Override
    public void validateImpl(Task task, TaskContext context) {
        //先确定要校验得参数  参数白名单呢？
        String runtimeConfigJson = task.getRuntimeConfig();
        RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(runtimeConfigJson);

        String batchParams =runtimeConfig.getAdvancedParameters().getBatchParams();
        //校验参数的合法性,不合法抛出错误
        if (batchParams != null && !batchParams.isEmpty()) {
            String replaceBatchParams = batchParams.replace("--", " --");

            //参数替换成固定格式
            String formatBatchParams = ParseParamUtil.formatParam(replaceBatchParams);
            Set<String> opt = ParseParamUtil.getOpt(formatBatchParams);
            AtomicReference<Integer> ex = new AtomicReference<>(0);
            Set<String> exKey = new HashSet<>();
            opt.stream().map(p -> {
                if (!SparkSubmitOptionEnum.isValid(p)) {
                    ex.getAndSet(ex.get() + 1);
                    exKey.add(p);
                }
                return p;
            });

            if (ex.get() != 0) {
                StringBuilder exStr = new StringBuilder(" ");
                exKey.stream().map(data -> {
                    exStr.append(data).append("，");
                    return data;
                }).collect(Collectors.toList());
                exStr.deleteCharAt(exStr.length() - 1);
                exStr.append(" : 参数格式有误。");
                throw new ServiceException(BaseResponseCodeEnum.SPARK_PARAM_CHECK_FAIL.name(), exStr.toString());
            }

            Map<String, String> sparkParamMap = ParseParamUtil.formatParamNew(formatBatchParams);
            // 转化成map 然后校验 参数十分在白名单 以及取值
            context.setTaskSparkParam(sparkParamMap);

        }
    }

//    public static void main(String[] args) {
//
//        //先确定要校验得参数  参数白名单呢？
//
//        String batchParams = "--jars asd asd --conf 1 asdsa s-1=1 --jars asdasd asdas asdasd sda--conf 1 1 1";
//        String replaceBatchParams = batchParams.replace("--", " --");
//
//        //校验参数的合法性,不合法抛出错误
//        if (replaceBatchParams != null && !replaceBatchParams.isEmpty()) {
//            StringBuilder illegelOpt = new StringBuilder("");
//
//            //参数替换成固定格式
//            String formatBatchParams = ParseParamUtil.formatParam(replaceBatchParams);
//            Set<String> opt = ParseParamUtil.getOpt(formatBatchParams);
//            opt.forEach(p -> {
//                if (!SparkSubmitOptionEnum.isValid(p)) {
//                    illegelOpt.append(p).append(";");
//                }
//            });
//            if (illegelOpt.length() > 0) {
//                throw new ServiceException(BaseResponseCodeEnum.TASK_ILLEGEL_PARAMS, "非法参数:" + illegelOpt);
//            }
//            Map<String, String> sparkParamMap = ParseParamUtil.formatParamNew(formatBatchParams);
//            for (String key : sparkParamMap.keySet()) {
//                System.out.println(key);
//                System.out.println(sparkParamMap.get(key));
//            }
//        }
//
//    }


}
