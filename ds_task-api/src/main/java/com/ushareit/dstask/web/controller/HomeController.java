package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.service.HomeService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author wuyan
 * @date 2022/9/2
 */
@Api(tags = "home")
@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private HomeService queryHistoryService;

    @GetMapping("/query/statistics")
    public BaseResponse queryTaskStatics(@RequestParam("recent") Integer recent,@RequestParam("userGroup") String userGroup) {
        return BaseResponse.success(queryHistoryService.queryTaskStatics(recent,userGroup));
    }

    @GetMapping("/top/scan")
    public BaseResponse queryScanTop(@RequestParam("userGroup") String userGroup) {
        return BaseResponse.success(queryHistoryService.queryScanTop(userGroup));
    }

    @GetMapping("/top/execution")
    public BaseResponse queryExecutionTop() {
        return BaseResponse.success(queryHistoryService.queryExecutionTop());
    }

    @GetMapping("/overall/score")
    public BaseResponse overallScore() {
        return BaseResponse.success(queryHistoryService.overallScore());
    }

    @GetMapping("/task/statistics")
    public BaseResponse metaTaskStatics(@RequestParam("recent") Integer recent,@RequestParam("userGroup") String userGroup) {
        return BaseResponse.success(queryHistoryService.metaTaskStatics(recent, userGroup));
    }

    @GetMapping("/key")
    public BaseResponse keyIndex() {
        return BaseResponse.success(queryHistoryService.keyIndex());
    }

    @GetMapping("/data/asset")
    public BaseResponse dataResource() {
        return BaseResponse.success(queryHistoryService.dataResource());
    }

    @GetMapping("/top/table")
    public BaseResponse metaTop(@RequestParam("type") Integer type) {
        return BaseResponse.success(queryHistoryService.metaTop(type));
    }
}
