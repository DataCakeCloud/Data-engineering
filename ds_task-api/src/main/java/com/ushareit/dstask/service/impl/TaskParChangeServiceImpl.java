package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.TaskParChange;
import com.ushareit.dstask.mapper.TaskParChangeMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.TaskParChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;



/**
 * @author: xuebotao
 * @create: 2022-01-12
 */
@Slf4j
@Service
public class TaskParChangeServiceImpl extends AbstractBaseServiceImpl<TaskParChange> implements TaskParChangeService {

    @Resource
    private TaskParChangeMapper taskParChangeMapper;

    @Override
    public CrudMapper<TaskParChange> getBaseMapper() {
        return taskParChangeMapper;
    }

    @Override
    public void insertTaskParChange(TaskParChange taskParChange) {
        taskParChangeMapper.insert(taskParChange);
    }
}
