package com.ushareit.dstask.web.factory.flink.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.AbstractJob;
import com.ushareit.dstask.web.factory.flink.FlinkRunCommand;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import com.ushareit.dstask.web.utils.CompileUtils;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.MavenUtil;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.FileUtils;
import org.springframework.mock.web.MockMultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Data
public class FlinkBaseJob extends AbstractJob {
    public  TaskServiceImpl taskServiceImp;

    public Task task;

    public Integer tagId;

    public Integer savepointId;

    public String savepointUrl;

    public TaskBase runTimeTaskBase;

    public FlinkCluster cluster;

    public File localMainJar;

    public Properties confProperties;

    public FlinkRunCommand command;

    /**
     * 在线代码 上传obs后的地址
     */
    public String onlineUdfJarObsUrl;

    public String artifactUdfJarObsUrl;

    public String flinkExecutionPackages;

    public List<String> execArgs;

    public TaskInstance taskInstance;

    public RuntimeConfig runtimeConfig;

    public List<Dataset> inputDatasets;
    public List<Dataset> outputDatasets;

    public List<EventDepend> eventDepends;
    public TriggerParam triggerParam;
    public String dependTypes;
    /**
     * 所有的udf:jar + online
     */
    public Set<URL> allOnlineDependJars = new HashSet();

    /**
     * sql中所有的maven依赖
     */
    public Set<URL> localMavenJars = new HashSet();
    /**
     * sql中所有的udf依赖
     */
    public Set<URL> localDependJars = new HashSet();

    /**
     * 云客户端
     */
    public CloudBaseClientUtil cloudClientUtil;

    public String webUi;

    public String submitUUid;

    public FlinkBaseJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        this.task = task;
        this.tagId = tagId;
        this.savepointId = savepointId;
        this.taskServiceImp = taskServiceImp;
        //1、获取历史版本
        if (tagId != null && tagId != -1){
            runTimeTaskBase = taskServiceImp.taskVersionService.getById(tagId);
        } else {
            runTimeTaskBase = task.clone();
        }

        //2、参数预处理
        if (StringUtils.isBlank(runTimeTaskBase.getRuntimeConfig())){
            runtimeConfig = new RuntimeConfig();
        } else {
            runtimeConfig = JSON.parseObject(runTimeTaskBase.getRuntimeConfig(), RuntimeConfig.class);
        }

        //3、输入输出数据集处理
        if (StringUtils.isNotEmpty(runTimeTaskBase.getInputDataset())) {
            inputDatasets = JSON.parseArray(runTimeTaskBase.getInputDataset(), Dataset.class);
        }
        if (StringUtils.isNotEmpty(runTimeTaskBase.getOutputDataset())) {
            outputDatasets = JSON.parseArray(runTimeTaskBase.getOutputDataset(), Dataset.class);
        }


        if (!runtimeConfig.getIsBatchTask()) {
            return;
        }
        if (StringUtils.isNotEmpty(runTimeTaskBase.getEventDepends())) {
            eventDepends = JSON.parseArray(runTimeTaskBase.getEventDepends(), EventDepend.class);
        }

        if (StringUtils.isNotEmpty(runTimeTaskBase.getTriggerParam())) {
            triggerParam = JSON.parseObject(runTimeTaskBase.getTriggerParam(), TriggerParam.class);
        }

