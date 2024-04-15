package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Catalog;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.CatalogService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "catalog管理")
@RestController
@RequestMapping("/catalog")
public class CatalogController extends BaseBusinessController<Catalog>{

    @Override
    public BaseService<Catalog> getBaseService() {
        return catalogService;
    }

    @Autowired
    private CatalogService catalogService;

    @ApiOperation(value = "获取模板动态表单")
    @GetMapping("/getConfigByCode")
    public BaseResponse getConfigByCode(@Valid String code) {
        return BaseResponse.success(catalogService.getConfigByCode(code));
    }
}
