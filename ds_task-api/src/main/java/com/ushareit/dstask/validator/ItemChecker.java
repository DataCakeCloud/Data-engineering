package com.ushareit.dstask.validator;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.validator.vo.ValidateItem;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
public interface ItemChecker {

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Validator.class);

    default ValidateItem check(Task task, TaskContext context) {
        CheckFor checkFor = this.getClass().getDeclaredAnnotation(CheckFor.class);
        if (checkFor == null) {
            throw new RuntimeException("实例未设置 CheckFor 注解");
        }

        try {
            checkImpl(task, context);
            return ValidateItem.ok(checkFor.desc());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ValidateItem.fail(checkFor, e.getMessage());
        }
    }

    /**
     * 校验子项的实现方法
     *
     * @param task    任务详情信息
     * @param context 任务上下文信息
     */
    void checkImpl(Task task, TaskContext context);
}
