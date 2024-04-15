package com.ushareit.dstask.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessGroupMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AccessGroupServiceImpl extends AbstractBaseServiceImpl<AccessGroup> implements AccessGroupService {
    @Resource
    private AccessGroupMapper accessGroupMapper;

    @Resource
    private AccessUserGroupService accessUserGroupService;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private TaskService taskService;

    @Resource
    private TaskVersionService taskVersionService;

    @Resource
    private CostAllocationService costAllocationService;


    @Override
    public CrudMapper<AccessGroup> getBaseMapper() {
        return accessGroupMapper;
    }

    @Override
    public Object save(AccessGroup accessGroup) {
        Integer tenantId = accessGroup.getTenantId();

        accessTenantService.checkExist(tenantId);
        accessGroup.setId(null);

        // 存入group表
        accessGroup.setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        if (accessGroup.getParentId() == null) {
            accessGroup.setHierarchy(1);
        } else {
            AccessGroup dbParentGroup = checkExist(accessGroup.getParentId());
            accessGroup.setHierarchy(dbParentGroup.getHierarchy() + 1);
        }

        super.save(accessGroup);

        return accessGroup;
    }


    /**
     * @param id 主键
     * @return
     */
    @Override
    public AccessGroup getById(Object id) {
        AccessGroup accessGroup = checkExist(Integer.parseInt(id.toString()));
        List<AccessUser> users = accessUserService.selectByGroupId(accessGroup.getId());
        accessGroup.setUsers(users);

        return accessGroup;
    }

    @Override
    public List<AccessGroup> listByExample(AccessGroup accessGroup) {
        return accessGroupMapper.listRootGroup();
    }

    @Override
    public PageInfo<AccessGroup> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        if (StringUtils.isEmpty(paramMap.get("name"))) {
            pageSize = Integer.MAX_VALUE;
            PageInfo<AccessGroup> pageInfo = getPageInfo(pageNum, pageSize, getOrganizationStructure(Integer.parseInt(paramMap.get("userId"))));
            return pageInfo;
        }

        AccessGroup build = AccessGroup.builder().tenantId(Integer.parseInt(paramMap.get("tenantId"))).build();
        build.setDeleteStatus(0);

        List<AccessGroup> allAccessGroup = accessGroupMapper.select(build);

        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));

        Page<AccessGroup> page = accessGroupMapper.listByMap(paramMap);

        List<AccessGroup> list = page.getResult();
        for (AccessGroup accessGroup : list) {
            if (accessGroup.getType().equals(1)) {
                if (accessGroup.getIsLeader().equals(0)) {
                    accessGroup.setIsLeaderFlag("是");
                } else {
                    accessGroup.setIsLeaderFlag("否");
                }
                accessGroup.setGroupId(accessGroup.getParentId());
                accessGroup.setHasChildren(false);
                continue;
            }
            Set<String> integer = getGroupUserSize(accessGroup, allUserList, allNotRootGroup, new HashSet<>());
            accessGroup.setUserSize(integer.size());
        }
