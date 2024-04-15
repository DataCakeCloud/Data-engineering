package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.obs.services.EnvironmentVariableObsCredentialsProvider;
import com.obs.services.ObsClient;
import com.ushareit.dstask.DsTaskApplication;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.configuration.AwsConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.HuaweiConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.AwsClientUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CopyFileJob extends ScheduledJob{

    private Boolean isAcrossCloud;
    private String sourcePath;
    private String targetPath;
    private String targetRegion;
    private String filtersContext;
    private Boolean deleteMode;
    private String filtersFile;
    private String filters;

    public CopyFileJob(Task task, TaskServiceImpl taskServiceImpl){
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        sourcePath = runtimeConfigObject.getString("sourcePath");
        targetPath = runtimeConfigObject.getString("targetPath");
        filtersFile = runtimeConfigObject.getString("filtersFile");
        deleteMode = runtimeConfigObject.getBoolean("deleteMode");
        targetRegion = runtimeConfigObject.getString("targetRegion");
        isAcrossCloud = (region.equals("sg2") || this.targetRegion.equals("sg2")) && (!region.equals(this.targetRegion));
        if (filtersFile != null && !filtersFile.equals("")){
            uploadCommand(String.format("hebe/prod/re/%s.txt",task.getName()),filtersFile);
        }
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if (isAcrossCloud) {
            clusterTags = String.format("type:migration");
        } else {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:spark-submit-ds");
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("command", buildCommand());
        return taskItems;
    }

    @Override
    StringBuilder setTemplateRegionImp(StringBuilder config) {
        TemplateRegionImp copyFileJob =
                taskServiceImpl.templateRegionImpService.selectOne(new TemplateRegionImp().setTemplateCode("CopyFileJob").setRegionCode(region));
        this.mainClass = copyFileJob.getMainClass();
        this.jarUrl = copyFileJob.getUrl();
        TemplateRegionImp templateRegionImp =
                taskServiceImpl.templateRegionImpService.selectOne(new TemplateRegionImp().setTemplateCode(templateCode).setRegionCode(region));
        String image = templateRegionImp.getImage();
        config.append(String.format("--conf spark.kubernetes.container.image=%s ", image));
        config.append(String.format("--class %s ", mainClass));
        config.append(jarUrl).append(" ");
        return config;
    }

    @Override
    protected String buildCommand() {
        String acrossCloudTag = "False";
        if(isAcrossCloud){
            acrossCloudTag = "True";
            sparkConfig = sparkConfig.replaceAll("--conf spark.kubernetes.container.image=\\S+","--conf spark.kubernetes.container.image=swr.ap-southeast-3.myhuaweicloud.com/shareit-bdp/spark:2.4.3.14-rc4-hadoop-3.1.1-mrs-10 ");
        }
        String command = String.format("--acrossCloud %s %s %s  ap-southeast-1 ",acrossCloudTag,sourcePath,targetPath);
        if(deleteMode){
            command += "--overwrite --delete ";
        }else{
            command += "--update ";
        }
        if (filtersFile != null && !filtersFile.equals("")){
            command += "--filters "+filters;
        }
        return sparkConfig + command;
    }

    public void uploadCommand(String fileName,String command){
        try {
            CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
            InputStream inputStreamRoute = new ByteArrayInputStream(command.getBytes());
            String transformedRegion = cloudResource.getRegion();
            String bucketName = cloudResource.getBucket();
            if("sg2".equals(region)){
                ObsClient obsClient = new ObsClient(new EnvironmentVariableObsCredentialsProvider(), HuaweiConstant.END_POINT);
                obsClient.putObject(bucketName, fileName, inputStreamRoute);
                filters = String.format("obs://%s/%s", bucketName,fileName);
            }else{
                AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
                AmazonS3 client = AwsClientUtil.getClient(awsConfig);
                client.putObject(bucketName, fileName, inputStreamRoute, new ObjectMetadata());
                filters = String.format("s3://%s/%s", bucketName,fileName);
            }
            log.info("命令文件上传成功!");
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.FIlE_UPLOAD_FAIL);
        }
    }

    @Override
    public void beforeExec() throws Exception {

    }

    @Override
    public void afterExec() {

    }
}
