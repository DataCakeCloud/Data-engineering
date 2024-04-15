package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/13
 */
@Api(tags = "group管理")
@RestController
@RequestMapping("/group")
public class AccessGroupController extends BaseBusinessController<AccessGroup> {
    @Resource
    private AccessGroupService accessGroupService;

    @Override
    public BaseService<AccessGroup> getBaseService() {
        return accessGroupService;
    }

    @ApiOperation(value = "添加用户")
    @GetMapping("/add/user")
    public BaseResponse addUsers(@RequestParam("id") Integer id, @RequestParam("userIds") String userIds,
                                 @RequestParam("isLeader") String isLeader) throws Exception {
        // id表示角色id
        accessGroupService.addUsers(id, userIds, isLeader);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "移除用户")
    @GetMapping("/remove/user")
    public BaseResponse addUsers(@RequestParam("id") Integer id, @RequestParam("userId") Integer userId) throws Exception {
        // id表示角色id
        accessGroupService.removeUser(id, userId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


    @ApiOperation(value = "列出用户组织架构")
    @GetMapping("/user/organization")
    public BaseResponse getOrganizationStructure(@RequestParam("userId") Integer userId) {
        return BaseResponse.success(accessGroupService.getOrganizationStructure(userId));
    }

    @ApiOperation(value = "获取子组信息")
    @PostMapping("/user/childrenGroup")
    public BaseResponse getChildrenGroup(@RequestBody AccessGroup accessGroup) {
        return BaseResponse.success(accessGroupService.getChildrenGroup(accessGroup));
    }


    @ApiOperation(value = "获取当前用户组的目录树")
    @GetMapping("/userTree")
    public BaseResponse userTree(@RequestParam("userId") String userId) {
        return BaseResponse.success(accessGroupService.userTree(userId));
    }

    @ApiOperation(value = "获取组织架构")
    @GetMapping("/groupTree")
    public BaseResponse groupTree() {
        return BaseResponse.success(accessGroupService.groupTree());
    }

    @ApiOperation(value = "获取创建者的树")
    @GetMapping("/createByUserTree")
    public BaseResponse userTreeByCreateBY(@RequestParam("userId") String userId) {
        return BaseResponse.success(accessGroupService.createByTree(userId));
    }

    @ApiOperation(value = "获取组下面的所有人")
    @GetMapping("/listUserIdsByGroupIds")
    public BaseResponse listUserIdsByGroupIds(@RequestParam("groupIds") String groupIds) {
        return BaseResponse.success(accessGroupService.listUserIdsByGroupIds(Arrays.asList(groupIds.split(",")).stream().map(s -> Integer.valueOf(s)).collect(Collectors.toList())));
    }

    @ApiOperation(value = "获取用户的组包含父节点")
    @GetMapping("/listGroupIdsByUserId")
    public BaseResponse listGroupIdsByUserId(@RequestParam("userId") String userId) {
        return BaseResponse.success(accessGroupService.listAllParentGroupByUserId(userId));
    }

    @ApiOperation(value = "获取当前用户组")
    @PostMapping("/getCurrentGroup")
    public BaseResponse getCurrentGroup(@RequestBody AccessGroup accessGroup) {
        return BaseResponse.success(accessGroupService.getCurrentGroup(accessGroup));
    }


    @ApiOperation(value = "获取当前组下的所有用户")
    @PostMapping("/getCurrentGroupUser")
    public BaseResponse getCurrentGroupUser(@RequestBody AccessGroup accessGroup) {
        return BaseResponse.success(accessGroupService.getCurrentGroupUser(accessGroup));
    }

    @ApiOperation(value = "获取当前用户下属的所有用户")
    @GetMapping("/getChildrenUser")
    public BaseResponse getChildrenUser(@RequestParam("userId") String userId) {
        return BaseResponse.success(accessGroupService.getChildrenGroupUser(userId));
    }

    @ApiOperation(value = "获取当部门全路径")
    @GetMapping("/getDeptFullPath")
    public BaseResponse getChildrenUser(@RequestParam("groupId") Integer groupId) {
        return BaseResponse.success(accessGroupService.getDeptFullPath(groupId));
    }

    @ApiOperation(value = "刷所有任务的成本归属")
    @GetMapping("/taskCostInit")
    public BaseResponse taskCostInit() {
        accessGroupService.taskCostInit();
        return BaseResponse.success();
    }

}
