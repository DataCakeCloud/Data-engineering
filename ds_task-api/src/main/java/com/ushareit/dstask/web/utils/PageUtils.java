package com.ushareit.dstask.web.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/11/25
 */
public class PageUtils {

    /**
     * 将原分页数据，转换为目标结构的分页数据
     *
     * @param mapper 对象转换函数
     * @param <IN>   原数据对象
     * @param <OUT>  目标数据对象
     * @return 转换后的分页数据
     */
    public static <IN, OUT> PageInfo<OUT> map(PageInfo<IN> originPage, Function<IN, OUT> mapper) {
        // 创建Page对象，实际上是一个ArrayList类型的集合
        Page<OUT> page = new Page<>(originPage.getPageNum(), originPage.getPageSize());
        page.setTotal(originPage.getTotal());
        page.addAll(originPage.getList().stream().map(mapper).collect(Collectors.toList()));

        return new PageInfo<>(page);
    }

    /**
     * 将原分页数据，转换为目标结构的列表数据
     *
     * @param mapper 对象转换函数
     * @param <IN>   原数据对象
     * @param <OUT>  目标数据对象
     * @return 转换后的列表数据
     */
    public static <IN, OUT> List<OUT> mapToList(PageInfo<IN> originPage, Function<IN, OUT> mapper) {
        return map(originPage, mapper).getList();
    }

}
