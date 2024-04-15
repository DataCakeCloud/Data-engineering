package com.ushareit.dstask.validator;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.utils.SpringUtil;
import com.ushareit.dstask.validator.vo.ValidateItem;
import com.ushareit.dstask.validator.vo.ValidateResult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface Validator {

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Validator.class);

    /**
     * 校验子项
     */
    Map<Class<? extends Validator>, List<? extends ItemChecker>> ITEM_CHECKER_MAP = new ConcurrentHashMap<>();

    default ValidateResult validate(Task task) {
        ValidFor validFor = this.getClass().getDeclaredAnnotation(ValidFor.class);
        if (validFor == null) {
            throw new RuntimeException("校验未设置 ValidFor 注解");
        }

        try {
            TaskContext context = new TaskContext();
            validateImpl(task, context);

            List<ValidateItem> checkResultList = getSubCheckers().stream().map(checker -> checker.check(task, context))
                    .collect(Collectors.toList());
            ValidateResult validateResult = new ValidateResult()
                    .setStep(validFor.type().getStep()).setItemList(checkResultList);

            Optional<ValidateItem> forbidOptional = checkResultList.stream()
                    .filter(item -> ValidStatus.of(item.getItemStatus()) == ValidStatus.FORBID).findFirst();
            if (forbidOptional.isPresent()) {
                return validateResult.setStatus(ValidStatus.FORBID.showName());
            }

            Optional<ValidateItem> warnOptional = checkResultList.stream()
                    .filter(item -> ValidStatus.of(item.getItemStatus()) == ValidStatus.WARN).findFirst();
            if (warnOptional.isPresent()) {
                return validateResult.setStatus(validFor.type().isMandatory() ? ValidStatus.FORBID.showName() : ValidStatus.WARN.showName());
            }

            return ValidateResult.ok().setItemList(checkResultList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ValidateResult.fail(validFor, e.getMessage());
        }
    }

    default List<? extends ItemChecker> getSubCheckers() {
        return ITEM_CHECKER_MAP.computeIfAbsent(this.getClass(), key -> SpringUtil.getBeansMap(ItemChecker.class)
                .values().stream().filter(item -> {
                    CheckFor checkFor = item.getClass().getDeclaredAnnotation(CheckFor.class);
                    return checkFor != null && checkFor.value() != null && checkFor.value().isAssignableFrom(this.getClass());
                }).sorted(Comparator.comparingInt(item -> {
                    CheckFor checkFor = item.getClass().getDeclaredAnnotation(CheckFor.class);
                    return checkFor.order();
                })).collect(Collectors.toList()));
    }

    /**
     * 校验的实际实现方法
     *
     * @param task    任务详情信息
     * @param context 任务上下文信息
     */
    void validateImpl(Task task, TaskContext context) throws Exception;

}
