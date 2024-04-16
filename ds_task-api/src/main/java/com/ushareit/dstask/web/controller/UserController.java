package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.DeptService;
import com.ushareit.dstask.service.UserService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户相关管理")
@RestController
@RequestMapping("/user")
public class UserController extends BaseBusinessController<UserBase> {

    @Autowired
    private UserService userService;

    @Autowired
    private DeptService deptService;

    @Override
    public BaseService<UserBase> getBaseService() {
        return userService;
    }


    @ApiOperation(value = "获取用户基本信息")
    @Override
    @GetMapping("/list")
    public BaseResponse list(UserBase user) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS,userService.listUsersInfo(user.getName()));
    }

    @ApiOperation(value = "判断用户是否管理员角色")
    @GetMapping("/admin")
    public BaseResponse isAdmin() {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, userService.isAdmin());
    }

    @ApiOperation(value = "获取部门列表")
    @GetMapping("/getDingDingDepList")
    public BaseResponse getDingDingDepList(UserBase user) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, deptService.getDepartmentsList());
    }


    @ApiOperation(value = "获取用户部门信息")
    @GetMapping("/getDingDingDep")
    public BaseResponse getDingDingDep(UserBase user) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, deptService.getDeptInfo(user.getName()));
    }

    @ApiOperation(value = "获取有效成本部门列表")
    @GetMapping("/getEffectiveCostList")
    public BaseResponse getEffectiveCostList(UserBase user) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, deptService.getEffectiveDeptList(user.getName()));
    }
}