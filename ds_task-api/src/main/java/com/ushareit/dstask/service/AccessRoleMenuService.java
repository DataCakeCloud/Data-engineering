package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessRole;
import com.ushareit.dstask.bean.AccessRoleMenu;

import java.util.Collection;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AccessRoleMenuService extends BaseService<AccessRoleMenu> {

    void deleteByRoleId(Integer roleId);

    List<AccessRoleMenu> selectByRoleId(Integer roleId);

    /**
     * 为租户初始化角色和菜单关系
     *
     * @param tenantName 租户信息
     * @param roleList   角色列表
     */
    void initRoleMenus(String tenantName, Collection<AccessRole> roleList);
}
