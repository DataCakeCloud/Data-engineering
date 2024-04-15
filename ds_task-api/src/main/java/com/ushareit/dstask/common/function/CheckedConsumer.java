package com.ushareit.dstask.common.function;

/**
 * @author fengxiao
 * @date 2023/1/6
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    void accept(T t) throws Exception;

}
