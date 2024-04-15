package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.IdUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;

public interface UserGroupService extends BaseService<UserGroup>{
    void addUserGroup(UserGroup userGroup);

    void editUserGroup(UserGroup userGroup);

    void deleteUserGroup(Integer id);


    void addUser(UserGroupRelation userGroupRelation);

    void removeUser(UserGroupRelation userGroupRelation);

    List<UserGroupVo> selectAllUserGroup();

    List<UserGroupVo> selectLoginUserGroup();
}
