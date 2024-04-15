package com.ushareit.dstask.validator.impl;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.OUTPUT_DATASET)
public class OutputValidator implements Validator {
    
    @Override
    public void validateImpl(Task task, TaskContext context) {

    }
}
