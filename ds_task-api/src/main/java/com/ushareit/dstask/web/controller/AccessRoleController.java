package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessRole;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AccessRoleService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2022/4/13
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/role")
public class AccessRoleController  extends BaseBusinessController<AccessRole> {
    @Resource
    private AccessRoleService accessRoleService;

    @Override
    public BaseService<AccessRole> getBaseService() {
        return accessRoleService;
    }

    @ApiOperation(value = "详情中更新菜单")
    @GetMapping("/updateMenus")
    public BaseResponse getList(@RequestParam("id") Integer id, @RequestParam("menuIds") String menuIds) throws Exception {
        // id表示角色id
        accessRoleService.updateMenus(id, menuIds);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }
}
