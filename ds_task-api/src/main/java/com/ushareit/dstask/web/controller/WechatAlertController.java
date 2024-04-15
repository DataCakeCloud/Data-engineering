package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.WechatAlert;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.WechatAlertService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "微信报警信息")
@RestController
@RequestMapping("/wechatAlert")
public class WechatAlertController extends BaseBusinessController<WechatAlert>{
    @Resource
    private WechatAlertService weChatAlertService;

    @Override
    public BaseService getBaseService() {
        return weChatAlertService;
    }

    @ApiOperation(value = "添加报警信息")
    @PostMapping("/add")
    public BaseResponse add(@RequestBody @Valid WechatAlert wechatAlert) {
        CurrentUser currentUser = getCurrentUser();
        wechatAlert.setDeleteStatus(DeleteEntity.NOT_DELETE);
        weChatAlertService.addWechat(wechatAlert, currentUser);
        return BaseResponse.success();
    }

    @ApiOperation(value = "更新报警信息")
    @PostMapping("/update")
    public BaseResponse update(@RequestBody @Valid WechatAlert wechatAlert) {
        CurrentUser currentUser = getCurrentUser();
        weChatAlertService.updateWechat(wechatAlert, currentUser);
        return BaseResponse.success();
    }

    @ApiOperation(value = "删除报警信息")
    @DeleteMapping("/deleteWechat")
    public BaseResponse deleteWechat(@RequestParam("id") @Valid Integer id) {
        CurrentUser currentUser = getCurrentUser();
        weChatAlertService.deleteWechat(id, currentUser);
        return BaseResponse.success();
    }

    @ApiOperation(value = "查询报警信息")
    @PostMapping("/getWechat")
    public BaseResponse getWechat(@RequestBody @Valid WechatAlert wechatAlert) {
        List<WechatAlert> result = weChatAlertService.getWechat(wechatAlert, getCurrentUser());
        return BaseResponse.success(result);
    }

    @GetMapping("listById")
    public BaseResponse<List<WechatAlert>> listById(@RequestParam("id") Integer id) {
        List<WechatAlert> wechatAlerts = weChatAlertService.selectTockenByUserGroupId(id);
        return BaseResponse.success(wechatAlerts);
    }

    @GetMapping("listByName")
    public BaseResponse<List<WechatAlert>> listByName(@RequestParam("name") String name) {
        List<WechatAlert> wechatAlerts = weChatAlertService.selectTockenByUserGroupName(name);
        return BaseResponse.success(wechatAlerts);
    }

}
