package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
public interface AccessUserService extends BaseService<AccessUser> {
    void addUser(AccessUser accessUser);

    void editUser(AccessUser accessUser);

    void freeze(Integer id, Integer freeze);

    AccessUser checkExist(Object id);

    AccessUser checkExistAndFreeze(Object id);

    List<AccessUser> selectByIds(String ids);

    List<AccessUser> selectByRoleId(Integer roleId);

    List<AccessUser> selectByGroupId(Integer groupId);

    List<AccessUser> selectByGroupIds(List<Integer> groupIds);

    List<AccessUser> selectByTenantId(Integer tenantId);

    void addGroup(Integer userId, String groupIds);

    Boolean isRootUser(String userId);

    void sendCode(String tenantName, String email);

    Boolean checkLatestCode(String tenantName, String email, String code);

    void updatePassword(String tenantName, String email, String password);

    void resetPassword(Integer userId);

    AccessUser login(AccessUser accessUser);

    List<AccessUser> selectByNames(List<String> userNmes, Integer tenantId);

    AccessUser checkMFACode( AccessUser accessUser);

    void unbundlingMFA( AccessUser accessUser);

    AccessUser selectByNameTenant(Integer tenantId, String name);

    void superSave(AccessUser accessUser);

    void batchAddUser() throws Exception;

    List<AccessUser> selectByNames(List<String> userNmes);

    List<AccessUser> likeByName(String name);
}
