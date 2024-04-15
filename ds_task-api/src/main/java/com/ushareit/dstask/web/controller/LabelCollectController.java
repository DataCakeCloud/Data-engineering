package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.LabelCollect;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.LabelCollectService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2022/1/19
 */
@Api(tags = "标签收藏")
@RestController
@RequestMapping("/collect")
public class LabelCollectController extends BaseBusinessController<LabelCollect> {
    @Resource
    private LabelCollectService labelCollectService;
    @Override
    public BaseService<LabelCollect> getBaseService() {
        return labelCollectService;
    }

    @PatchMapping("/add")
    public BaseResponse add(@RequestParam("labelId") Integer labelId) {
        labelCollectService.collect(labelId);
        return BaseResponse.success();
    }

    @GetMapping("/cancel")
    public BaseResponse cancel(@RequestParam("labelId") Integer labelId) {
        labelCollectService.cancel(labelId);
        return BaseResponse.success();
    }

    @GetMapping("/my")
    public BaseResponse my() {
        LabelCollect labelCollect = labelCollectService.get();
        return BaseResponse.success(labelCollect);
    }
}
