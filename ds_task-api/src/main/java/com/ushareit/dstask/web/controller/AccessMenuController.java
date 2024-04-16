package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessMenu;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AccessMenuService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/menu")
public class AccessMenuController extends BaseBusinessController<AccessMenu>{
    @Resource
    private AccessMenuService accessMenuService;


    @Override
    public BaseService<AccessMenu> getBaseService() {
        return accessMenuService;
    }

    @ApiOperation(value = "菜单列表")
    @GetMapping("/getlist")
    public BaseResponse getList(@RequestParam("userId") Integer userId) throws Exception {
        // id表示角色id
        List<AccessMenu> list = accessMenuService.list(userId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, list);
    }
}
