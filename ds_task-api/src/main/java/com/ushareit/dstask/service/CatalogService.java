package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Catalog;
import com.ushareit.dstask.bean.DynamicForm;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface CatalogService extends BaseService<Catalog>{
    /**
     * 获取模板动态表单
     *
     * @param code 字典码
     */
    List<Catalog> getConfigByCode(@Valid String code);
}