package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.DynamicForm;
import com.ushareit.dstask.mapper.DynamicFormMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.DynamicFormService;
import com.ushareit.dstask.service.SysDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-09 10:59
 **/
@Service
@Slf4j
public class DynamicFormServiceImpl extends AbstractBaseServiceImpl<DynamicForm> implements DynamicFormService {
    @Autowired
    private DynamicFormMapper dynamicFormMapper;

    @Autowired
    private SysDictService dictService;

    @Override
    public CrudMapper<DynamicForm> getBaseMapper() {
        return dynamicFormMapper;
    }


    @Override
    public List<DynamicForm> getConfigByCode(@Valid String code) {
        return dynamicFormMapper.select(DynamicForm.builder().componentCode(code).build());
    }
}
