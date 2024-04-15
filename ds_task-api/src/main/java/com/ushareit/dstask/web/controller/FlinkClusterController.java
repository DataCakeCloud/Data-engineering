package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.FlinkCluster;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.FlinkClusterService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "Flink集群管理")
@RestController
@RequestMapping("/flinkcluster")
public class FlinkClusterController extends BaseBusinessController<FlinkCluster> {

    @Autowired
    private FlinkClusterService flinkClusterService;

    @Override
    public BaseService<FlinkCluster> getBaseService() {
        return flinkClusterService;
    }

    @GetMapping("/auto")
    public BaseResponse auto() {
        return BaseResponse.success(flinkClusterService.listAutoScaleClusters());
    }

    @GetMapping("/noauto")
    public BaseResponse noAuto() {
        return BaseResponse.success(flinkClusterService.listNonAutoScaleClusters());
    }
}
