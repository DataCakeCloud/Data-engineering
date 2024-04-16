package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessUserRole;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
public interface AccessUserRoleService extends BaseService<AccessUserRole> {
    void addRole(Integer userId, String roleIds);

    void addUsers(Integer roleId, String userIds);

    List<Integer> selectByRoleId(Integer roleId);

    List<Integer> selectByUserId(Integer userId);

    void deleteByUserId(Integer userId);

    void removeUser(Integer roleId, Integer userId);
}
