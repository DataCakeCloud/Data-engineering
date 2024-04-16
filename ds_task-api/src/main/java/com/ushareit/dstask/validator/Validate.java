package com.ushareit.dstask.validator;

import com.ushareit.dstask.validator.vo.ValidateItem;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
@FunctionalInterface
public interface Validate<T> {

    ValidateItem apply(T t);

}
