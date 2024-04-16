package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.flink.job.FlinkBaseJob;
import lombok.extern.slf4j.Slf4j;


/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
public class FlinkJarJob extends FlinkBaseJob {
    public FlinkJarJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    @Override
    public void beforeExec() throws Exception {
        if (!runTimeTaskBase.getTypeCode().equals(DsTaskConstant.ARTIFACT_TYPE_ONLINE)){
            runTimeTaskBase.setJarUrl(runTimeTaskBase.getDisplayDependJars().get(0).getContent());
        }
        super.beforeExec();
    }

}
