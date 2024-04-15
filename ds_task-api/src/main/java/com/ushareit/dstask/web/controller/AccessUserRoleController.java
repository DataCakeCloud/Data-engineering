package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.AccessUserRole;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.mapper.AccessUserMapper;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.AccessUserRoleService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Api(tags = "用户与角色管理")
@RestController
@RequestMapping("/userrole")
public class AccessUserRoleController extends BaseBusinessController<AccessUserRole> {
    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Resource
    private AccessUserMapper accessUserMapper;

    @Resource
    private AccessGroupService accessGroupService;

    @Override
    public BaseService<AccessUserRole> getBaseService() {
        return accessUserRoleService;
    }

    @ApiOperation(value = "给用户添加不同角色")
    @GetMapping("/addRoles")
    public BaseResponse addRoles(@RequestParam("userId") Integer userId, @RequestParam("roleIds") String roleIds) throws Exception {
        accessUserRoleService.addRole(userId, roleIds);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "给角色添加不同用户")
    @GetMapping("/addUsers")
    public BaseResponse addUsers(@RequestParam("roleId") Integer roleId, @RequestParam(name = "userIds",required = false) String userIds,@RequestParam(name = "groupIds",required = false)String groupIds) throws Exception {
        if (StringUtils.isNoneBlank(groupIds)){
            Set<String> ulist=accessGroupService.listUserIdsByGroupIds(Arrays.asList(groupIds.split(",")).stream().map(s -> Integer.valueOf(s)).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(ulist)){
                StringBuffer stringBuffer=new StringBuffer();
                List<AccessUser> accessUserList=accessUserMapper.selectAll();
                if (CollectionUtils.isNotEmpty(accessUserList)){
                    for (String u:ulist){
                        for (AccessUser accessUser:accessUserList){
                            if (accessUser.getDeleteStatus()== DeleteEntity.NOT_DELETE&&accessUser.getName().equals(u)){
                                stringBuffer.append(accessUser.getId()).append(",");
                            }
                        }
                    }
                }
                if (StringUtils.isNoneBlank(stringBuffer.toString())){
                    String s=stringBuffer.toString();
                    accessUserRoleService.addUsers(roleId,s.substring(0,s.length()-1) );
                }
            }
            return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
        }
        accessUserRoleService.addUsers(roleId, userIds);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "移除角色下的用户")
    @GetMapping("/removeUser")
    public BaseResponse removeUser(@RequestParam("roleId") Integer roleId, @RequestParam("userId") Integer userId) throws Exception {
        accessUserRoleService.removeUser(roleId, userId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }
}
