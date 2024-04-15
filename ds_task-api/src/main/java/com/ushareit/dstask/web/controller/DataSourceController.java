package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.third.airbyte.AirbyteService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengxiao
 * @date 2022/7/15
 */
@Slf4j
@Api(tags = "数据源管理")
@RestController
@RequestMapping("/source")
public class DataSourceController {

    @Autowired
    private AirbyteService airbyteService;

    @ApiOperation("获取 spec")
    @GetMapping("spec")
    public BaseResponse<?> spec() throws Exception {
        return BaseResponse.success(airbyteService.spec("airbyte/source-mysql:0.5.11"));
    }
}
