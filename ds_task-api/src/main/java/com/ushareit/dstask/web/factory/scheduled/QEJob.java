package com.ushareit.dstask.web.factory.scheduled;

import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QEJob extends ScheduledJob{
    private String sql;

    public QEJob(Task task, TaskServiceImpl taskServiceImpl){
        super(task, taskServiceImpl);
        sql = task.getContent();
    }
    @Override
    public void beforeExec() throws Exception {

    }

    @Override
    public void afterExec() {

    }

    @Override
    protected List <Map <String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap <>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:qe");
        taskItem.put("task_type", "DatacakeQeOperator");
        taskItem.put("command", buildCommand());
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }
    @Override
    protected String buildCommand() {
        return "/*datacakebianma*/" + sql + "/*datacakebianma*/";
    }
}
