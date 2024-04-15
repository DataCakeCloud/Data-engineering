package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.SysDict;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface SysDictService extends BaseService<SysDict> {

    /**
     * 通过字典码获取内容
     *
     * @param code 字典码
     */
    String getValueByCode(String code);

    SysDict getByCode(String code);

    /**
     * 通过字典码 父码获取 所有childre内容
     *
     * @param parentCode 字典码 父码
     */
    Map<String, String> getConfigByParentCode(String parentCode);

    String getConfigByParentCode(String parentCode, String key);

    /**
     * 获取模板列表
     */
    Map<String, List<SysDict>> getTemplateList();
}
