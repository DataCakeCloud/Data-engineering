package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.ApiGateway;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.DataAudit;
import com.ushareit.dstask.service.AuditService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author tianxu
 * @date 2023/12/25
 **/

@Api(tags = "审计")
@RestController
@RequestMapping("/audit")
public class AuditController extends BaseBusinessController<ApiGateway> {

    @Resource
    private AuditService auditService;

    @Override
    public BaseService getBaseService() {
        return auditService;
    }

    @ApiOperation(value = "uri映射")
    @GetMapping("/uriMap")
    public BaseResponse uriMap(@RequestParam(name = "uri", required = false) String uri) {
        CurrentUser currentUser = getCurrentUser();
        return BaseResponse.success(auditService.uriMap(uri, currentUser));
    }

    @ApiOperation(value = "模块包含事件")
    @GetMapping("/moduleEvent")
    public BaseResponse moduleEvent(@RequestParam(name = "source", required = false) String source,
                                    @RequestParam(name = "name", required = false) String name) {
        CurrentUser currentUser = getCurrentUser();
        return BaseResponse.success(auditService.moduleEvent(source, name, currentUser));
    }

    @ApiOperation(value = "功能审计")
    @PostMapping("/function")
    public BaseResponse function(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "50") Integer pageSize,
                            @RequestBody @Valid ApiGateway apiGateway) {
        CurrentUser currentUser = getCurrentUser();
        return BaseResponse.success(auditService.auditFunction(pageNum, pageSize, apiGateway, currentUser));
    }

    @ApiOperation(value = "数据审计")
    @PostMapping("/data")
    public BaseResponse data(@RequestBody @Valid DataAudit dataAudit) {
        CurrentUser currentUser = getCurrentUser();
        return BaseResponse.success(auditService.auditData(dataAudit, currentUser));
    }
}
