package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Auditlog;
import com.ushareit.dstask.service.AuditlogService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author: xuebotao
 * @create: 2022-01-05
 */
@Api(tags = "任务版本以及操作审计")
@RestController
@RequestMapping("/auditlog")
public class AuditlogController extends BaseBusinessController<Auditlog> {

    @Autowired
    private AuditlogService auditlogService;

    @Override
    public BaseService<Auditlog> getBaseService() {
        return auditlogService;
    }

    @ApiOperation("获取所有的操作类型")
    @GetMapping("/type")
    public BaseResponse<?> type(@RequestParam("module") String module) {
        return BaseResponse.success(auditlogService.type(module));
    }


}
