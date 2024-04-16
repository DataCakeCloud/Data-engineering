package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessTenantProduct;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
public interface AccessTenantProductService extends BaseService<AccessTenantProduct>{
    List<AccessTenantProduct> getByTenantId(Integer id);

    void deleteByTenantId(Integer tenantId);

    void insertList(List<AccessTenantProduct> accessTenantProductList);
}
