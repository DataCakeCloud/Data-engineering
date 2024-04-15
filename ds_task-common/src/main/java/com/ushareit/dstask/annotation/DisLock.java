package com.ushareit.dstask.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DisLock {

    /**
     * 锁的唯一标识
     */
    String key();

    /**
     * 锁失效时间默认5分钟
     * 单位/s
     */
    int expiredSeconds() default 300;

    /**
     * 执行完方法是否释放，默认释放
     * 方法执行很快时，可以配置方法执行完不释放
     *
     * @return
     */
    boolean isRelease() default true;

}
