package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessGroup;

import java.util.List;
import java.util.Set;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AccessGroupService extends BaseService<AccessGroup> {
    void addUsers(Integer groupId, String userIds, String isLeader);

    void removeUser(Integer groupId, Integer userId);

    List<AccessGroup> getOrganizationStructure(Integer userId);

    List<AccessGroup> getChildrenGroup(AccessGroup accessGroup);

    List<AccessGroup> userTree(String userId);

    List<AccessGroup> selectByName(String name);

    List<AccessGroup> seletByParentIds(List<Integer> userIds,Integer type);

    List<AccessGroup> selectByUserIds(List<Integer> userIds);

    List<AccessGroup> getCurrentGroup(AccessGroup accessGroup);

    List<AccessGroup> getCurrentGroupUser(AccessGroup accessGroup);

    Set<String> getChildrenGroupUser(String userId);

    List<AccessGroup> getDeptFullPath(Integer groupId);

    List<AccessGroup> getParentGroupList(Integer tenantId, String userName);

    void taskCostInit();

    AccessGroup getRootGroup(Integer groupId);

    /**
     * 获取创建人为当前userId的数
     * @param userId
     * @return
     */
    List<AccessGroup> createByTree(String userId);

    List<AccessGroup> groupTree();

    /**
     * 根据用户组id 级联获取底下的用户
     * @param accessGroupIds
     * @return
     */
    Set<String> listUserIdsByGroupIds(List<Integer> accessGroupIds);

    /**
     * 级联获取用户的所有父节点
     * @param userId
     * @return
     */
    Set<Integer> listAllParentGroupByUserId(String userId);
}
