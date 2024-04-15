package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccumulateOnlineTask;
import com.ushareit.dstask.mapper.AccumulateOnlineTaskMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccumulateOnlineTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2022/4/20
 */
@Slf4j
@Service
public class AccumulateOnlineTaskServiceImpl extends AbstractBaseServiceImpl<AccumulateOnlineTask> implements AccumulateOnlineTaskService {
    @Resource
    private AccumulateOnlineTaskMapper accumulateOnlineTaskMapper;


    @Override
    public CrudMapper<AccumulateOnlineTask> getBaseMapper() {
        return accumulateOnlineTaskMapper;
    }
}
