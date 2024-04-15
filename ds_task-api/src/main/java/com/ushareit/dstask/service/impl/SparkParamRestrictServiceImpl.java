package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.mapper.SparkParamRestrictMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author xuebotao
 * @date 2023-02-17
 */
@Slf4j
@Service
public class SparkParamRestrictServiceImpl extends AbstractBaseServiceImpl<SparkParamRestrict> implements SparkParamRestrictService {

    @Resource
    private SparkParamRestrictMapper sparkParamRestrictMapper;

    @Override
    public CrudMapper<SparkParamRestrict> getBaseMapper() {
        return sparkParamRestrictMapper;
    }

    @Override
    public List<SparkParamRestrict> getAllVauleCheck() {
        SparkParamRestrict build = SparkParamRestrict.builder().isValueCheck("0").build();
        build.setDeleteStatus(0);
        return getBaseMapper().select(build);
    }
}
