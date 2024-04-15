package com.ushareit.dstask.validator.impl;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.*;
import com.ushareit.dstask.validator.vo.ValidTypeVO;
import com.ushareit.dstask.validator.vo.ValidateItem;
import com.ushareit.dstask.validator.vo.ValidateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
@Slf4j
@Service
public class ValidatorServiceImpl implements ValidatorService {

    @Resource
    private List<Validator> validatorList;
    private Map<ValidType, Validator> validatorMap;

    @PostConstruct
    public void init() {
        validatorMap = validatorList.stream().map(item -> {
                    ValidFor validFor = item.getClass().getDeclaredAnnotation(ValidFor.class);
                    return Pair.create(validFor, item);
                }).filter(pair -> pair.getKey() != null && pair.getKey().enabled())
                .collect(HashMap::new, (m, pair) -> m.put(pair.getKey().type(), pair.getValue()), HashMap::putAll);
    }

    @Override
    public List<ValidTypeVO> getValidList(TemplateEnum templateEnum) {
        List<ValidType> validTypeList = ValidType.TEMPLATE_VALIDATORS_MAP.get(templateEnum);
        if (CollectionUtils.isEmpty(validTypeList)) {
            return Collections.emptyList();
        }

        return validTypeList.stream()
                .filter(validatorMap::containsKey)
                .map(item -> validatorMap.get(item).getClass().getDeclaredAnnotation(ValidFor.class).type())
                .map(ValidTypeVO::new).collect(Collectors.toList());
    }

    @Override
    public ValidateResult validate(ValidType validType, Task task) {
        Validator validator = validatorMap.get(validType);
        if (validator == null) {
            throw new ServiceException(validType.getDesc(), validType.getDesc() + ": 不支持的校验类别");
        }

        return validator.validate(task);
    }

    @Override
    public void validTask(Task task) {
        ValidType.TEMPLATE_VALIDATORS_MAP.computeIfAbsent(TemplateEnum.of(task.getTemplateCode()),
                        key -> Arrays.asList(ValidType.BASE_PARAM_CHECK, ValidType.OUTPUT_DATASET)).stream()
                .filter(ValidType::isMandatory)
                .filter(validatorMap::containsKey)
                .forEach(item -> {
                    ValidateResult validateResult = validate(item, task);
                    if (ValidStatus.of(validateResult.getStatus()) == ValidStatus.FORBID) {
                        String message = validateResult.getMessage();
                        if (StringUtils.isBlank(message)) {
                            message = CollectionUtils.emptyIfNull(validateResult.getItemList()).stream()
                                    .filter(one -> StringUtils.isNotBlank(one.getErrorMessage()))
                                    .findFirst().map(ValidateItem::getErrorMessage).orElse(StringUtils.EMPTY);
                        }

                        throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), message);
                    }
                });
    }
}
