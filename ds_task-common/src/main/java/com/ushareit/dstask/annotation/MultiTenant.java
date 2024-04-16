package com.ushareit.dstask.annotation;

import java.lang.annotation.*;

/**
 * 支持多租户
 *
 * @author fengxiao
 * @date 2022/12/27
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MultiTenant {

}
