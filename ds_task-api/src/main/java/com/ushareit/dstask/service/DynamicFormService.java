package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.DynamicForm;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface DynamicFormService extends BaseService<DynamicForm>{
    /**
     * 获取模板动态表单
     *
     * @param code 字典码
     */
    List<DynamicForm> getConfigByCode(@Valid String code);
}