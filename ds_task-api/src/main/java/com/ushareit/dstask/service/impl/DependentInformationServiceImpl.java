package com.ushareit.dstask.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessRoleMapper;
import com.ushareit.dstask.mapper.DependentInformationMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2023/08/04
 */
@Slf4j
@Service
public class DependentInformationServiceImpl extends AbstractBaseServiceImpl<DependentInformation> implements DependentInformationService {

    @Resource
    private DependentInformationMapper dependentInformationMapper;


    @Override
    public CrudMapper<DependentInformation> getBaseMapper() {
        return dependentInformationMapper;
    }


}
