package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.DashboardBase;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.DashboardService;
import com.ushareit.dstask.third.cloudresource.AwsRegions;
import com.ushareit.dstask.third.cloudresource.CloudResource;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.third.cloudresource.ExternalData;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Api(tags = "云资源管理")
@RestController
@RequestMapping("/cloud")
public class CloudResourceController extends BaseBusinessController<CloudResouce> {


    @Autowired
    private CloudResourcesService cloudResourcesService;


    @Override
    public BaseService<CloudResouce> getBaseService() {
        return cloudResourcesService;
    }


    @ApiOperation(value = "根据ID查找挂载数据")
    @GetMapping(value = "/data/getOne")
    public BaseResponse<ExternalData> getExternalDataById(@ApiParam(value = "数据挂载id") @RequestParam("id") Integer id) {
        ExternalData externalData = cloudResourcesService.getExternalDataById();
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, externalData);
    }


    @ApiOperation(value = "根据云商查找region")
    @GetMapping(value = "/resource/getRegion")
    public BaseResponse<List<AwsRegions>> getRegionByProvider(
            @ApiParam(value = "provider", required = true) @RequestParam("provider") String provider) {
        if (StringUtils.equalsIgnoreCase(provider, "aws")) {
            return BaseResponse.success(AwsRegions.AWS_REGIONS);
        }
        return BaseResponse.success();
    }


    @ApiOperation(value = "查询serviceAccount")
    @GetMapping(value = "/resource/getServiceAccount")
    public BaseResponse<String> getServiceAccount(
            @ApiParam(value = "provider", required = true) @RequestParam("provider") String provider,
            @ApiParam(value = "region", required = true) @RequestParam("region") String region) {
        String serviceAccount = cloudResourcesService.getServiceAccount();
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, serviceAccount);
    }


    @ApiOperation(value = "查询当前租户云资源数量")
    @GetMapping(value = "/resource/{tenantId}/queryresourcecount")
    public BaseResponse<Integer> queryCloudResourceCount(
            @ApiParam(value = "租户ID", required = true) @PathVariable("tenantId") String tenantId) {
        return BaseResponse.success(cloudResourcesService.queryCloudResourceCount());
    }


    @ApiOperation(value = "检索云资源")
    @GetMapping(value = "/resource/search")
    public BaseResponse<PageInfo<CloudResource>> searchCloudResource(
            @ApiParam(value = "云资源名称") @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "云供应商") @RequestParam(value = "provider", required = false) String provider,
            @ApiParam(value = "区域") @RequestParam(value = "region", required = false) String region,
            @ApiParam(value = "桶") @RequestParam(value = "storage", required = false) String storage,
            @ApiParam(value = "页号") @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @ApiParam(value = "页大小") @RequestParam(value = "pageSize", defaultValue = "100") int pageSize,
            HttpServletRequest request) {

        String tenantId = request.getHeader("tenantId");
        PageInfo<CloudResource> cloudResourcePageInfo = cloudResourcesService.searchCloudResource(tenantId);
        return BaseResponse.success(cloudResourcePageInfo);
    }


    @ApiOperation(value = "查询当前租户的所有云资源")
    @GetMapping(value = "/resource/searchInTenant")
    public BaseResponse<List<CloudResource>> queryCloudResourceInTenant(HttpServletRequest request) {
        List<CloudResource> cloudResources = cloudResourcesService.searchCloudResourceInTenant(request.getHeader("tenantId"));
        return BaseResponse.success(cloudResources);
    }


    @ApiOperation(value = "查询当前租户的所有云资源")
    @GetMapping(value = "/cache/CloudResouce")
    public BaseResponse<CloudResouce> getCacheCloudResource() {
        CloudResouce cacheCloudResource = cloudResourcesService.getCloudResource();
        return BaseResponse.success(cacheCloudResource);
    }


}
