package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.SysDict;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.SysDictMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.SysDictService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class SysDictServiceImpl extends AbstractBaseServiceImpl<SysDict> implements SysDictService {

    @Resource
    private SysDictMapper SysDictMapper;

    @Override
    public CrudMapper<SysDict> getBaseMapper() {
        return SysDictMapper;
    }

    @Override
    public String getValueByCode(String code) {
        return SysDictMapper.selectOne(SysDict.builder().code(code).build()).getValue();
    }

    @Override
    public SysDict getByCode(String code) {
        return SysDictMapper.selectOne(SysDict.builder().code(code).build());
    }

    @Override
    public Map<String, String> getConfigByParentCode(String parentCode) {
        return SysDictMapper.select(SysDict.builder().parentCode(parentCode).build())
                .stream()
                .collect(Collectors.toMap(SysDict::getCode, SysDict::getValue));
    }


    @Override
    public String getConfigByParentCode(String parentCode, String key) {
        Map<String, String> configByParentCode = getConfigByParentCode(parentCode);
        String value = configByParentCode.get(key);
        if (StringUtils.isEmpty(value)) {
            throw new ServiceException(BaseResponseCodeEnum.TEMPLATE_DEP_NOT_FOUND);
        }
        return value;
    }

    @Override
    public Map<String, List<SysDict>> getTemplateList() {
        LinkedHashMap<String, List<SysDict>> resultMap = new LinkedHashMap<>(8);
//        List<SysDict> templateGroups = SysDictMapper.selectByParentCode(DsTaskConstant.TEMPLATE_TYPE);
//        for (SysDict templateGroup:templateGroups) {
//            resultMap.put(templateGroup.getCode(), SysDictMapper.selectByParentCode(templateGroup.getCode()));
//        }
        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        Example example = new Example(SysDict.class);
        example.or().andIsNotNull("parentCode").andEqualTo("status", 1);
        List<SysDict> allTemplates = SysDictMapper.selectByExample(example);

        Map<String, List<SysDict>> templateGroups = allTemplates.stream()
                .collect(Collectors.groupingBy(SysDict::getParentCode));

        List<SysDict> sortList = new ArrayList<>();
        for (SysDict templateGroup : templateGroups.get(DsTaskConstant.TEMPLATE_TYPE)) {
            SysDict build = SysDict.builder().code(templateGroup.getCode()).build();
            sortList.add(build);
            resultMap.put(templateGroup.getCode(), templateGroups.get(templateGroup.getCode()));
        }
        if (!sortList.isEmpty()) {
            resultMap.put("sort", sortList);
        }
        return resultMap;
    }


}
