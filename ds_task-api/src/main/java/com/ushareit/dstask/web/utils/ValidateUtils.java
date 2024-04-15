package com.ushareit.dstask.web.utils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
public class ValidateUtils {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static <T> void validate(T object) {
        if (object == null) {
            throw new NullPointerException();
        }

        Set<ConstraintViolation<T>> result = validator.validate(object);
        result.stream().findFirst().ifPresent(one -> {
            throw new RuntimeException(one.getMessage());
        });
    }

}
