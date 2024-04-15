package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.OperateLog;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.constant.DsIndicatorsEnum;
import com.ushareit.dstask.mapper.OperateLogMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.OperateLogService;
import com.ushareit.dstask.web.utils.ItUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
@Service
public class OperateLogServiceImpl extends AbstractBaseServiceImpl<OperateLog> implements OperateLogService {

    @Resource
    private OperateLogMapper operateLogMapper;

    @Override
    public CrudMapper<OperateLog> getBaseMapper() {
        return operateLogMapper;
    }

    @Async
    @Override
    public Object save(OperateLog operateLog) {
        return super.save(operateLog);
    }

    @Override
    public Map<String, Integer> getIndicators() {
        Map<String, Integer> result = new HashMap<>();

        Example errorExample = new Example(OperateLog.class);
        errorExample.or()
                .andGreaterThanOrEqualTo("requestTime", Date.from(LocalDate.now().atStartOfDay()
                        .toInstant(ZoneOffset.of("+8"))))
                .andNotEqualTo("resultCode", '0');

        result.put(DsIndicatorsEnum.FAULT_COUNT.name(), operateLogMapper.selectCountByExample(errorExample));
        result.put(DsIndicatorsEnum.INTERFACE_AGG_RESPONSE_TIME.name(), Double.valueOf(operateLogMapper.getDailyAverageCostTime() * 100)
                .intValue());

        return result;
    }

    @Override
    public List<Map<String, Integer>> getDayUsers() {
        List<Map<String, Integer>> dayUsers = operateLogMapper.getDayUsers();
        return dayUsers;
    }

    @Override
    public Map<String, Integer> getWeekUsers(Timestamp start, Timestamp end) {
        Integer weekUsers = operateLogMapper.getWeekUsers(start, end);
        Map<String, Integer> map = new HashMap<>();
        map.put("weekUsers", weekUsers);
        return map;
    }


    public Map<String, Integer> getCumulativeUsers() {
        List<String> cumulativeUsers = operateLogMapper.getCumulativeUsers();
        Map<String, Integer> map = new HashMap<>();
        ItUtil itUtil = new ItUtil();
        List<UserBase> userBases = itUtil.batchGetUsersInfo(cumulativeUsers);
        Set<String> userSet = new HashSet<>();
        for (UserBase user : userBases) {
            userSet.add(user.getShareId());
        }
        for (String id : cumulativeUsers) {
            if (!userSet.contains(id)) {
                UserBase user = new UserBase();
                user.setShareId(id);
                user.setStatus("8");
                userBases.add(user);
            }
        }
        List<UserBase> result = userBases.stream().map(data -> {
            if (data.getStatus().equals("8")) {
                data.setStatus(DsIndicatorsEnum.ACC_DEPARTURE_USER_COUNT.name());
                return data;
            }
            data.setStatus(DsIndicatorsEnum.ACC_USER_COUNT.name());
            return data;
        }).collect(Collectors.toList());
        Map<String, List<UserBase>> collect = result.stream().collect(Collectors.groupingBy(UserBase::getStatus));
        int ACC_USER_COUNT = collect.get(DsIndicatorsEnum.ACC_USER_COUNT.name()) == null ? 0 : collect.get(DsIndicatorsEnum.ACC_USER_COUNT.name()).size();
        int ACC_DEPARTURE_USER_COUNT = collect.get(DsIndicatorsEnum.ACC_DEPARTURE_USER_COUNT.name()) == null ? 0 : collect.get(DsIndicatorsEnum.ACC_DEPARTURE_USER_COUNT.name()).size();
        map.put(DsIndicatorsEnum.ACC_USER_COUNT.name(), ACC_USER_COUNT);
        map.put(DsIndicatorsEnum.ACC_DEPARTURE_USER_COUNT.name(), ACC_DEPARTURE_USER_COUNT);
        return map;
    }
}
