package com.ushareit.dstask.service;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Auditlog;

import java.util.List;


public interface AuditlogService extends BaseService<Auditlog> {


     PageInfo<Auditlog> pages(Integer pageNum, Integer pageSize, Auditlog auditlog);

     List<String> type(String module);

}