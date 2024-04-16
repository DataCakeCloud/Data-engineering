package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

@Slf4j
public class SparkLocalJob extends ScheduledJob{

    private final boolean isSql;

    ArrayList<String> files;
    private String encodeSql;
    private String typeCode;

    public SparkLocalJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        typeCode = task.getTypeCode();
        files = getExecFileName();
        isSql = !"SPARKJAR".equals(templateCode) && !"MergeSmallFiles".equals(templateCode);
        if(isSql){
            try{
                encodeSql = encodeFlagDatacake(task.getContent());
            }catch(Exception e){
                throw new ServiceException(BaseResponseCodeEnum.SQL_PARSE_EXCEPTION);
            }
        }
    }

    @Override
    public void beforeExec() throws Exception {

    }

    @Override
    public void afterExec() {

    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        taskItem.put("files",files);
        taskItem.put("command_tags", "type:local-submit");
        taskItem.put("workdir", "/work");
        taskItem.put("bash_command", buildCommand());
        taskItem.put("task_type", "BashOperator");
        taskItem.put("task_id", name);
        taskItem.put("type","shell");
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
    protected String buildCommand() {
        String userGroupName = InfTraceContextHolder.get().getCurrentGroup();
        String sparkSubmit = "$SPARK_HOME/bin/spark-submit";
        sparkSubmit += String.format(" --conf spark.sql.default.dbName=%s " +
                        "--conf spark.master=yarn " +
                        "--conf spark.driver.extraJavaOptions=-Dfile.encoding=UTF-8 "+
                        "--conf spark.kerberos.principal=%s@HADOOP.COM " +
                        "--conf spark.kerberos.keytab=/data-kfs/home/%s/.keytab/%s.keytab ",
                this.defaultDb,userGroupName,userGroupName,userGroupName);
        if(isSql){
            sparkSubmit+=sparkConfig;
            sparkSubmit+=String.format(" --class com.ushareit.sql.SparkSubmitSql " +
                    "/data-kfs/soft/spark/spark-submit-sql-1.0-SNAPSHOT.jar -e \"%s\"",encodeSql);
        }else{
            if ("ARTIFACT".equals(typeCode)) {
                System.out.println(task.getJarUrl());
                sparkConfig = sparkConfig.replace(task.getJarUrl(), "");
                if (task.getDependArtifacts() != null && !task.getDependArtifacts().isEmpty()) {
                    List <ArtifactVersion> displayDependJars = task.getDisplayDependJars();
                    String url = displayDependJars.get(0).getContent();
                    log.info("工件文件：" + url);
                    String file = transformFile(url);
                    sparkConfig += String.format(" %s ", file);
                }
            }
            String args = task.getMainClassArgs();
            sparkSubmit += sparkConfig + args;
        }
        System.out.println(sparkSubmit);
        return sparkSubmit;
    }

    private String transformFile(String file) {
        String transformedFile;
        if (file.contains(".amazonaws.com")) {
            Pattern awsPattern = Pattern.compile(DsTaskConstant.AWS_ADDRESS_PATTERN);
            Matcher awsMatcher = awsPattern.matcher(file);
            if (awsMatcher.find()) {
                transformedFile = String.format("s3://%s", awsMatcher.group(2) + "/" + awsMatcher.group(5) + "/" + awsMatcher.group(6));
            } else {
                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_MODE_NOT_MATCH, "工件格式不匹配");
            }
        } else if (file.contains("myhuaweicloud.com")) {
            Pattern obsPattern = Pattern.compile(DsTaskConstant.OBS_ADDRESS_PATTERN);
            Matcher obsMatcher = obsPattern.matcher(file);
            if (obsMatcher.find()) {
                transformedFile = String.format("obs://%s", obsMatcher.group(2) + "/" + obsMatcher.group(4) + "/" + obsMatcher.group(5));
            } else {
                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_MODE_NOT_MATCH, "工件格式不匹配");
            }
        } else if (file.contains(".ksyuncs.com")){
            Pattern obsPattern = Pattern.compile(DsTaskConstant.KS3_ADDRESS_PATTERN);
            Matcher obsMatcher = obsPattern.matcher(file);
            if (obsMatcher.find()) {
                transformedFile = String.format("ks3://%s", obsMatcher.group(2) + "/" + obsMatcher.group(5) + "/" + obsMatcher.group(6));
            } else {
                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_MODE_NOT_MATCH, "工件格式不匹配");
            }
        }else {
            Pattern gcsPattern = Pattern.compile(DsTaskConstant.GCS_ADDRESS_PATTERN);
            Matcher gcsMatcher = gcsPattern.matcher(file);
            if (gcsMatcher.find()) {
                transformedFile = String.format("gs://%s", gcsMatcher.group(2) + "/" + gcsMatcher.group(3));
            } else {
                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_MODE_NOT_MATCH, "工件格式不匹配");
            }
        }
        return transformedFile;
    }
}
