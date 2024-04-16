package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.Artifact;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/accessuser")
public class AccessUserController extends BaseBusinessController<AccessUser> {
    @Resource
    private AccessUserService accessUserService;

    @Override
    public BaseService<AccessUser> getBaseService() {
        return accessUserService;
    }


    @ApiOperation(value = "根据角色获取用户列表")
    @GetMapping("/listBy")
    public BaseResponse selectByRoleId(@RequestParam("roleId") Integer roleId) throws Exception {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, accessUserService.selectByRoleId(roleId));
    }

    @ApiOperation(value = "冻结用户")
    @GetMapping("/freeze")
    public BaseResponse freeze(@RequestParam("id") Integer id,
                               @RequestParam("freeze") Integer freeze) throws Exception {
        accessUserService.freeze(id, freeze);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


    @ApiOperation(value = "发送验证码")
    @GetMapping("/sendCode")
    public BaseResponse sendCode(@RequestParam("tenantName") String tenantName,
                                 @RequestParam("email") String email) {
        accessUserService.sendCode(tenantName, email);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "校验最新的验证码")
    @GetMapping("/checkCode")
    public BaseResponse checkLatestCode(@RequestParam("tenantName") String tenantName,
                                        @RequestParam("email") String email,
                                        @RequestParam("code") String code) {
        Boolean aBoolean = accessUserService.checkLatestCode(tenantName, email, code);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, aBoolean);
    }

    @ApiOperation(value = "重置密码")
    @GetMapping("/resetPassword")
    public BaseResponse resetPassword(@RequestParam("userId") Integer userId) {
        accessUserService.resetPassword(userId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "更新密码")
    @GetMapping("/updatePassword")
    public BaseResponse updatePassword(@RequestParam("tenantName") String tenantName,
                                       @RequestParam("email") String email,
                                       @RequestParam("password") String password,
                                       @RequestParam("code") String code) {
        accessUserService.checkLatestCode(tenantName, email, code);
        accessUserService.updatePassword(tenantName, email, password);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public BaseResponse login(@RequestBody @Valid  AccessUser accessUser) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, accessUserService.login(accessUser));
    }

    @ApiOperation(value = "校验MFA")
    @PostMapping("/checkMFACode")
    public BaseResponse checkMFACode(@RequestBody @Valid AccessUser accessUser) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, accessUserService.checkMFACode(accessUser));
    }

    @ApiOperation(value = "解绑MFA")
    @PostMapping("/unbundlingMFA")
    public BaseResponse unbundlingMFA(@RequestBody @Valid AccessUser accessUser) {
        accessUserService.unbundlingMFA(accessUser);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "批量添加用户")
    @GetMapping("/batchAddUser")
    public BaseResponse batchAddUser() throws Exception {
        accessUserService.batchAddUser();
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @PostMapping("/addUser")
    public BaseResponse addUser(@RequestBody AccessUser accessUser){
        accessUserService.addUser(accessUser);
        return BaseResponse.success();
    }

    @PostMapping("/editUser")
    public BaseResponse editUser(@RequestBody AccessUser accessUser){
        accessUserService.editUser(accessUser);
        return BaseResponse.success();
    }
}
