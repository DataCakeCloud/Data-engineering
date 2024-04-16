package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuyan
 * @date 2021/12/29
 */
@Slf4j
public class FlinkBatchSqlJob extends FlinkSqlJob {
    public FlinkBatchSqlJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }
}
