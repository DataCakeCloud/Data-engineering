package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.DashboardBase;

import java.util.Map;

public interface DashboardService extends BaseService<DashboardBase> {
    Map<String,Object> getDashboardUrl(DashboardBase dashboard);

    void addPermission();
}
