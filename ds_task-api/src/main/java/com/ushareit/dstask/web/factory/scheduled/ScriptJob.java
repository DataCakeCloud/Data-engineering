package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ScriptJob extends ScheduledJob {
    String image;
    ArrayList<String> files;
    String cmds;
    String batchParams;

    String commandType;
    String executeMode;



    public ScriptJob(Task task, TaskServiceImpl taskServiceImpl){
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        commandType = runtimeConfigObject.getString("commandType");
        executeMode = runtimeConfigObject.getString("executeMode");
        files = getExecFileName();
        batchParams = runtimeConfigObject.getString("batchParams");
        log.info("batchParams:"+batchParams);
        image = runtimeConfigObject.getString("image");
        cmds = runtimeConfigObject.getString("cmds");
    }


    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        taskItem.put("image_pull_policy","IfNotPresent");
        taskItem.put("namespace", taskServiceImpl.getNamespace());
        Map<String, JSON> paramMap;
        try {
            paramMap = JSON.parseObject(batchParams, Map.class);
        }catch(RuntimeException e){
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL,"高级设置-其他参数不合法，必须为json格式");
        }
        if(paramMap != null && paramMap.size() !=0){
            Iterator<Map.Entry<String, JSON>> paramMapIter = paramMap.entrySet().iterator();
            while (paramMapIter.hasNext()){
                Map.Entry<String, JSON> paramEntry = paramMapIter.next();
                taskItem.put(paramEntry.getKey(),paramEntry.getValue());
            }
        }
        taskItem.put("files",files);
        if ("local".equals(executeMode)){
            taskItem.put("command_tags", "type:local-submit");
            taskItem.put("workdir", "/neworiental/data/xstream/work");
            taskItem.put("bash_command", buildCommand());
            taskItem.put("task_type", "BashOperator");
        }else{
            taskItem.put("scripts", Arrays.asList(buildCommand()));
            taskItem.put("task_type", "ScriptOperator");
            taskItem.put("image", image);
            taskItem.put("image_pull_policy","IfNotPresent");
            taskItem.put("namespace","bdp");
        }
        taskItem.put("task_id", name);
        taskItem.put("type",commandType);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }




    public ArrayList<String> getExecFileName(){
        ArrayList<String> fileList = new ArrayList<>();
        List<ArtifactVersion> displayDependJars = task.getDisplayDependJars();
        if(displayDependJars != null && displayDependJars.size() != 0){
            for(ArtifactVersion artifactVersion:displayDependJars){
                String url = artifactVersion.getContent();
                System.out.println(url);
                fileList.add(url);
            }
        }
        return fileList;
    }

    @Override
    public String buildCommand() {
        return cmds;
    }

    @Override
    public void beforeExec(){

    }
    @Override
    public void afterExec(){

    }
}