//        addUserNum(list);
        PageInfo<AccessGroup> pageInfo = getPageInfo(pageNum, pageSize, list);
        return pageInfo;
    }

    @Override
    public void addUsers(Integer groupId, String userIds, String isLeader) {
        checkExist(groupId);

        //保留原来的关系表
        String[] split = userIds.split(",");
        String[] isLeaders = isLeader.split(",");
        List<Integer> userIdList = Arrays.asList(split).stream().map(id -> Integer.parseInt(id)).collect(Collectors.toList());
        List<Integer> isLeaderList = Arrays.asList(isLeaders).stream().map(data -> Integer.parseInt(data)).collect(Collectors.toList());
        Map<Integer, List<AccessUser>> userMap = accessUserService.selectByIds(userIds).stream().collect(Collectors.groupingBy(AccessUser::getId));

        Map<Integer, Integer> map = IntStream.range(0, userIdList.size())
                .collect(HashMap::new, (m, i) -> m.put(userIdList.get(i), isLeaderList.get(i)), (m, n) -> {
                });

        List<AccessGroup> accessGroupList = new ArrayList<>();
        for (Integer key : map.keySet()) {
            AccessGroup accessGroup = new AccessGroup();
            accessGroup.setIsLeader(map.get(key));
            accessGroup.setDeleteStatus(0);
            accessGroup.setParentId(groupId).setUserId(key).setType(1).setName(userMap.get(key).stream().findFirst().get().getName())
                    .setTenantId(userMap.get(key).stream().findFirst().get().getTenantId())
                    .setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateBy(InfTraceContextHolder.get().getUserName())
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            accessGroupList.add(accessGroup);
        }

        save(accessGroupList);

        List<AccessUserGroup> collect = userIdList.stream().map(userId -> {
            AccessUserGroup accessUserGroup = new AccessUserGroup(userId, groupId);
            accessUserGroup.setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateBy(InfTraceContextHolder.get().getUserName())
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return accessUserGroup;
        }).collect(Collectors.toList());
        accessUserGroupService.save(collect);
    }

    @Override
    public void removeUser(Integer groupId, Integer userId) {
        AccessGroup build = AccessGroup.builder().parentId(groupId)
                .userId(userId).type(1).build();
        build.setDeleteStatus(0);

        List<AccessGroup> deleteList = accessGroupMapper.select(build)
                .stream().map(data -> {
                    data.setDeleteStatus(1);
                    data.setUpdateBy(InfTraceContextHolder.get().getUserName());
                    return data;
                }).collect(Collectors.toList());
        if (!deleteList.isEmpty()) {
            update(deleteList);
        }

        accessUserGroupService.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public void update(@RequestBody @Valid AccessGroup accessGroup) {
        checkExist(accessGroup.getId());
        super.update(accessGroup);
    }


    @Override
    public void delete(Object id) {
        AccessGroup accessGroup = checkExist(id);

//        List<Integer> userList = accessUserGroupService.selectByGroupId(accessGroup.getId());
        accessGroup.setIds(Collections.singletonList(accessGroup.getId()));
        Map<Integer, List<AccessGroup>> collect = getCurrentGroupUser(accessGroup).stream()
                .collect(Collectors.groupingBy(AccessGroup::getId));
        AccessGroup ag = collect.get(accessGroup.getId()).stream().findFirst().get();
        if (!ag.getUserSet().isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DELETE_USER_GROUP_FAIL_EXIST_GROUP);
        }
        AccessGroup build = AccessGroup.builder().parentId(accessGroup.getId()).type(0).build();
        build.setDeleteStatus(0);
        List<AccessGroup> groupList = accessGroupMapper.select(build);
        if (!groupList.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DELETE_GROUP_FAIL_EXIST_GROUP);
        }
