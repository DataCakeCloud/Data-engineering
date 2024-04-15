package com.ushareit.dstask.service;


import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.ApiGateway;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.DataAudit;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.model.TableUsageProfile;

import java.util.List;
import java.util.Map;


public interface AuditService extends BaseService<ApiGateway> {

    List<Map<String, String>> uriMap(String uri, CurrentUser currentUser);
    List<Map<String, String>> moduleEvent(String source, String name, CurrentUser currentUser);
    PageInfo<ApiGateway> auditFunction(Integer pageNum, Integer pageSize, ApiGateway apiGateway, CurrentUser currentUser);
    PagedList<TableUsageProfile> auditData(DataAudit dataAudit, CurrentUser currentUser);
}
