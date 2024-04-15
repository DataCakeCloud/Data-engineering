package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccumulateUser;
import com.ushareit.dstask.mapper.AccumulateUserMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccumulateUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2022/4/20
 */
@Slf4j
@Service
public class AccumulateUserServiceImpl extends AbstractBaseServiceImpl<AccumulateUser> implements AccumulateUserService {
    @Resource
    private AccumulateUserMapper accumulateUserMapper;

    @Override
    public CrudMapper<AccumulateUser> getBaseMapper() {
        return accumulateUserMapper;
    }
}
