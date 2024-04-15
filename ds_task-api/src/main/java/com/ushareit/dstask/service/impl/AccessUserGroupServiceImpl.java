package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessUserGroup;
import com.ushareit.dstask.mapper.AccessUserGroupMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.AccessUserGroupService;
import com.ushareit.dstask.service.AccessUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Slf4j
@Service
public class AccessUserGroupServiceImpl extends AbstractBaseServiceImpl<AccessUserGroup> implements AccessUserGroupService {
    @Resource
    private AccessUserGroupMapper accessUserGroupMapper;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessGroupService accessGroupService;

    @Override
    public CrudMapper<AccessUserGroup> getBaseMapper() {
        return accessUserGroupMapper;
    }


    @Override
    public List<Integer> selectByGroupId(Integer groupId) {
        return accessUserGroupMapper.selectByGroupId(groupId);
    }

    @Override
    public void deleteByGroupIdAndUserId(Integer groupId, Integer userId) {
        accessUserGroupMapper.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public List<AccessUserGroup> userGroupList(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        Example example = new Example(AccessUserGroup.class);
        example.or()
                .andEqualTo("userId", userId);
        return accessUserGroupMapper.selectByExample(example);
    }
}
