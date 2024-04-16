package com.ushareit.dstask.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFor {

    /**
     * 支持哪种类型的参数校验
     */
    ValidType type();

    /**
     * 是否可用
     *
     * @return true 可用，false 不可用
     */
    boolean enabled() default true;

}