        if(StringUtils.isNotEmpty(runTimeTaskBase.getDependTypes())){
            dependTypes =  runTimeTaskBase.getDependTypes().trim().replaceAll("\\[|\\]|\"","");
        }else{
            dependTypes = "";
        }

    }



    protected Task getTask() {
        return task;
    }

    @Override
    public void beforeExec() throws Exception {
        //4、根据配置匹配集群
        cluster = taskServiceImp.flinkClusterService.getById(runTimeTaskBase.getFlinkClusterId());

        //5、工件预处理
        dealJarUdf(runTimeTaskBase.getDisplayDependJars());

        dealOnlineUdf(runTimeTaskBase.getDisplayDependJars());

        //6、savepoint预处理
        if (savepointId != null && savepointId != -1) {
            savepointUrl = taskServiceImp.taskSnapshotService.getById(savepointId).getUrl();
        } else {
            savepointId = 0;
        }

        if (tagId == null ){
            tagId = 0;
        }
    }

    @Override
    public void beforeCheck() throws Exception {
        new CompileUtils().addLocalCompileDependJar(DsTaskConstant.FLINK_LIB_DIR,DsTaskConstant.FLINK_OPT_DIR);

        //1、 mainJar预处理
//        localMainJar = new File(ObsClientUtil.download(runTimeTaskBase.getJarUrl()));

        cloudClientUtil = taskServiceImp.cloudFactory.getCloudClientUtilByUrl(runTimeTaskBase.getJarUrl());
        localMainJar = new File(cloudClientUtil.download(runTimeTaskBase.getJarUrl()));

        //2、maven dependence预处理，无需删除
        if (StringUtils.isNotBlank(flinkExecutionPackages)) {
            localMavenJars.addAll(taskServiceImp.mavenUtil.downloadDependency(flinkExecutionPackages));
        }

        //3、依赖下载 artifactUdfJarObsUrl -> localUdfJars
        if (StringUtils.isNotBlank(artifactUdfJarObsUrl)){
            for (String uri:artifactUdfJarObsUrl.split(",")) {
                cloudClientUtil = taskServiceImp.cloudFactory.getCloudClientUtilByUrl(uri);
                localDependJars.add(new File(cloudClientUtil.download(uri)).toURI().toURL());
            }
        }
    }

    @Override
    public void afterCheck() throws Exception {
        try {
            if (localMainJar != null && localMainJar.exists()){
                FileUtils.deleteFileOrDirectory(localMainJar);
            }

            if (localDependJars != null || localDependJars.size() > 0) {
                for (URL localMavenJar : localDependJars) {
                    File file = new File(localMavenJar.toURI());
                    if (!file.exists()) {
                        continue;
                    }
                    FileUtils.deleteFileOrDirectory(file);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void afterExec() {
        deleteOnlineUdfJarObsUrl();
    }

    private void dealOnlineUdf(List<ArtifactVersion> dependJars) throws MalformedURLException {
        if (dependJars == null || dependJars.size() == 0){
            return;
        }
        List<ArtifactVersion> udfOnlineList = dependJars.stream().filter(artifactVersion -> DsTaskConstant.ARTIFACT_TYPE_ONLINE.equals(artifactVersion.getModeCode()))
                .collect(Collectors.toList());
        if (udfOnlineList.size() == 0){
            return;
        }

        //在线代码 本地文件
        File onlineUdfLocalJar = CompileUtils.doCompileAndPackage(udfOnlineList, getClasspath(DsTaskConstant.FLINK_LIB_DIR));

        CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
        cloudClientUtil = taskServiceImp.cloudFactory.getCloudClientUtil(defaultRegionConfig.getRegionAlias());
        onlineUdfJarObsUrl = cloudClientUtil.upload(jarFileConvertMultipartFile(onlineUdfLocalJar), defaultRegionConfig.getRegionAlias());
        log.info("onlineUdfJarObsUrl=" + onlineUdfJarObsUrl);

        artifactUdfJarObsUrl = artifactUdfJarObsUrl.isEmpty() ? onlineUdfJarObsUrl : artifactUdfJarObsUrl + "," + onlineUdfJarObsUrl;
    }

    private void dealJarUdf(List<ArtifactVersion> dependJars) {
        if (dependJars == null || dependJars.size() == 0  || TemplateEnum.valueOf(task.getTemplateCode()).equals(TemplateEnum.StreamingJAR)){
            return;
        }

        artifactUdfJarObsUrl = dependJars.stream().filter(artifactVersion -> DsTaskConstant.ARTIFACT_MODE_UPLOAD.equals(artifactVersion.getModeCode()))
                .map(artifactVersion -> artifactVersion.getContent()).collect(Collectors.joining(","));
        log.info("artifactUdfJarObsUrl=" + artifactUdfJarObsUrl);
    }

    private String getClasspath(String flinkLibDir) {
        StringBuffer sb = new StringBuffer();
        for (String path : new String[]{flinkLibDir}) {
            getClasspath(path, sb);
        }
        return sb.toString();
    }

    private void getClasspath(String path, StringBuffer sb) {
        File sourceFile = new File(path);
        if (sourceFile.exists() && sourceFile.isDirectory()) {
            File[] childrenFiles = sourceFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            for (File file : childrenFiles) {
                sb.append(file.getAbsolutePath()).append(":");
            }
        }
    }

    private MockMultipartFile jarFileConvertMultipartFile(File file) {
        MockMultipartFile mmf = null;
        try {
            mmf = new MockMultipartFile("jarfile", file.getName(), "text/plain", new FileInputStream(file));
        } catch (IOException e) {
            log.info(e.getMessage(), e);
        }
        return mmf;
    }



    private String getFlinkExecutionDelJar(String flinkExecutionPackages) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        if (StringUtils.isEmpty(flinkExecutionPackages)) {
            return stringBuilder.toString();
        }
        String[] split = flinkExecutionPackages.split(",");
        for (String oneJar : split) {
            Example example = new Example(AccessProduct.class);
            String[] jarArray = oneJar.split(":");
            Example.Criteria criteria = example.or();
            criteria.andEqualTo("region", cluster.getRegion());
            criteria.andEqualTo("groupId", jarArray[0].trim());
            criteria.andEqualTo("artifactId", jarArray[1].trim());
            criteria.andEqualTo("version", jarArray[2].trim());
            List<DependentInformation> dependentInformations = taskServiceImp.dependentInformationService.listByExample(example);
            if (!dependentInformations.isEmpty()) {
                stringBuilder.append(dependentInformations.stream().findFirst().orElse(null).getStorageLocation());
                stringBuilder.append(",");
            }
        }
        String res = stringBuilder.toString();
        if (res.endsWith(",")) {
            return res.substring(0, res.length() - 1);
        }
        return res;
    }

    void setGroupIdAndTenantId(TaskServiceImpl taskServiceImpl) {

    }


    public String getFlinkExecutionPackages(String sqlContent) throws UnsupportedEncodingException {
        if (sqlContent == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(DsTaskConstant.FLINK_SQL_SET_PATTERN);
        String[] sqlArr = URLDecoder.decode(new String(Base64.getDecoder().decode(sqlContent.getBytes())), "UTF-8").replaceAll("\r", "\n").replaceAll("\r\n", "\n").split("\n");
        StringBuffer sb = new StringBuffer();
        for (String line : Arrays.asList(sqlArr)) {
            if (line.trim().isEmpty() || line.trim().startsWith("--") || line.trim().startsWith("//")) {
                // skip empty line and comment line
                continue;
            }
            sb.append(line);
            if (line.endsWith(";")) {
                Matcher matcher = pattern.matcher(sb.toString());
                if (matcher.find()) {
                    String res = matcher.group(3);
                    return res.replaceAll("'", "").trim();
                }
                sb.setLength(0);
            }

        }
        return null;
    }

    private void deleteOnlineUdfJarObsUrl() {
        // 只删除udf和主jar
        try {
            if (onlineUdfJarObsUrl != null) {

                cloudClientUtil =taskServiceImp.cloudFactory.getCloudClientUtilByUrl(onlineUdfJarObsUrl);
                cloudClientUtil.delete(onlineUdfJarObsUrl);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void checkRegion(String region) {
        if (StringUtils.isEmpty(region)) {
            throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_REGION_IS_NULL);
        }
    }

}
