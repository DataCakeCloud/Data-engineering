package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessUserGroup;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
public interface AccessUserGroupService extends BaseService<AccessUserGroup> {

    List<Integer> selectByGroupId(Integer groupId);

    void deleteByGroupIdAndUserId(Integer groupId, Integer userId);

    /**
     * 获取用户的组关联信息
     *
     * @param userId 用户ID
     * @return 关联信息
     */
    List<AccessUserGroup> userGroupList(Integer userId);
}
