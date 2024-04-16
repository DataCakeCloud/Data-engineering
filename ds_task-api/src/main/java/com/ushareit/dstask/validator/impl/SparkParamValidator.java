package com.ushareit.dstask.validator.impl;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * 校验执行传参Args参数中的jinjia表达式是否正确(包括jinjia表达式中的格式、关键词)；
 * (1)注释用"--"
 * (2)换行用"\"
 * (3)jinja表达式
 *
 * @author fengxiao
 * @date 2023/2/2
 */
@Component
@ValidFor(type = ValidType.SPARK_PARAMS)
public class SparkParamValidator implements Validator {

    @Resource
    public SchedulerServiceImpl schedulerService;
    Pattern pattern = Pattern.compile("[0-9a-zA-Z]*");

    @Override
    public void validateImpl(Task task, TaskContext context) throws Exception {
        if (task.getTemplateCode().equals("SPARKJAR")) {
            String mainClassAgrs = task.getMainClassArgs();
            mainClassAgrs = schedulerService.parseJinja(mainClassAgrs);
            validateSymbols(mainClassAgrs);
        } else {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.NO_SPARKJAR_ARGS_CHECK.getMessage());
        }
    }

    private void validateSymbols(String arg) {
        String[] args = arg.trim().replaceAll(" ", "").split("\\\\\n");  // 以"\"换行，实际包含了"\n"
        for (int i = 0 ; i< args.length; i++) {
            String row = args[i];
            while (row.startsWith("\n")) {  // 过滤掉空行
                row = row.replaceFirst("\n", "");
            }
            if (row.contains("\n")) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.ARGS_LINE_BREAK_ERROR.getMessage());
            }
//            if (!pattern.matcher(row.charAt(0)+"").matches() && !row.startsWith("--")) {  // 注释没用--， 且非字母数字开始
//                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), BaseResponseCodeEnum.NOTE_ERROR.getMessage());
//            }
        }
    }

}
