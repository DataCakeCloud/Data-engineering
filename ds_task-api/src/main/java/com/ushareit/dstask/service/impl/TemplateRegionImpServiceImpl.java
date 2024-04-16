package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.mapper.TemplateRegionImpMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.TemplateRegionImpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TemplateRegionImpServiceImpl extends AbstractBaseServiceImpl<TemplateRegionImp> implements TemplateRegionImpService {

    @Resource
    private TemplateRegionImpMapper templateRegionImpMapper;

    @Override
    public CrudMapper<TemplateRegionImp> getBaseMapper() {
        return templateRegionImpMapper;
    }
}
