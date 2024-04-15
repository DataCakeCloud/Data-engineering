package com.ushareit.dstask.web.function;

/**
 * @author wuyan
 * @date 2022/9/20
 */
@FunctionalInterface
public interface HomeFunction<A, B, R> {
    R apply(A a, B b);
}
