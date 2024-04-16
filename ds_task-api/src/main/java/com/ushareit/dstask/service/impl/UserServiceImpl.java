package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.UserService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.ItUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserServiceImpl  extends AbstractBaseServiceImpl<UserBase> implements UserService {


    @Resource
    public AccessUserService accessUserService;

    @Override
    public CrudMapper<UserBase> getBaseMapper() {
        return null;
    }

    @Override
    public List<UserBase> listUsersInfo(String name) {
        //不是外部部署 并且不是私有才走内部架构
        boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (!DataCakeConfigUtil.getDataCakeConfig().getDcRole() && !isPrivate) {
            ItUtil itUtil = new ItUtil();
            return itUtil.getUsersInfo(name);
        }
        List<AccessUser> accessUserList = accessUserService.likeByName(name);
        return accessUserList.stream().map(UserBase::conversion).collect(Collectors.toList());
    }

    @Override
    public Boolean isAdmin() {
        return InfTraceContextHolder.get().getAdmin();
    }
}