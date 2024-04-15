package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.DashboardBase;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.DashboardService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "看板管理")
@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseBusinessController<DashboardBase> {
    @Autowired
    private DashboardService dashboardService;


    @Override
    public BaseService<DashboardBase> getBaseService() {
        return dashboardService;
    }


    @ApiOperation(value = "获取看板url")
    @PostMapping("/getUrl")
    public BaseResponse get(@RequestBody @Valid DashboardBase dashboard) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS,dashboardService.getDashboardUrl(dashboard));
    }

    @ApiOperation(value = "申请ds看板权限")
    @PostMapping("/permission")
    public BaseResponse addPermission() {
        dashboardService.addPermission();
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }
}
