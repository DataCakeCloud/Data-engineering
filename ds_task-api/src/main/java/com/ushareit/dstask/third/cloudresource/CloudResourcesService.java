package com.ushareit.dstask.third.cloudresource;


import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.service.BaseService;

import java.util.List;

/**
 * 获取云资源接口
 */
public interface CloudResourcesService extends BaseService<CloudResouce> {


    String getIsHasCloudResource(Integer tenantId);

    CloudResouce getCloudResource();

    CloudResouce.DataResource getCloudResource(String regionAlias);

    CloudResouce.DataResource getDefaultRegionConfig();

    String getCloudResourceSa(String provider, String region);

    ExternalData getExternalDataById();

    String getServiceAccount();

    Integer queryCloudResourceCount();

    PageInfo<CloudResource> searchCloudResource(String tenantId);

    List<CloudResource> searchCloudResourceInTenant(String tenantId);

    CloudResouce getCacheCloudResource(String tenantId);
}
