package com.ushareit.dstask.service.impl;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.OwnerApp;
import com.ushareit.dstask.mapper.OwnerAppMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.OwnerAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @auther tiyongshuai
 * @data 2024/3/28
 * @description
 */

@Slf4j
@Service
public class OwnerAppServiceImpl extends AbstractBaseServiceImpl<OwnerApp> implements OwnerAppService {

    @Autowired
    private OwnerAppMapper ownerAppMapper;

    @Override
    public CrudMapper<OwnerApp> getBaseMapper() {
        return ownerAppMapper;
    }


}
