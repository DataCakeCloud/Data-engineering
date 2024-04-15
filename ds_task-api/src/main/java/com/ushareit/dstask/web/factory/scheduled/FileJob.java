package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class FileJob extends ScheduledJob{

    private String email;
    private String subject;
    private String sql;
    private String text;
    private String targetType;
    private String format;
    private EmailUtils emailUtils;



    public FileJob(Task task, TaskServiceImpl taskServiceImpl){
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        email = runtimeConfigObject.getString("email");
        subject = new String(Base64.getEncoder().encode(runtimeConfigObject.getString("subject").getBytes()));
        sql = task.getContent();
        text = new String(Base64.getEncoder().encode(runtimeConfigObject.getString("text").getBytes()));
        targetType = runtimeConfigObject.getString("targetType");
        format = runtimeConfigObject.getString("format");
        emailUtils = taskServiceImpl.getEmailUtils();
    }




    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:yarn,region:%s,provider:%s", dataResource.getRegion(), dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,provider:%s", acrossCloud, dataResource.getRegion(), dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:spark-submit-ds");
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }



    @Override
    public String buildCommand() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("from",emailUtils.getFrom());
        paramMap.put("to",email);
        paramMap.put("from_password",emailUtils.getPassword());
        paramMap.put("subject",subject);
        paramMap.put("sql",encodeFlag(sql));
        paramMap.put("text",text);
        paramMap.put("targetType",targetType);
        paramMap.put("format",format);
        String json = JSON.toJSONString(paramMap, SerializerFeature.DisableCircularReferenceDetect);
        String s = json.replaceAll("\"", "\\\\\"");//.replaceAll("/转译双引号/", "\\\\\"");
        s = s.replaceAll("},","}, "); // 兼容genie
        return String.format("--conf spark.sql.default.dbName=%s ",defaultDb) + sparkConfig + "\"" + s + "\"";
    }


    @Override
    public void beforeExec() throws Exception {

    }

    @Override
    public void afterExec() {

    }
}
