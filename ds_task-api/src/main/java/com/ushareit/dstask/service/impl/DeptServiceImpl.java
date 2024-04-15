package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.DeptInfo;
import com.ushareit.dstask.mapper.DeptMapper;
import com.ushareit.dstask.mapper.TaskInstanceMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.DeptService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.trace.holder.InfWebContext;
import com.ushareit.dstask.web.utils.ItUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class DeptServiceImpl extends AbstractBaseServiceImpl<DeptInfo> implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private TaskInstanceMapper taskInstanceMapper;

    @Override
    public CrudMapper<DeptInfo> getBaseMapper() {
        return deptMapper;
    }

    @Override
    public List<DeptInfo> getDepartmentsList() {
        ItUtil itUtil = new ItUtil();
        return itUtil.getDepartmentsList();
    }

    @Override
    public List<DeptInfo> getDeptInfo(String shareId) {
        ItUtil itUtil = new ItUtil();
        return itUtil.getDeptInfo(shareId);

    }

    @Override
    public List<DeptInfo> getEffectiveDeptList(String name) {
        boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        List<DeptInfo> listInfo = deptMapper.select(DeptInfo.builder().isEffectiveCost(1).build());
        if (isPrivate) {
            return listInfo;
        }
        List<DeptInfo> userDeptInfo = getDeptInfo(name);
        AtomicReference<DeptInfo> isMainDeptInfo = new AtomicReference<>();
        if (userDeptInfo != null && !userDeptInfo.isEmpty()) {
            userDeptInfo.stream().forEach(data -> {
                if (data.getIsMain().equals("1")) {
                    isMainDeptInfo.set(data);
                }
            });
        }
        if (isMainDeptInfo.get() != null) {
            List<String> result = Arrays.asList(isMainDeptInfo.get().getOrganizationPath().split("#"));
            for (DeptInfo deptInfo : listInfo) {
                if (result.contains(deptInfo.getOrganizationName())) {
                    deptInfo.setIsDefault(1);
                }
            }
        }
        return listInfo;
    }
}