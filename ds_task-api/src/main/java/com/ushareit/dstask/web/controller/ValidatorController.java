package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.common.format.TaskFormatter;
import com.ushareit.dstask.common.param.ValidatorCheckParam;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.ValidatorService;
import com.ushareit.dstask.validator.vo.ValidTypeVO;
import com.ushareit.dstask.validator.vo.ValidateResult;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author fengxiao
 * @date 2023/2/7
 */
@RestController
@RequestMapping("validator")
public class ValidatorController {

    @Autowired
    private ValidatorService validatorService;

    @GetMapping("item/list")
    public BaseResponse<List<ValidTypeVO>> list(@RequestParam("templateCode") String templateCode) {
        return BaseResponse.success(validatorService.getValidList(TemplateEnum.of(templateCode)));
    }

    @PostMapping("check")
    public BaseResponse<ValidateResult> check(@Valid @RequestBody ValidatorCheckParam param) {
        TaskFormatter.formatFromWeb(param.getTask());
        return BaseResponse.success(validatorService.validate(ValidType.of(param.getValidType()), param.getTask()));
    }

}
