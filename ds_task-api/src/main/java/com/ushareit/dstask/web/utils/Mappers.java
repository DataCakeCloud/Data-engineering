package com.ushareit.dstask.web.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/12/28
 */
public class Mappers {

    /**
     * 将 collection 转换为 list
     *
     * @param collection 通用集合
     * @param mapper     转换函数
     * @param <O>        原始对象结构
     * @param <R>        返回的对象结构
     * @return 转换后的对象列表
     */
    public static <O, R> List<R> mapToList(Collection<O> collection, Function<O, R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

}
