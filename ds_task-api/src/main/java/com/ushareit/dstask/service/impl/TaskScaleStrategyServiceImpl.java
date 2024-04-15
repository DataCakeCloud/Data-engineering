package com.ushareit.dstask.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.mapper.TaskScaleStrategyMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author: xuebotao
 * @create: 2022-01-04
 */
@Slf4j
@Service
public class TaskScaleStrategyServiceImpl extends AbstractBaseServiceImpl<TaskScaleStrategy> implements TaskScaleStrategyService {

    @Resource
    private TaskScaleStrategyMapper taskScaleStrategyMapper;

    @Override
    public CrudMapper<TaskScaleStrategy> getBaseMapper() {
        return taskScaleStrategyMapper;
    }

    @Override
    public List<TaskScaleStrategy> selectByTaskId(Integer id) {
        if (id != null) {
            return taskScaleStrategyMapper.queryByTaskId(id);
        }
        return new ArrayList<>();
    }

    @Override
    public void updateStrategy(Task task) {
        String currentUserName = InfTraceContextHolder.get().getUserName();
        if (currentUserName.equals("systm") || task == null) {
            return;
        }
        RuntimeConfig runtimeConfig = JSON.parseObject(task.getRuntimeConfig(), RuntimeConfig.class);
        List<Strategy> strategyList = runtimeConfig.getStrategyList();
        TaskScaleStrategy taskScaleStrategy = new TaskScaleStrategy();
        taskScaleStrategy.setTaskId(task.getId());
        taskScaleStrategy.setDeleteStatus(0);
        List<TaskScaleStrategy> selectDbList = getBaseMapper().select(taskScaleStrategy);
        Map<String, List<TaskScaleStrategy>> taskDbMap = selectDbList.stream().collect(Collectors.groupingBy(TaskScaleStrategy::getName));
        Map<String, List<Strategy>> taskWebMap = strategyList.stream().collect(Collectors.groupingBy(Strategy::getName));
        for (TaskScaleStrategy scaleStrategy : selectDbList) {
            if (taskWebMap.get(scaleStrategy.getName()) == null) {
                //删除
                TaskScaleStrategy deleteStrategy = new TaskScaleStrategy();
                deleteStrategy.setId(scaleStrategy.getId());
                deleteStrategy.setDeleteStatus(1);
                deleteStrategy.setUpdateBy(currentUserName);
                deleteStrategy.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                getBaseMapper().updateByPrimaryKeySelective(deleteStrategy);
            }
        }
        for (Strategy strategy : strategyList) {
            List<TaskScaleStrategy> taskScaleStrategies = taskDbMap.get(strategy.getName());
            String jsonStr = JSONObject.toJSONString(strategy);
            if (taskScaleStrategies == null || taskScaleStrategies.isEmpty()) {
                //添加
                TaskScaleStrategy saveStrategy = new TaskScaleStrategy();
                saveStrategy.setTaskId(task.getId());
                saveStrategy.setName(strategy.getName());
                saveStrategy.setSpecificStrategy(jsonStr);
                saveStrategy.setCreateBy(currentUserName);
                saveStrategy.setUpdateBy(currentUserName);
                saveStrategy.setCoolingTime(strategy.getCoolingTime());
                save(saveStrategy);
                continue;
            }
            //修改
            Integer id = taskScaleStrategies.stream().findFirst().orElse(null).getId();
            TaskScaleStrategy updateStrategy = new TaskScaleStrategy();
            updateStrategy.setId(id);
            updateStrategy.setSpecificStrategy(jsonStr);
            updateStrategy.setCoolingTime(strategy.getCoolingTime());
            updateStrategy.setUpdateBy(currentUserName);
            updateStrategy.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            getBaseMapper().updateByPrimaryKeySelective(updateStrategy);
        }
    }
}
