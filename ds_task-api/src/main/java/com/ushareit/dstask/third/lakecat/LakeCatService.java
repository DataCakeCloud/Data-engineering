package com.ushareit.dstask.third.lakecat;

/**
 * @author fengxiao
 * @date 2023/2/14
 */
public interface LakeCatService {

    /**
     * 创建租户
     *
     * @param tenantName 租户名
     */
    void createTenant(String tenantName);

     void checkTablePri(String user, String sourceRegion, String sourceDB, String sourceTable, String privilege);

}
