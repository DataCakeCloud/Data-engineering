package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessProduct;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AccessProductService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/13
 */
@Api(tags = "产品管理")
@RestController
@RequestMapping("/product")
public class AccessProductController  extends BaseBusinessController<AccessProduct>{
    @Resource
    private AccessProductService accessProductService;

    @Override
    public BaseService<AccessProduct> getBaseService() {
        return accessProductService;
    }

    @ApiOperation(value = "资源列表")
    @GetMapping("/getConfig")
    public BaseResponse getList(@RequestParam(value = "tenantId", required = false) Integer tenantId) throws Exception {
        // 通过租户找到对应配置的资源
        List<AccessProduct> list = accessProductService.getConfig(tenantId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, list);
    }
}
