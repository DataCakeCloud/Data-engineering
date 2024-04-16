package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessTenantRole;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AccessTenantRoleService extends BaseService<AccessTenantRole>{
    List<Integer> selectByTenantId(Integer tenantId);

    Integer selectByRoleId(Integer roleId);
}
