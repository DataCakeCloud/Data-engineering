package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.OperateLog;
import com.ushareit.dstask.common.param.OperateLogListParam;
import com.ushareit.dstask.service.OperateLogService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.stream.events.EndDocument;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
@Api(tags = "操作日志管理")
@RestController
@RequestMapping("operate")
public class OperateLogController {

    @Autowired
    private OperateLogService operateLogService;

    @GetMapping("/page")
    public BaseResponse<PageInfo<OperateLog>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @Valid @ModelAttribute OperateLogListParam param) {
        return BaseResponse.success(operateLogService.listByPage(pageNum, pageSize, param.toExample()));
    }

    @GetMapping("/dump")
    public BaseResponse<List<OperateLog>> page(@Valid @ModelAttribute OperateLogListParam param) {
        return BaseResponse.success(operateLogService.listByExample(param.toExample()));
    }

    @GetMapping("/getDayUsers")
    public BaseResponse<List<Map<String, Integer>>> getDayUsers() {
        return BaseResponse.success(operateLogService.getDayUsers());
    }

    @GetMapping("/getWeekUsers")
    public BaseResponse<Map<String, Integer>> getWeekUsers(@RequestParam("start") Timestamp start, @RequestParam("end") Timestamp end) {
        return BaseResponse.success(operateLogService.getWeekUsers(start, end));
    }

}
