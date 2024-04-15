package com.ushareit.dstask.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckFor {

    /**
     * 父校验类
     */
    Class<? extends Validator> value();

    /**
     * 是否为强校验（阻塞式校验）
     */
    boolean mandatory() default false;

    /**
     * 子项校验描述
     *
     * @return 描述信息
     */
    String desc() default "";

    /**
     * 排序值，值越小越靠前
     *
     * @return 排序值
     */
    int order() default 0;

}
