package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ETLJob extends ScheduledJob {
    private final String CHECK_FILE_COMMAND_DEMO = "fs -mkdir -p %s || echo \"File already exists !\" && hadoop fs -touchz %s/%s";
    private final boolean isSql;
    private String sql;
    private String typeCode;
    private boolean isCreateTable = false;

    public ETLJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        isSql = !"SPARKJAR".equals(templateCode) && !"MergeSmallFiles".equals(templateCode);
        typeCode = task.getTypeCode();
        if (isSql) {
            try{
                String decodesql = URLDecoder.decode(new String(Base64.getDecoder().decode(task.getContent().getBytes())), "UTF-8");
                Pattern pattern = Pattern.compile("(?i)CREATE\\s+EXTERNAL\\s+TABLE|CREATE\\s+TABLE");
                Matcher matcher = pattern.matcher(decodesql);
                if(matcher.find()){
                    isCreateTable = true;
                    log.info("存在建表语句");
                }
            }catch(Exception e){
                throw new ServiceException(BaseResponseCodeEnum.SQL_PARSE_EXCEPTION);
            }
            sql = encodeFlag(task.getContent());
            /*if (sqlStr != null && !sqlStr.isEmpty()) {
                sql = Arrays.stream(sqlStr.split("\n"))
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining(" "))
                        .replaceAll("\\\\\"", "/转译双引号/")
                        .replaceAll("\"", "'")
                        .replaceAll("/转译双引号/", "\\\\\"");
            }*/
        }
    }

    @Override
    protected List <Map <String, Object>> buildTaskItems() {
        HashMap <String, Object> taskItem = new HashMap <>();
        taskItems.add(taskItem);
        String runtimeConfig = task.getRuntimeConfig();
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:yarn,region:%s,provider:%s", cloudResource.getRegion(), cloudResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,provider:%s", acrossCloud, cloudResource.getRegion(), cloudResource.getProvider());
        }

        if (isSql) {
            if(sqlEngine.equals("hive")){
                taskItem.put("command_tags", "type:hive");
                taskItem.put("task_type", "KyuubiHiveOperator");
                clusterTags = String.format("region:%s,provider:%s", cloudResource.getRegion(), cloudResource.getProvider());

            }else{
                taskItem.put("command_tags", "type:spark-submit-sql-ds");
                taskItem.put("task_type", "KyuubiOperator");
            }
        } else {
            taskItem.put("command_tags", "type:spark-submit-ds");
            taskItem.put("task_type", "KyuubiOperator");
        }
        taskItem.put("cluster_tags", clusterTags);

        if (commandTags != null && commandTags.length() > 0) {
            taskItem.put("command_tags", commandTags);
        }
        if (this.clusterTags != null && this.clusterTags.length() > 0) {
            taskItem.put("cluster_tags", this.clusterTags);
        }
        if (dependencies != null && dependencies.length() > 0) {
            taskItem.put("dependencies", dependencies);
        }
        taskItem.put("command", buildCommand());
//        addCheckFileTask(taskItem);
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }

    private void addCheckFileTask(HashMap <String, Object> preTask) {
        if (outputDatasets == null || outputDatasets.size() == 0) {
            return;
        }
        for (Dataset dataSet : outputDatasets) {
            if (StringUtils.isNotEmpty(dataSet.getLocation())) {
                HashMap <String, Object> taskItem = new HashMap <>();
                taskItem.put("task_id", "createCheckFile");
                taskItem.put("genie_conn_id", "dev-datastudio-big-authority");
                taskItem.put("command", String.format(CHECK_FILE_COMMAND_DEMO, dataSet.getLocation(), dataSet.getLocation(), dataSet.getFileName()));
                taskItem.put("cluster_tags", preTask.get("cluster_tags"));
                taskItem.put("command_tags", "type:hadoop");
                taskItem.put("task_type", preTask.get("task_type"));
                taskItem.put("upstream_tasks", new String[]{name});
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
        String args;
        if (isSql) {
            if(sqlEngine.equals("hive")){
                return defaultDb + sql;
            }else{
                args = String.format("-e \"%s\"", sql);
                if(isCreateTable && !"".equals(owner) && owner != null){
                    sparkConfig += String.format("--conf bdp-query-provider=%s ",owner);
                }
                sparkConfig += String.format("--conf spark.sql.default.dbName=%s ",defaultDb);
                return sparkConfig + args;
            }

        } else {
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
            args = task.getMainClassArgs();
            return sparkConfig + args;
        }
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
        }else if(file.contains(".aliyuncs.com")){
            Pattern ossPattern = Pattern.compile(DsTaskConstant.OSS_ADDRESS_PATTERN);
            Matcher ossMatcher = ossPattern.matcher(file);
            if (ossMatcher.find()) {
                transformedFile = String.format("oss://%s", ossMatcher.group(2) + "/" + ossMatcher.group(5) + "/" + ossMatcher.group(6));
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

    @Override
    public void beforeExec() {

    }

    @Override
    public void afterExec() {

    }
}
