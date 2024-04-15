package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessTenantRole;
import com.ushareit.dstask.mapper.AccessTenantRoleMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessTenantRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AccessTenantRoleServiceImpl extends AbstractBaseServiceImpl<AccessTenantRole> implements AccessTenantRoleService {
    @Resource
    private AccessTenantRoleMapper accessTenantRoleMapper;



    @Override
    public CrudMapper<AccessTenantRole> getBaseMapper() {
        return accessTenantRoleMapper;
    }

    @Override
    public List<Integer> selectByTenantId(Integer tenantId) {
        return accessTenantRoleMapper.selectByTenantId(tenantId);
    }

    @Override
    public Integer selectByRoleId(Integer roleId) {
        return accessTenantRoleMapper.selectByRoleId(roleId);
    }

}
