package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.bean.DutyInfo;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.DutyInfoMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.DutyInfoService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2022-08-10
 */

/**
 * @author xuebotao
 * @date 2022-08-10
 */
@Service
@Slf4j
public class DutyInfoServiceImpl extends AbstractBaseServiceImpl<DutyInfo> implements DutyInfoService {

    @Resource
    private DutyInfoMapper dutyInfoMapper;

    @Resource
    private AccessUserService accessUserService;

    @Override
    public CrudMapper<DutyInfo> getBaseMapper() {
        return dutyInfoMapper;
    }


    @Override
    public String getDutyMan(String module) {

        InfTraceContextHolder.get().setTenantName(DsTaskConstant.INSIDE_SUPPER_TENANT_NAME);
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        log.debug(" tenantId is :" + tenantId);

        long utc_8Time = new Timestamp(System.currentTimeMillis()).getTime() + 28800000;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dt = format.format(utc_8Time);

        List<DutyInfo> dutyInfos = dutyInfoMapper.selectByTenant(1, module);
        List<DutyInfo> currentDutys = dutyInfos.stream().filter(data -> data.getIsDuty().equals("0"))
                .collect(Collectors.toList());
        if (currentDutys.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DUTY_NOT_INIT);
        }

        DutyInfo dutyInfo = currentDutys.stream().findFirst().get();
        if (dutyInfo.getDutyDate().equals(dt)) {
            AccessUser byId = accessUserService.getById(dutyInfo.getUserId());
            return byId.getName();
        }

        //只有一个值班人
        if (dutyInfos.size() == 1) {
            dutyInfo.setDutyDate(dt);
            update(dutyInfo);
            AccessUser byId = accessUserService.getById(dutyInfo.getUserId());
            return byId.getName();
        }

        List<DutyInfo> updateList = new ArrayList<>();
        dutyInfo.setIsDuty("1");
        dutyInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        updateList.add(dutyInfo);
        DutyInfo nextDateDutyInfo = dutyInfo;
        Integer flag = 0;
        for (int i = 0; i < dutyInfos.size(); i++) {
            if (flag == 1) {
                nextDateDutyInfo = dutyInfos.get(i);
                nextDateDutyInfo.setDutyDate(dt).setIsDuty("0");
                nextDateDutyInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                updateList.add(nextDateDutyInfo);
                break;
            }
            if (dutyInfo.getId().equals(dutyInfos.get(i).getId())) {
                if (i == dutyInfos.size() - 1) {
                    nextDateDutyInfo = dutyInfos.stream().findFirst().get();
                    nextDateDutyInfo.setDutyDate(dt).setIsDuty("0");
                    nextDateDutyInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    updateList.add(nextDateDutyInfo);
                }
                flag = flag + 1;
            }
        }

        update(updateList);
        AccessUser accessUser = accessUserService.getById(nextDateDutyInfo.getUserId());
        return accessUser.getName();
    }
}
