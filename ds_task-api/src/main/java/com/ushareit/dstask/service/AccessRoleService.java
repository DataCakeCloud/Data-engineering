package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessRole;

import java.util.Collection;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AccessRoleService extends BaseService<AccessRole> {

    AccessRole checkExist(Object id);

    void updateMenus(Integer id, String menuIds);

    List<AccessRole> selectByUserName(String userName);

    List<AccessRole> selectByUserId(Integer userId);

    AccessRole getCommonRoleId(Integer id);

    /**
     * 获取租户的下角色的全部信息
     *
     * @param tenantName 租户
     * @param roleNames  角色名
     * @return 角色信息
     */
    List<AccessRole> getRoleList(String tenantName, Collection<String> roleNames);

    /**
     * 为租户初始化角色数据
     *
     * @param tenantName 租户名字
     * @param roleList   角色信息
     */
    void initRoles(String tenantName, Collection<AccessRole> roleList);
}
