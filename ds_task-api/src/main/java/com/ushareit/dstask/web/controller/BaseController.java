package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.AuthResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.AuthException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 基础操作Controller
 *
 * @author wuyan
 * @date 2018/10/30
 **/
public abstract class BaseController {

    @Resource
    private HttpServletRequest request;

    public CurrentUser getCurrentUser() {
        CurrentUser currentUser = (CurrentUser) request.getAttribute(CommonConstant.CURRENT_LOGIN_USER);
        if (currentUser == null) {
            throw new AuthException(AuthResponseCodeEnum.NO_LOGIN);
        }
        return currentUser;
    }
}
