package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessProduct;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/13
 */
public interface AccessProductService extends BaseService<AccessProduct> {

    List<AccessProduct> getConfig(Integer userId);

    /**
     * 为租户初始化产品信息
     *
     * @param tenantName 租户名
     */
    void initProducts(String tenantName);

}
