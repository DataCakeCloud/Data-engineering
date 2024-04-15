package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.common.param.OperateLogSaveParam;
import com.ushareit.dstask.service.OperateLogService;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author fengxiao
 * @date 2021/8/12
 */
@Lazy
@RestController
@RequestMapping("oidc")
public class OidcController {

    @Autowired
    private OperateLogService operateLogService;

    @PostMapping("log/{source}/save")
    public BaseResponse<?> logStoreWhitelist(@PathVariable("source") String source,
                                             @Valid @RequestBody OperateLogSaveParam param) {
        operateLogService.save(param.toEntity().setSource(source));
        return BaseResponse.success();
    }

}
