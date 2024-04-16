package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.DeptInfo;

import java.util.List;

public interface DeptService extends BaseService<DeptInfo> {

    List<DeptInfo> getDepartmentsList();

    List<DeptInfo> getDeptInfo(String shareId);

    List<DeptInfo> getEffectiveDeptList(String userId);

}