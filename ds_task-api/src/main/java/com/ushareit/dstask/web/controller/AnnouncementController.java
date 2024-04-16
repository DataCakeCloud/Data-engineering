package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Announcement;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AnnouncementService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.DeptService;
import com.ushareit.dstask.service.UserService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Api(tags = "用户相关管理")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController extends BaseBusinessController<Announcement> {

    @Autowired
    private AnnouncementService announcementService;

    @Override
    public BaseService<Announcement> getBaseService() {
        return announcementService;
    }


    @Override
    @GetMapping("/list")
    public BaseResponse list(Announcement announcement) {
        return BaseResponse.success(announcementService.limit());
    }



    @ApiOperation(value = "上下线")
    @GetMapping("/online/offline")
    public BaseResponse getEffectiveCostList(Integer id, Integer online) {
        announcementService.onlineAndOffline(id, online);
        return BaseResponse.success();
    }

    @PostMapping("/upload")
    public BaseResponse upload(@RequestBody @Valid MultipartFile image) {
        return BaseResponse.success(announcementService.uploadImage(image));
    }
}