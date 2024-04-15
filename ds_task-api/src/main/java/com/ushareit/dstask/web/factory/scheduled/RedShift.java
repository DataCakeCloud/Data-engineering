package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class RedShift extends ScheduledJob{
    private final String CHECK_FILE_COMMAND_DEMO= "fs -mkdir -p %s || echo \"File already exists !\" && hadoop fs -touchz %s/%s";
    private String redshiftAddr;
    private String redshiftPort;
    private String redshiftUser;
    private String redshiftDatabase;
    private String content;
    private String batchParams;
    private String acrossCloud;

    public RedShift(Task task, TaskServiceImpl taskService){
        super(task,taskService);
        String runtimeConfig = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        acrossCloud = runtimeConfigObject.getOrDefault("acrossCloud","common").toString().trim();
        redshiftAddr = runtimeConfigObject.getString("redshiftAddr");
        redshiftPort = runtimeConfigObject.getString("redshiftPort");
        redshiftUser = runtimeConfigObject.getString("redshiftUser");
        redshiftDatabase = runtimeConfigObject.getString("redshiftDatabase");
        batchParams = runtimeConfigObject.getString("batchParams").replaceAll("\n"," ");
        content = encodeFlag(task.getContent());
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:psql");
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
//        addCheckFileTask(taskItem);
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }

    private void addCheckFileTask(HashMap<String, Object> preTask) {
        if (outputDatasets == null || outputDatasets.size() == 0) {
            return;
        }
        for (Dataset dataSet : outputDatasets) {
            if (StringUtils.isNotEmpty(dataSet.getLocation())) {
                HashMap<String, Object> taskItem = new HashMap<>();
                taskItem.put("task_id","createCheckFile");
                taskItem.put("genie_conn_id","dev-datastudio-big-authority");
                taskItem.put("command", String.format(CHECK_FILE_COMMAND_DEMO,dataSet.getLocation(),dataSet.getLocation(),dataSet.getFileName()));
                taskItem.put("cluster_tags",preTask.get("cluster_tags"));
                taskItem.put("command_tags","type:hadoop");
                taskItem.put("task_type",preTask.get("task_type"));
                taskItem.put("upstream_tasks",new String[]{name});
                log.info("addCheckFileTask taskItem:task_id=createCheckFile,command="
                        + taskItem.get("command")
                        + ",cluster_tags=" + taskItem.get("cluster_tags")
                        + ",command_tags=" + taskItem.get("command_tags")
                        + ",task_type=" + taskItem.get("task_type")
                        + ",upstream_tasks=" + taskItem.get("upstream_tasks"));
                taskItems.add(taskItem);
                break;
            }
        }
    }


    @Override
    protected String buildCommand() {
        return String.format("-h %s -p %s -U %s -d %s %s -c \"%s\"",
                redshiftAddr,redshiftPort,redshiftUser,redshiftDatabase,batchParams,content);
    }

    @Override
    public void beforeExec() throws Exception {

    }

    @Override
    public void afterExec() {

    }
}
