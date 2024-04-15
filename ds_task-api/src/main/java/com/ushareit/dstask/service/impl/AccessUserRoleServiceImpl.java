package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessUserRole;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessUserRoleMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessRoleService;
import com.ushareit.dstask.service.AccessTenantRoleService;
import com.ushareit.dstask.service.AccessUserRoleService;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Slf4j
@Service
public class AccessUserRoleServiceImpl extends AbstractBaseServiceImpl<AccessUserRole> implements AccessUserRoleService {
    @Resource
    private AccessUserRoleMapper accessUserRoleMapper;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessRoleService accessRoleService;

    @Resource
    private AccessTenantRoleService accessTenantRoleService;

    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Override
    public CrudMapper<AccessUserRole> getBaseMapper() {
        return accessUserRoleMapper;
    }

    @Override
    public void addRole(Integer userId, String roleIds) {
        // roleIds 包含新的和旧的
       accessUserService.checkExistAndFreeze(userId);

        // 根据用户id ，删除用户下所有对应的旧角色
        accessUserRoleService.deleteByUserId(userId);

       if (StringUtils.isEmpty(roleIds)) {
           return;
       }

        String[] arr = roleIds.split(",");
        List<String> roles = Arrays.asList(arr);
        List<Integer> newRoleList = roles.stream().map(role -> Integer.parseInt(role)).collect(Collectors.toList());

        List<AccessUserRole> result = newRoleList.stream().map(roleId -> {
            AccessUserRole accessUserRole = new AccessUserRole(roleId, userId);
            accessUserRole.setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return accessUserRole;
        }).collect(Collectors.toList());
        accessUserRoleMapper.insertList(result);

    }

    @Override
    public void addUsers(Integer roleId, String userIds) {
        // userIds 只有新增的用户
        accessRoleService.checkExist(roleId);

        String[] split = userIds.split(",");
        List<Integer> newUserIds = Arrays.asList(split).stream().map(id -> Integer.parseInt(id)).collect(Collectors.toList());
        List<Integer> oldUserIds = accessUserRoleService.selectByRoleId(roleId);
        List<Integer> intersection = intersection(newUserIds, oldUserIds);
        if (intersection.size() >= 1) {
            for (Iterator<Integer> iterator = newUserIds.iterator(); iterator.hasNext(); ){
                Integer i=iterator.next();
                if (intersection.contains(i)){
                    iterator.remove();
                }
            }
                /*throw new ServiceException(BaseResponseCodeEnum.USER_HAD_ADDED, "用户id分别是:"
                    + intersection.stream().map(Object::toString).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())));*/
        }
        if (CollectionUtils.isNotEmpty(newUserIds)){
            List<AccessUserRole> collect = newUserIds.stream().map(userId -> {
                AccessUserRole accessUserRole = new AccessUserRole(roleId, userId);
                accessUserRole.setCreateBy(InfTraceContextHolder.get().getUserName())
                        .setUpdateBy(InfTraceContextHolder.get().getUserName())
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()));
                return accessUserRole;
            }).collect(Collectors.toList());
            accessUserRoleMapper.insertList(collect);
        }

    }

    private List<Integer> intersection(List<Integer> list1, List<Integer> list2) {
        List<Integer> collect = list1.stream().filter(id -> list2.contains(id)).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Integer> selectByRoleId(Integer roleId) {
        return accessUserRoleMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Integer> selectByUserId(Integer userId) {
        return accessUserRoleMapper.selectByUserId(userId);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        accessUserRoleMapper.deleteByUserId(userId);
    }

    @Override
    public void removeUser(Integer roleId, Integer userId) {
        accessUserRoleMapper.deleteByRoleIdAndUserId(roleId, userId);
    }
}