//        List<String> taskIds = taskService.selectByCostGroup(accessGroup.getName())
//                .stream().map(data -> data.getId().toString()).collect(Collectors.toList());
//
//        if (!taskIds.isEmpty()) {
//            throw new ServiceException(BaseResponseCodeEnum.DELETE_USER_GROUP_FAIL_EXIST_TASK, String.join(",", taskIds));
//        }

        accessGroup.setDeleteStatus(1)
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessGroup);
    }

    private void addUserNum(List<AccessGroup> accessGroups) {
        accessGroups.stream().forEach(accessGroup -> {
            Integer id = accessGroup.getId();
            int userNum = accessUserGroupService.selectByGroupId(id).size();
            accessGroup.setUserNum(userNum);
        });
    }

    private AccessGroup checkExist(Object id) {
        AccessGroup accessGroup = super.getById(id);
        if (accessGroup == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "group不存在");
        }

        if (accessGroup.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "group已删除");
        }
        return accessGroup;
    }


    @Override
    public List<AccessGroup> getOrganizationStructure(Integer userId) {
        AccessUser accessUser = accessUserService.checkExist(userId);

        Integer tenantId = accessUser.getTenantId();
        AccessGroup build = AccessGroup.builder().tenantId(tenantId).build();
        build.setDeleteStatus(0);

        List<AccessGroup> allAccessGroup = accessGroupMapper.select(build);

        List<AccessGroup> allRootAccessGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() == null).collect(Collectors.toList());

        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));


        for (AccessGroup accessGroup : allRootAccessGroup) {
            if (accessGroup.getType().equals(1)) {
                if (accessGroup.getIsLeader().equals(0)) {
                    accessGroup.setIsLeaderFlag("是");
                } else {
                    accessGroup.setIsLeaderFlag("否");
                }
                accessGroup.setGroupId(accessGroup.getParentId());
                accessGroup.setHasChildren(false);
                continue;
            }
            if (allNotRootGroup.get(accessGroup.getId()) != null && !allNotRootGroup.get(accessGroup.getId()).isEmpty()) {
                accessGroup.setIsHasChildrenDir(true);
            }
            Set<String> integer = getGroupUserSize(accessGroup, allUserList, allNotRootGroup, new HashSet<>());
            accessGroup.setUserSize(integer.size());
        }
        return allRootAccessGroup;
    }


    public Set<String> getGroupUserSize(AccessGroup accessGroup, List<AccessGroup> allUserList,
                                        Map<Integer, List<AccessGroup>> allNotRootGroup, Set<AccessGroup> resultGroup) {
        Set<Integer> allGroup = new HashSet<>();
        Set<String> allUserSet = new HashSet<>();
        allGroup.add(accessGroup.getId());
        resultGroup.add(accessGroup);
        findAllChildrenGroup(accessGroup, allGroup, allNotRootGroup, resultGroup);
        for (AccessGroup ag : allUserList) {
            if (allGroup.contains(ag.getParentId())) {
                allUserSet.add(ag.getName());
            }
        }
        return allUserSet;
    }


    public void findAllChildrenGroup(AccessGroup accessGroup, Set<Integer> allGroup,
                                     Map<Integer, List<AccessGroup>> allNotRootGroup,
                                     Set<AccessGroup> resultGroup) {
        List<AccessGroup> accessGroupList = allNotRootGroup.get(accessGroup.getId());
        if (accessGroupList == null) {
            return;
        }
        for (AccessGroup ag : accessGroupList) {
            allGroup.add(ag.getId());
            resultGroup.add(ag);
            findAllChildrenGroup(ag, allGroup, allNotRootGroup, resultGroup);
        }

    }


    @Override
    public List<AccessGroup> getChildrenGroup(AccessGroup accessGroup) {
        AccessGroup dbAccessGroup = checkExist(accessGroup.getId());
        AccessGroup build = AccessGroup.builder().parentId(dbAccessGroup.getId()).build();
        build.setDeleteStatus(0);
        List<AccessGroup> childrenGroup = accessGroupMapper.select(build);

        Integer tenantId = dbAccessGroup.getTenantId();
        AccessGroup tenantBuild = AccessGroup.builder().tenantId(tenantId).build();
        tenantBuild.setDeleteStatus(0);

        List<AccessGroup> allAccessGroup = accessGroupMapper.select(tenantBuild);

        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));

        for (AccessGroup ag : childrenGroup) {
            if (ag.getType().equals(1)) {
                if (ag.getIsLeader().equals(0)) {
                    ag.setIsLeaderFlag("是");
                } else {
                    ag.setIsLeaderFlag("否");
                }
                ag.setGroupId(ag.getParentId());
                ag.setHasChildren(false);
                continue;
            }
            if (allNotRootGroup.get(ag.getId()) != null && !allNotRootGroup.get(accessGroup.getId()).isEmpty()) {
                ag.setIsHasChildrenDir(true);
            }
            Set<String> integer = getGroupUserSize(ag, allUserList, allNotRootGroup, new HashSet<>());
            ag.setUserSize(integer.size());
        }
        return childrenGroup;
    }

    public List<AccessGroup> createByTree(String userId) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        if (tenantId == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        AccessGroup group = AccessGroup.builder().tenantId(tenantId).type(0).build();
        //group.setCreateBy("xuebotao");
        group.setCreateBy(InfTraceContextHolder.get().getUserName());
        group.setDeleteStatus(DeleteEntity.NOT_DELETE);
        List<AccessGroup> userGroup = accessGroupMapper.select(group);
        if (userGroup.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, List<AccessGroup>> groupByParentId = userGroup.stream().filter(accessGroupEntity ->
                accessGroupEntity.getParentId() != null
        ).collect(Collectors.groupingBy(AccessGroup::getParentId));
        Map<Integer, List<AccessGroup>> groupByHy = userGroup.stream().collect(Collectors.groupingBy(AccessGroup::getHierarchy));
        return buildGroupTreeVo(groupByParentId, groupByHy, userGroup);
    }

    @Override
    public List<AccessGroup> groupTree() {
        AccessGroup group = AccessGroup.builder().type(0).build();
        group.setDeleteStatus(DeleteEntity.NOT_DELETE);
        List<AccessGroup> userGroup = accessGroupMapper.select(group);
        if (userGroup.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, List<AccessGroup>> groupByParentId = userGroup.stream().filter(accessGroupEntity ->
                accessGroupEntity.getParentId() != null
        ).collect(Collectors.groupingBy(AccessGroup::getParentId));
        Map<Integer, List<AccessGroup>> groupByHy = userGroup.stream().collect(Collectors.groupingBy(AccessGroup::getHierarchy));
        return buildGroupTreeVo(groupByParentId, groupByHy, userGroup);
    }

    public List<AccessGroup> buildGroupTreeVo(Map<Integer, List<AccessGroup>> parent, Map<Integer, List<AccessGroup>> leveMap, List<AccessGroup> accessGroupList) {
        List<AccessGroup> accessGroups = Lists.newArrayList();
        for (AccessGroup accessGroup : accessGroupList) {
            if (CollectionUtils.isNotEmpty(parent.get(accessGroup.getId()))) {
                accessGroup.setChildren(parent.get(accessGroup.getId()));
            }
        }
        leveMap.get(1).forEach(accessGroupEntity -> {
            accessGroups.add(accessGroupEntity);
        });
        return accessGroups;
    }

    /**
     * @return
     */
    public Set<String> listUserIdsByGroupIds(List<Integer> accessGroupIds) {
        List<AccessGroup> userGroup = accessGroupMapper.selectAll();
        if (userGroup.isEmpty()) {
            return Sets.newHashSet();
        }
        Map<Integer, List<AccessGroup>> groupByParentId = userGroup.stream().filter(accessGroupEntity ->
                accessGroupEntity.getParentId() != null
        ).collect(Collectors.groupingBy(AccessGroup::getParentId));
        for (AccessGroup accessGroup : userGroup) {
            if (CollectionUtils.isNotEmpty(groupByParentId.get(accessGroup.getId()))) {
                accessGroup.setChildren(groupByParentId.get(accessGroup.getId()));
            }
        }
        Set<String> userIds = Sets.newHashSet();
        for (Integer groupId : accessGroupIds) {
            findAllChildren(groupByParentId, groupId, userIds);
        }
        return userIds;
    }

    @Override
    public Set<Integer> listAllParentGroupByUserId(String userId) {
        List<AccessGroup> userGroup = accessGroupMapper.selectAll();
        Set<Integer> set=Sets.newHashSet();
        Integer parentId=null;
        if (CollectionUtils.isNotEmpty(userGroup)){
            for (AccessGroup accessGroup:userGroup){
                if (accessGroup.getName().equals(userId)){
                    parentId=accessGroup.getParentId();
                    set.add(accessGroup.getParentId());
                }
            }
        }
        if (parentId!=null){
            for (;;){
                boolean stop=false;
                for (AccessGroup accessGroup:userGroup){
                    if (accessGroup.getId().equals(parentId)){
                        if (accessGroup.getParentId()==null||accessGroup.getParentId().equals(accessGroup.getId())){
                            stop=true;
                        }else {
                            set.add(accessGroup.getParentId());
                            parentId=accessGroup.getParentId();
                        }
                    }
                }
                if (stop){
                    break;
                }
            }
        }
        return set;
    }

    private void findAllChildren(Map<Integer, List<AccessGroup>> parents, Integer groupId, Set<String> userIds) {
        List<AccessGroup> childrens = parents.get(groupId);
        if (CollectionUtils.isNotEmpty(childrens)) {
            for (AccessGroup accessGroup : childrens) {
                if (accessGroup.getType() == 1) {
                    userIds.add(accessGroup.getName());
                } else {
                    if (CollectionUtils.isNotEmpty(accessGroup.getChildren())) {
                        findAllChildren(parents, accessGroup.getId(), userIds);
                    }
                }
            }
        }
    }


    @Override
    public List<AccessGroup> userTree(String userId) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        if (tenantId == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        AccessUser accessUserBuild = AccessUser.builder().name(userId).tenantId(tenantId).build();
        accessUserBuild.setDeleteStatus(0);
        AccessUser accessUser = accessUserService.selectOne(accessUserBuild);

        //check user in group
        AccessGroup user = AccessGroup.builder().tenantId(accessUser.getTenantId())
                .type(1).userId(accessUser.getId()).build();
        user.setDeleteStatus(0);

        List<AccessGroup> userGroup = accessGroupMapper.select(user);
        if (userGroup.isEmpty()) {
            return new ArrayList<>();
        }

        AccessGroup accessGroup = AccessGroup.builder().tenantId(accessUser.getTenantId()).type(0).build();
        accessGroup.setDeleteStatus(0);

        Map<Integer, List<AccessGroup>> groupMap = accessGroupMapper.select(accessGroup)
                .stream().collect(Collectors.groupingBy(AccessGroup::getId));

        Set<AccessGroup> resultGroup = new HashSet<>();
        Set<Integer> groupIdSet = new HashSet<>();


        AccessGroup tenantBuild = AccessGroup.builder().tenantId(tenantId).build();
        tenantBuild.setDeleteStatus(0);
        List<AccessGroup> allAccessGroup = accessGroupMapper.select(tenantBuild);


        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));


        //如果自己是leader 可以同级别的所有子组
        for (AccessGroup ag : userGroup) {
            findAllUserParentGroup(resultGroup, groupIdSet, groupMap, ag);
            List<AccessGroup> groupList = allNotRootGroup.get(ag.getParentId());
            if (ag.getIsLeader() == 0 && groupList != null) {
                for (AccessGroup accessGroup1 : groupList) {
                    getGroupUserSize(accessGroup1, allUserList, allNotRootGroup, resultGroup);
                }
            }
        }

        List<Integer> userInGroup = resultGroup.stream().map(BaseEntity::getId)
                .distinct().collect(Collectors.toList());

        List<AccessGroup> allInGroupUserList = accessGroupMapper.seletByParentIds(userInGroup, 1);


        //user add group
        for (AccessGroup group : resultGroup) {
            for (AccessGroup aUser : allInGroupUserList) {
                if (aUser.getParentId().equals(group.getId())) {
                    List<AccessGroup> childrenGroups = group.getChildren();
                    childrenGroups.add(aUser);
                }
            }
        }

        List<AccessGroup> treeGroup = new ArrayList<>();
        // group tree build
        for (AccessGroup parentGroup : resultGroup) {
            if (parentGroup.getParentId() == null) {
                treeGroup.add(parentGroup);
            }
            for (AccessGroup childrenGroup : resultGroup) {
                if (!parentGroup.getId().equals(childrenGroup.getId()) && parentGroup.getId().equals(childrenGroup.getParentId())) {
                    List<AccessGroup> childrenGroups = parentGroup.getChildren();
                    childrenGroups.add(childrenGroup);
                }
            }
        }

        return treeGroup;
    }


    @Override
    public List<AccessGroup> selectByName(String name) {
        AccessGroup build = AccessGroup.builder().name(name).type(1).build();
        build.setDeleteStatus(0);
        return getBaseMapper().select(build);
    }

    @Override
    public List<AccessGroup> seletByParentIds(List<Integer> userIds, Integer type) {
        return accessGroupMapper.seletByParentIds(userIds, type);
    }


    @Override
    public List<AccessGroup> selectByUserIds(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return new ArrayList<>();
//            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR, "当前用户不存在组织关系");
        }
        return accessGroupMapper.selectByUserIds(userIds);
    }

    @Override
    public List<AccessGroup> getCurrentGroup(AccessGroup accessGroup) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        if (tenantId == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        AccessUser accessUserBuild = AccessUser.builder().name(accessGroup.getName()).tenantId(tenantId).build();
        accessUserBuild.setDeleteStatus(0);
        AccessUser accessUser = accessUserService.selectOne(accessUserBuild);

        if (accessUser == null) {
            return new ArrayList<>();
        }
        AccessGroup build = AccessGroup.builder().userId(accessUser.getId()).type(1).tenantId(tenantId).build();
        build.setDeleteStatus(0);

        return getBaseMapper().select(build);
    }


    @Override
    public List<AccessGroup> getCurrentGroupUser(AccessGroup accessGroup) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        if (tenantId == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        if (accessGroup.getIds().isEmpty()) {
            return new ArrayList<>();
        }
        List<AccessGroup> accessGroupList = listByIds(accessGroup.getIds());

        AccessGroup tenantBuild = AccessGroup.builder().tenantId(tenantId).build();
        tenantBuild.setDeleteStatus(0);
        List<AccessGroup> allAccessGroup = accessGroupMapper.select(tenantBuild);

        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));

        for (AccessGroup ag : accessGroupList) {
            if (ag.getType().equals(1)) {
                continue;
            }
            Set<String> userSet = getGroupUserSize(ag, allUserList, allNotRootGroup, new HashSet<>());
            ag.setUserSet(userSet);
        }
        return accessGroupList;
    }

    @Override
    public Set<String> getChildrenGroupUser(String userId) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        if (tenantId == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        if (StringUtils.isEmpty(userId)) {
            return new HashSet<>();
        }
        Set<String> allChildrenUserSet = new HashSet<>();
        AccessGroup user = AccessGroup.builder().tenantId(tenantId).name(userId).build();
        user.setDeleteStatus(0);

        allChildrenUserSet.add(userId);
        List<AccessGroup> selectGroup = accessGroupMapper.select(user);
        List<Integer> ids = new ArrayList<>();
        for (AccessGroup accessGroup : selectGroup) {
            if (accessGroup.getIsLeader() != null && accessGroup.getIsLeader().equals(0)) {
                ids.add(accessGroup.getParentId());
            }
        }

        if (ids.isEmpty()) {
            return allChildrenUserSet;
        }

        List<AccessGroup> accessGroupList = seletByParentIds(ids, 0);

        List<String> collect = seletByParentIds(ids, 1)
                .stream().map(AccessGroup::getName).collect(Collectors.toList());

        allChildrenUserSet.addAll(collect);

        AccessGroup tenantBuild = AccessGroup.builder().tenantId(tenantId).build();
        tenantBuild.setDeleteStatus(0);
        List<AccessGroup> allAccessGroup = accessGroupMapper.select(tenantBuild);

        //build user count
        List<AccessGroup> allUserList = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(1))
                .collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> allNotRootGroup = allAccessGroup.stream()
                .filter(data -> data.getParentId() != null && data.getType().equals(0))
                .collect(Collectors.groupingBy(AccessGroup::getParentId));


        allChildrenUserSet.add(userId);
        for (AccessGroup ag : accessGroupList) {
            if (ag.getType().equals(1)) {
                continue;
            }
            Set<String> userSet = getGroupUserSize(ag, allUserList, allNotRootGroup, new HashSet<>());
            allChildrenUserSet.addAll(userSet);
        }

        //添加同级别用户
        allUserList.stream().map(data -> {
            if (data.getParentId().equals(user.getParentId())) {
                allChildrenUserSet.add(data.getName());
            }
            return data;
        });
        return allChildrenUserSet;
    }

    @Override
    public List<AccessGroup> getDeptFullPath(Integer groupId) {
        if (groupId == null) {
            return new ArrayList<>();
        }
        AccessGroup accessGroup = checkExist(groupId);
        AccessGroup build = AccessGroup.builder().tenantId(accessGroup.getTenantId()).type(0).build();
        build.setDeleteStatus(0);

        List<AccessGroup> allGroupList = getBaseMapper().select(build);
        Map<Integer, List<AccessGroup>> collect = allGroupList.stream()
                .collect(Collectors.groupingBy(AccessGroup::getId));

        List<AccessGroup> allParentGroupList = new ArrayList<>();
        getParentGroup(accessGroup, allParentGroupList, collect);

        return allParentGroupList;
    }

    @Override
    public List<AccessGroup> getParentGroupList(Integer tenantId, String userName) {
        AccessUser accessUserBuilder = AccessUser.builder().tenantId(tenantId).name(userName).build();
        accessUserBuilder.setDeleteStatus(DeleteEntity.NOT_DELETE);
        AccessUser accessUser = accessUserService.selectOne(accessUserBuilder);
        if (accessUser == null || accessUser.getFreezeStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "当前用户已删除或已冻结");
        }

        List<AccessUserGroup> userGroupList = accessUserGroupService.userGroupList(accessUser.getId());
        if (CollectionUtils.isEmpty(userGroupList)) {
            return Collections.emptyList();
        }

        Example example = new Example(AccessGroup.class);
        example.or()
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        Map<Integer, AccessGroup> groupMap = accessGroupMapper.selectByExample(example).stream()
                .collect(HashMap::new, (m, item) -> m.put(item.getId(), item), HashMap::putAll);

        List<AccessGroup> resultList = new ArrayList<>();
        userGroupList.forEach(item -> traceParent(item.getGroupId(), groupMap, resultList));
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void taskCostInit() {
        CostAllocation costAllocation = new CostAllocation();
        costAllocation.setDeleteStatus(0);
        Task task = new Task();
        task.setDeleteStatus(0);
//        TaskVersion taskVersion = new TaskVersion();
        List<CostAllocation> costAllocationList = costAllocationService.listByExample(costAllocation);
        Map<Integer, List<CostAllocation>> collect = costAllocationList.stream()
                .collect(Collectors.groupingBy(CostAllocation::getTaskId));

//        List<Task> updateTask = new ArrayList<>();
//        List<TaskVersion> updateTaskVersion = new ArrayList<>();
        Example example = new Example(Task.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", 0);

        List<Task> taskList = taskService.listByExample(example);
        for (Task data : taskList) {
            if (collect.get(data.getId()) != null) {
                Map<String, Object> jsonMap = JSON.parseObject(data.getRuntimeConfig(), Map.class);
                List<CostAllocation> costAllocationList1 = collect.get(data.getId());
                List<RuntimeConfig.Cost> costList = new ArrayList<>();
                for (CostAllocation costAc : costAllocationList1) {
                    RuntimeConfig.Cost cost = new RuntimeConfig.Cost();
                    BigDecimal value = new BigDecimal(costAc.getValue().toString());
                    BigDecimal dividend = new BigDecimal("100");
                    Integer result = value.multiply(dividend).setScale(0, BigDecimal.ROUND_UP).intValue();
                    List<Integer> costKeyList = getDeptFullPath(costAc.getGroupId()).stream()
                            .map(BaseEntity::getId).collect(Collectors.toList());
                    Integer[] costKey = costKeyList.toArray(new Integer[costKeyList.size()]);
//                    cost.setKey(costKey);
                    cost.setValue(result.toString());
                    costList.add(cost);
                }

                jsonMap.put("cost", costList);
                data.setRuntimeConfig(JSON.toJSONString(jsonMap));
//                updateTask.add(data);
                taskService.superUpdate(data);
                List<TaskVersion> updateTaskVersion = new ArrayList<>();
                TaskVersion taskVersion = TaskVersion.builder().taskId(data.getId()).build();
                List<TaskVersion> list = taskVersionService.list(taskVersion);
                for (TaskVersion tv : list) {
                    Map<String, Object> versionMap = JSON.parseObject(tv.getRuntimeConfig(), Map.class);
                    versionMap.put("cost", costList);
                    tv.setRuntimeConfig(JSON.toJSONString(jsonMap));
                    updateTaskVersion.add(tv);
                }
                taskVersionService.update(updateTaskVersion);
            }
        }

    }


    @Override
    public AccessGroup getRootGroup(Integer groupId) {
        if (groupId == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }

        AccessGroup group = AccessGroup.builder().type(0).build();
        List<AccessGroup> allGroup = accessGroupMapper.select(group);
        Map<Integer, List<AccessGroup>> collect = allGroup.stream().collect(Collectors.groupingBy(AccessGroup::getId));
        AccessGroup rootGroup = findRootGroup(collect, groupId);

        return rootGroup;
    }

    public AccessGroup findRootGroup(Map<Integer, List<AccessGroup>> collect, Integer groupId) {
        List<AccessGroup> accessGroupList = collect.get(groupId);
        if (accessGroupList == null || accessGroupList.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }

        AccessGroup accessGroup = accessGroupList.stream().findFirst().get();
        if (accessGroup.getParentId() == null) {
            return accessGroup;
        }
        groupId = accessGroup.getParentId();

        return findRootGroup(collect, groupId);
    }


    public void getParentGroup(AccessGroup accessGroup, List<AccessGroup> allParentGroupList,
                               Map<Integer, List<AccessGroup>> allGroup) {
        allParentGroupList.add(0, accessGroup);
        if (accessGroup.getParentId() == null || allGroup.get(accessGroup.getParentId()) == null) {
            return;
        }
        getParentGroup(allGroup.get(accessGroup.getParentId()).stream().findFirst().get(), allParentGroupList, allGroup);
    }


    public void findAllUserParentGroup(Set<AccessGroup> resultGroup, Set<Integer> groupIdSet,
                                       Map<Integer, List<AccessGroup>> groupMap,
                                       AccessGroup accessGroup) {
        AccessGroup group = groupMap.get(accessGroup.getParentId()).stream().findFirst().get();
        if (groupIdSet.contains(group.getId())) {
            return;
        }
        resultGroup.add(group);
        groupIdSet.add(group.getId());
        if (group.getParentId() == null) {
            return;
        }
        findAllUserParentGroup(resultGroup, groupIdSet, groupMap, group);
    }

    public List<AccessGroup> initUser(Map<String, List<AccessUser>> userMap) throws Exception {
        List<AccessGroup> groupList = new ArrayList<>();
        FileReader fr = new FileReader("/data/code/k8s/user_group_relation");
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        String[] arrs = null;
        while ((line = br.readLine()) != null) {
            arrs = line.split("\t");
            AccessGroup accessGroup = new AccessGroup();
            accessGroup.setParentId(Integer.parseInt(arrs[1]));
            accessGroup.setName(arrs[0]);
            accessGroup.setIsLeader(1);
            List<AccessUser> accessUsers = userMap.get(arrs[0]);
            if (accessUsers != null) {
                accessGroup.setUserId(accessUsers.stream().findFirst().get().getId());
                accessGroup.setType(1);
                accessGroup.setCreateBy("xuebotao");
                accessGroup.setUpdateBy("xuebotao");
                accessGroup.setDeleteStatus(0);
                accessGroup.setTenantId(1);
                accessGroup.setCreateTime(new Timestamp(System.currentTimeMillis()));
                accessGroup.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                groupList.add(accessGroup);
            }
        }
        br.close();
        fr.close();
        return groupList;
    }

    private void traceParent(Integer groupId, Map<Integer, AccessGroup> groupMap, List<AccessGroup> depthList) {
        if (!groupMap.containsKey(groupId)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), String.format("用户组不存在 %s", groupId));
        }

        AccessGroup currentGroup = groupMap.get(groupId);
        depthList.add(currentGroup);

        if (currentGroup.getParentId() == null) {
            return;
        }

        traceParent(currentGroup.getParentId(), groupMap, depthList);
    }

}
