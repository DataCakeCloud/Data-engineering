package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.third.sharestore.ShareStoreService;
import com.ushareit.dstask.utils.SpringUtil;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.ParseParamUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RocksDbETLJob extends ScheduledJob{

    private String restEndpoint;
    private String zkStr;
    private String clusterLoad;
    private Integer rateLimitMb;
    private String sourceTable;
    private String segmentLoad;
    private String partitions;
    private String input;
    private boolean isHive;

    private String inputPath;
    private String outputPath = "";
    private String acrossCloud;


    public RocksDbETLJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        String s3ObsPath = runtimeConfigObject.getString("s3ObsPath");
        acrossCloud = runtimeConfigObject.getOrDefault("acrossCloud","common").toString().trim();
        if (s3ObsPath.endsWith("/")){
            input = s3ObsPath + runtimeConfigObject.getString("partitions");
        }else{
            input = s3ObsPath + "/" +runtimeConfigObject.getString("partitions");
        }
        isHive = runtimeConfigObject.getBoolean("isHive");
        restEndpoint = runtimeConfigObject.getString("restEndpoint");
        zkStr = runtimeConfigObject.getString("zkStr");
        clusterLoad = runtimeConfigObject.getString("clusterLoad");
        rateLimitMb = (Integer) runtimeConfigObject.getOrDefault("rateLimitMb", 64);
        sourceTable = runtimeConfigObject.getOrDefault("sourceDb", "") + "." + runtimeConfigObject.getOrDefault("sourceTable", "");
        segmentLoad = runtimeConfigObject.getString("segmentLoad");
        long nowTime = +System.currentTimeMillis();

        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        outputPath = cloudResource.getStorage() + cloudResource.getPath() + "datastudio/" + "temporary/sharestore/" + clusterLoad + "/" + segmentLoad + "/" + "{{ (execution_date).strftime(\"%Y%m%d%H\") }}" + nowTime;
        provider = cloudResource.getProvider();

        //inputPath = getInputPath(runtimeConfigObject.getString("location"));
        partitions = ((String) runtimeConfigObject.getOrDefault("partitions", "")).replaceAll(" ", "");
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        HashMap<String, String> regionEnum = new HashMap<>();
        for (CloudResouce.DataResource regionConfig : cloudResource.getList()) {
            if (regionConfig.getRegionAlias().contains("sg2")) {
                regionEnum.put(regionConfig.getRegionAlias(), String.format("obs.%s.myhuaweicloud.com", regionConfig.getRegion()));
            } else {
                regionEnum.put(regionConfig.getRegionAlias(), regionConfig.getRegion());
            }
        }

        HashMap<String, Object> taskItem1 = new HashMap<>();
        taskItems.add(taskItem1);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem1.put("cluster_tags", clusterTags);
        taskItem1.put("command_tags", "type:spark-submit-ds");
        taskItem1.put("task_type", "GenieJobOperator");
        taskItem1.put("command", buildCommand());
        taskItem1.put("task_id", name);

        HashMap<String, Object> taskItem2 = new HashMap<>();
        taskItems.add(taskItem2);
        taskItem2.put("cluster_tags", clusterTags);
        taskItem2.put("command_tags", "type:kubernetes-pod-operator");
        taskItem2.put("task_type", "ImportSharestoreOperator");
        taskItem2.put("cluster_load", clusterLoad);
        taskItem2.put("loadsst_version", "load_sst_v2");
        taskItem2.put("segment_load", segmentLoad);
        taskItem2.put("input_path", outputPath);
        taskItem2.put("region", regionEnum.get(region));
        taskItem2.put("load_mode", "0");
        taskItem2.put("rest_endpoint", restEndpoint);
        taskItem2.put("rate_limit_mb", rateLimitMb);
        taskItem2.put("zk_str", zkStr);
        taskItem2.put("pool", clusterLoad);
        taskItem2.put("task_id", String.format("%s_import_sharestore", name));
        TemplateRegionImp templateRegionImp =
                taskServiceImpl.templateRegionImpService.selectOne(new TemplateRegionImp().setTemplateCode(templateCode).setRegionCode(region));
        String image = templateRegionImp.getImage();
        taskItem2.put("image", image);
        taskItem2.put("namespace", "bdp");
        taskItem2.put("name", segmentLoad.replaceAll("_", "-").toLowerCase());

        ArrayList<String> upstreams = new ArrayList<>();
        upstreams.add(name);
        taskItem2.put("upstream_tasks", upstreams);
        return taskItems;
    }

    public Integer getPartitionNum(){
        JSONObject runtimeConfigObject = JSON.parseObject(task.getRuntimeConfig());
        String batchParams = runtimeConfigObject.getString("batchParams");
        String parsedParam = ParseParamUtil.formatParam(batchParams);
        String[] strs = parsedParam.split("\\s+");

        int partitions = 32;
        ShareStoreService shareStoreService = SpringUtil.getBean(ShareStoreService.class);
        try {
            partitions = shareStoreService.getPartitionNum(restEndpoint, clusterLoad, segmentLoad);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        for (String str : strs) {
            if(str.contains("--partitions_num")) {
                partitions = Integer.parseInt(str.split("=")[1].trim());
            }
        }
        return partitions;
    }

    @Override
    protected String buildCommand() {
        String args;
        if(isHive){
            args = String.format("--table_name %s ",sourceTable);
        }else{
            args = String.format("--input %s ",input);
        }
        args += String.format("--output %s --partition_num %d --segment %s --partitions %s ", outputPath, getPartitionNum(),segmentLoad,partitions);
        return sparkConfig + args;
    }


    @Override
    public void beforeExec() {

    }

    @Override
    public void afterExec() {

    }
}
