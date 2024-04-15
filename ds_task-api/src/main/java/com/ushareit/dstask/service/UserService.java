package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.UserBase;

import java.util.List;

public interface UserService extends BaseService<UserBase>{

    List<UserBase> listUsersInfo(String name);

    Boolean isAdmin();
}