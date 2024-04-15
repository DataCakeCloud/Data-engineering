package com.ushareit.dstask.web.controller;


import com.ushareit.dstask.bean.FeedbackProcessItem;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.FeedbackProcessItemService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
/**
 * @author xuebotao
 * @date 2022-04-06
 */
@Api(tags = "工单反馈处理详情")
@RestController
@RequestMapping("feedbackdetail")
public class FeedbackProcessItemController extends BaseBusinessController<FeedbackProcessItem> {

    @Autowired
    private FeedbackProcessItemService feedbackProcessItemService;

    @Override
    public BaseService<FeedbackProcessItem> getBaseService() {
        return feedbackProcessItemService;
    }

    @ApiOperation(value = "详情信息")
    @GetMapping("getDetailInformation")
    public BaseResponse<?> getDetailInformation(@Valid FeedbackProcessItem param) {
        return BaseResponse.success(feedbackProcessItemService.getDetailInformation(param));
    }

    @ApiOperation(value = "消息发送")
    @PostMapping("messageSend")
    public BaseResponse<?> messageSend(@Valid FeedbackProcessItem param) {
        return BaseResponse.success(feedbackProcessItemService.messageSend(param));
    }



}
