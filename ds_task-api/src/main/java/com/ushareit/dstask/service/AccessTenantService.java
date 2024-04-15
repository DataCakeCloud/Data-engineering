package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/7
 */
public interface AccessTenantService extends BaseService<AccessTenant> {

    void freeze(Integer id, Integer freeze);

    AccessTenant checkExist(Integer id);

    void config(Integer id, String productIds);

    AccessTenant current(Integer tenantId);

    AkSk getAkSk(CurrentUser currentUser);

    AkSk updateAkSk(AkSkRequest akskRequest, CurrentUser currentUser);

    String generateAkSkToken(AkSkTokenRequest akSkTokenRequest) throws UnsupportedEncodingException;

    String generateAkSkPersonalToken(AkSkTokenRequest akSkTokenRequest, CurrentUser currentUser) throws UnsupportedEncodingException;

    AkSkResponse getAkSkPersonalToken(CurrentUser currentUser);

    Map<String, Object> getUserInfo(String token);

    /**
     * 获取未被冻结的租户列表
     *
     * @return 租户列表
     */
    List<AccessTenant> getActiveList();

    /**
     * 创建数据库
     *
     * @param tenantName 租户名
     */
    void initDatabases(String tenantName,String sqlFlile);

    void insertAdminUser(AccessTenant webAccessTenant , AccessTenant dbAccessTenant);
}
