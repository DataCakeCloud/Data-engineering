package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessGroupMapper;
import com.ushareit.dstask.mapper.CostAllocationMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author xuebotao
 * @date 2022/12/02
 */
@Slf4j
@Service
public class CostAllocationServiceImpl extends AbstractBaseServiceImpl<CostAllocation> implements CostAllocationService {
    @Resource
    private CostAllocationMapper costAllocationMapper;

    @Resource
    private AccessGroupService accessGroupService;

    @Override
    public CrudMapper<CostAllocation> getBaseMapper() {
        return costAllocationMapper;
    }


    @Override
    public void updateCostAllocation(Task task) {

        String userName = InfTraceContextHolder.get().getUserName();

        JSONObject jsonObject = JSON.parseObject(task.getRuntimeConfig());
        List<RuntimeConfig.Cost> costList = new ArrayList<>();
        try {
            JSONArray cost = jsonObject.getJSONArray("cost");
            costList = cost.toJavaList(RuntimeConfig.Cost.class);
        } catch (Exception e) {
            return;
        }

        CostAllocation costAllocation = new CostAllocation();

        costAllocation.setTaskId(task.getId());
        costAllocation.setDeleteStatus(0);
        List<CostAllocation> selectDbList = getBaseMapper().select(costAllocation);

        Map<Integer, List<CostAllocation>> taskDbMap = selectDbList.stream().collect(Collectors.groupingBy(CostAllocation::getGroupId));


        Map<Integer, List<RuntimeConfig.Cost>> taskWebMap = costList.stream().collect(Collectors.groupingBy(data -> Integer.parseInt(data.getKey())));
        for (CostAllocation ca : selectDbList) {
            if (taskWebMap.get(ca.getGroupId()) == null) {
                //删除
                CostAllocation deleteCostAllocation = new CostAllocation();
                deleteCostAllocation.setId(ca.getId());
                deleteCostAllocation.setDeleteStatus(1);
                deleteCostAllocation.setUpdateBy(userName);
                deleteCostAllocation.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                getBaseMapper().updateByPrimaryKeySelective(deleteCostAllocation);
            }
        }
        for (RuntimeConfig.Cost cost : costList) {
            List<CostAllocation> CostAllocations = taskDbMap.get(Integer.parseInt(cost.getKey()));
            BigDecimal value = new BigDecimal(cost.getValue());
            BigDecimal dividend = new BigDecimal("100");
            BigDecimal result = value.divide(dividend, 2, BigDecimal.ROUND_HALF_UP);
            if (CostAllocations == null || CostAllocations.isEmpty()) {
                //添加
                CostAllocation saveCostAllocation = new CostAllocation();
                saveCostAllocation.setTaskId(task.getId())
                        .setGroupId(Integer.parseInt(cost.getKey()))
                        .setValue(result)
                        .setCreateBy(userName)
                        .setUpdateBy(userName)
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                        .setCreateTime(new Timestamp(System.currentTimeMillis()));
                save(saveCostAllocation);
                continue;
            }
            //修改
            Integer id = CostAllocations.stream().findFirst().orElse(null).getId();
            CostAllocation updateCostAllocation = new CostAllocation();
            updateCostAllocation.setId(id);
            updateCostAllocation.setGroupId(Integer.parseInt(cost.getKey())).setValue(result)
                    .setUpdateBy(userName)
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            getBaseMapper().updateByPrimaryKeySelective(updateCostAllocation);
        }

    }

    @Override
    public List<CostAllocation> selectByTaskId(Integer taskId) {
        CostAllocation allocation = new CostAllocation();
        allocation.setTaskId(taskId).setDeleteStatus(0);

        List<CostAllocation> selectList = getBaseMapper().select(allocation);
        List<Integer> ids = selectList.stream().map(CostAllocation::getGroupId).collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> collect = accessGroupService.listByIds(ids).stream()
                .collect(Collectors.groupingBy(AccessGroup::getId));

        for (CostAllocation ca : selectList) {
            if (ca.getGroupId() != null) {
                ca.setName(collect.get(ca.getGroupId()).stream().findFirst().get().getName());
            }
        }
        return selectList;
    }

}
