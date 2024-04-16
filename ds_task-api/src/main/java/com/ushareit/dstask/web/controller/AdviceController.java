package com.ushareit.dstask.web.controller;


import com.ushareit.dstask.bean.Advice;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AdvicekService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author xuebotao
 * @date 2022-08-09
 */
@Api(tags = "用户建议")
@RestController
@RequestMapping("advice")
public class AdviceController extends BaseBusinessController<Advice> {

    @Autowired
    private AdvicekService advicekService;

    @Override
    public BaseService<Advice> getBaseService() {
        return advicekService;
    }

    @ApiOperation("增加用户建议")
    @PostMapping("save")
    public BaseResponse<?> save(@Valid Advice advice) {
        advicekService.save(advice);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


}
