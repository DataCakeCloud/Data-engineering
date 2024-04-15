package com.ushareit.dstask.schedule;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fengxiao
 * @date 2022/12/27
 */
@Slf4j
@Service
public class MultiServiceImpl implements MultiService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void monitor() {
        Task task = taskMapper.selectByName("test");
        log.info("current tenant is {}, task is {}", InfTraceContextHolder.get().getTenantName(), task);
    }
}
