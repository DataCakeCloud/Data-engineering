package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Auditlog;
import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.service.AuditlogService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.TaskVersionService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "任务版本管理")
@RestController
@RequestMapping("/taskversion")
public class TaskVersionController extends BaseBusinessController<TaskVersion> {

    @Autowired
    private TaskVersionService taskVersionService;

    @Autowired
    private AuditlogService auditlogService;

    @Override
    public BaseService<TaskVersion> getBaseService() {
        return taskVersionService;
    }

    @ApiOperation("获取任务版本信息以及审计")
    @GetMapping("/pages")
    public BaseResponse<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @Valid @ModelAttribute Auditlog auditlog) {
        return BaseResponse.success(auditlogService.pages(pageNum, pageSize, auditlog));
    }


    @ApiOperation("版本切换")
    @GetMapping("/verionSwitch")
    public BaseResponse<?> verionSwitch(@Valid TaskVersion taskVersion) {
        taskVersionService.verionSwitch(taskVersion);
        return BaseResponse.success();
    }

}
