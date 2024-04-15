package com.ushareit.dstask.third.cloudresource.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.AccessUserGroup;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.configuration.DataCakeConfig;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.impl.AbstractBaseServiceImpl;
import com.ushareit.dstask.third.cloudresource.CloudResource;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.third.cloudresource.ExternalData;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * author:xuebotao
 * date:2023-04-20
 */
@Slf4j
@Service
public class CloudResourcesServiceImpl extends AbstractBaseServiceImpl<CloudResouce> implements CloudResourcesService {


    @Override
    public CrudMapper<CloudResouce> getBaseMapper() {
        return null;
    }


    /**
     * 获取用户是否注册过云资源
     *
     * @param tenantId
     * @return
     */
    public String getIsHasCloudResource(Integer tenantId) {
        Boolean currentService = DataCakeConfigUtil.getDataCakeConfig().getCurrentService();
        if (currentService) {
            Integer integer = queryCloudResourceCount();
            return integer.toString();
        }
        Map<String, String> headers = new HashMap<>(1);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
        String clusetrManagerUrl = DataCakeConfigUtil.getDataCakeServiceConfig().getCLUSTER_MANAGER_URL() + "cloud/resource/%s/queryresourcecount";

        log.info("clusetrManagerUrl url is :" + clusetrManagerUrl + " ,tenantId is :" + tenantId);
        BaseResponse response = HttpUtil.get(String.format(clusetrManagerUrl, tenantId), null, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        Integer size = jsonObject.getInteger("data");

        if (size != null && size > 0) {
            return "0";
        }
        return "1";
    }


    /**
     * 获取对应租户云资源信息
     *
     * @return
     */
    public CloudResouce getCloudResource() {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        return getCacheCloudResource(tenantId.toString());
    }

    /**
     * 获取对应区域的云资源信息
     *
     * @return
     */
    public CloudResouce.DataResource getCloudResource(String regionAlias) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        CloudResouce cacheCloudResource = getCacheCloudResource(tenantId.toString());
        if (cacheCloudResource != null) {
            Map<String, List<CloudResouce.DataResource>> collect = cacheCloudResource.getList().stream()
                    .collect(Collectors.groupingBy(CloudResouce.DataResource::getRegionAlias));
            List<CloudResouce.DataResource> dataResources = collect.get(regionAlias);
            if (!dataResources.isEmpty()) {
                return dataResources.stream().findFirst().get();
            }
        }
        throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
    }

    /**
     * 获取对应租户云资源信息
     *
     * @return
     */
    @Cacheable(cacheNames = {"cloudResource"}, key = "#tenantId +'-cloudResource")
    public CloudResouce getCacheCloudResource(String tenantId) {
        Boolean currentService = DataCakeConfigUtil.getDataCakeConfig().getCurrentService();
        if (currentService) {
            PageInfo<CloudResource> cloudResourcePageInfo = searchCloudResource(tenantId);
            List<CloudResource> list = cloudResourcePageInfo.getList();
            CloudResouce cloudResouce = new CloudResouce();
            List<CloudResouce.DataResource> collect = list.stream().map(data -> {
                CloudResouce.DataResource build = new CloudResouce.DataResource();
                build.setProvider(data.getProvider()).setName(data.getName())
                        .setTenantId(Integer.parseInt(data.getTenantId())).setRegion(data.getRegion())
                        .setRegionAlias(data.getRegionAlias()).setProviderAlias(data.getProviderAlias()).setRoleName(data.getRoleName()).setStorage(data.getStorage());
                return build;
            }).collect(Collectors.toList());
            cloudResouce.setList(collect);
            return cloudResouce;
        }
        Map<String, String> headers = new HashMap<>(1);
        headers.put("tenantId", tenantId);
        String clusetrManagerUrl = DataCakeConfigUtil.getDataCakeServiceConfig().getCLUSTER_MANAGER_URL() + "cloud/resource/search";

        BaseResponse response = HttpUtil.get(clusetrManagerUrl, null, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        CloudResouce resouce = JSON.parseObject(jsonObject.getString("data"), CloudResouce.class);
        return resouce;
    }

    /**
     * 获取sa
     * @param provider
     * @param region
     * @return
     */
    public String getCloudResourceSa(String provider, String region) {
        Boolean currentService = DataCakeConfigUtil.getDataCakeConfig().currentService;
        if (currentService) {
            return getServiceAccount();
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("provider", provider);
        paramMap.put("region", region);

        Map<String, String> headers = new HashMap<>(3);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        headers.put("tenantId", tenantId.toString());
        headers.put("tenantName", tenantName);
        String clusetrManagerUrl = DataCakeConfigUtil.getDataCakeServiceConfig().getCLUSTER_MANAGER_URL() + "cloud/resource/getServiceAccount";
        BaseResponse response = HttpUtil.get(clusetrManagerUrl, paramMap, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        String sa = (String) response.getData();
        return sa;
    }


    public CloudResouce.DataResource getDefaultRegionConfig() {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        CloudResouce cacheCloudResource = getCacheCloudResource(tenantId.toString());
        if (cacheCloudResource != null) {
            List<CloudResouce.DataResource> list = cacheCloudResource.getList();
            for (CloudResouce.DataResource dataResource : list) {
                if (dataResource.getRegionAlias().contains("sg2")) {
                    return dataResource;
                }
            }
            return list.stream().findFirst().get();
        }
        return null;
    }


    @Override
    public ExternalData getExternalDataById() {
        ExternalData build = ExternalData.builder().cloudResourceId(1L)
                .dataSource("ue1").path("").name("ue1").tenantId("1").build();
        return build;
    }


    @Override
    public Integer queryCloudResourceCount() {
        return 0;
    }

    @Override
    public String getServiceAccount() {
        DataCakeRegionProperties.RegionConfig regionConfig = DataCakeConfigUtil.getDataCakeRegionProperties().getRegionConfig().stream().findFirst().orElse(null);
        return regionConfig.getServiceAccount();
    }


    @Override
    public PageInfo<CloudResource> searchCloudResource(String tenantId) {
        List<CloudResource> cloudResources = searchCloudResourceInTenant(tenantId);
        return new PageInfo<>(cloudResources);
    }

    @Override
    public List<CloudResource> searchCloudResourceInTenant(String tenantId) {
        List<DataCakeRegionProperties.RegionConfig> regionConfig = DataCakeConfigUtil.getDataCakeRegionProperties().getRegionConfig();
        List<CloudResource> collect = regionConfig.stream().map(data -> {
            CloudResource build = CloudResource.builder()
                    .name(data.getRegionAlias())
                    .tenantId(tenantId)
                    .provider(data.getProvider())
                    .region(data.getRegion())
                    .roleName(data.getRoleName())
                    .storage(data.getStorage())
                    .type(data.getType())
                    .providerAlias(data.getProviderAlias())
                    .regionAlias(data.getRegionAlias())
                    .tenantName(data.getTenantName()).build();
            build.setDescription(data.getDescription());
            return build;
        }).collect(Collectors.toList());
        return collect;
    }
}
