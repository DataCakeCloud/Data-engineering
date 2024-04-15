package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.common.vo.BatchUserGroupRelationVo;
import com.ushareit.dstask.service.AccessTableService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.UserGroupService;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户组接口
 */
@RestController
@RequestMapping("/userGroup")
public class UserGroupController  extends BaseBusinessController<UserGroup>  {

    @Autowired
    private UserGroupService userGroupService;

    @Resource
    private AccessTableService accessTableService;

    @Override
    public BaseService<UserGroup> getBaseService() {
        return userGroupService;
    }

    @RequestMapping("/selectAllUserGroup")
    public BaseResponse selectAllUserGroup(){
        return BaseResponse.success(userGroupService.selectAllUserGroup());
    }

    @RequestMapping("/selectLoginUserGroup")
    public BaseResponse selectLoginUserGroup(){
        return BaseResponse.success(userGroupService.selectLoginUserGroup());
    }

    @RequestMapping("/addUserGroup")
    public BaseResponse addUserGroup(@RequestBody UserGroup userGroup){
        userGroupService.addUserGroup(userGroup);
        return BaseResponse.success();
    }

    @RequestMapping("/tableStatistics")
    public BaseResponse tableStatistics() {
        accessTableService.tableStatistics();
        return BaseResponse.success();
    }

    @RequestMapping("/editUserGroup")
    public BaseResponse editUserGroup(@RequestBody UserGroup userGroup){
        userGroupService.editUserGroup(userGroup);
        return BaseResponse.success();
    }

    @RequestMapping("/deleteUserGroup")
    public BaseResponse deleteUserGroup(Integer id){
        userGroupService.deleteUserGroup(id);
        return BaseResponse.success();
    }

    @RequestMapping("/addUser")
    public BaseResponse addUser(@RequestBody UserGroupRelation userGroupRelation){
        userGroupService.addUser(userGroupRelation);
        return BaseResponse.success();
    }

    @RequestMapping("/batchAddUser")
    public BaseResponse batchAddUser(@RequestBody BatchUserGroupRelationVo batchUserGroupRelationVo){
        if (CollectionUtils.isNotEmpty(batchUserGroupRelationVo.getUserGroupRelationList())){
            batchUserGroupRelationVo.getUserGroupRelationList().forEach(userGroupRelation -> {
                userGroupService.addUser(userGroupRelation);
            });
        }
        return BaseResponse.success();
    }

    @RequestMapping("/removeUser")
    public BaseResponse removeUser(@RequestBody UserGroupRelation userGroupRelation){
        userGroupService.removeUser(userGroupRelation);
        return BaseResponse.success();
    }
}
