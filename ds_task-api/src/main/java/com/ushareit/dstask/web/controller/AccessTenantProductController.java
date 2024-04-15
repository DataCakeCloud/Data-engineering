package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.AccessTenantProduct;
import com.ushareit.dstask.service.AccessTenantProductService;
import com.ushareit.dstask.service.BaseService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Api(tags = "租户与产品管理")
@RestController
@RequestMapping("/tenantproduct")
public class AccessTenantProductController extends BaseBusinessController<AccessTenantProduct> {
    @Resource
    private AccessTenantProductService accessTenantProductService;

    @Override
    public BaseService<AccessTenantProduct> getBaseService() {
        return accessTenantProductService;
    }
}
