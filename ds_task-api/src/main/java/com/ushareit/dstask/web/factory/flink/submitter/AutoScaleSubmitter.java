package com.ushareit.dstask.web.factory.flink.submitter;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.ReleaseResourceUtil;
import com.ushareit.dstask.web.ddl.model.K8SClusterInfo;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.YamlFactory;
import com.ushareit.dstask.web.factory.flink.job.FlinkSqlJob;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.kubernetes.Yaml;
import com.ushareit.dstask.web.utils.CommonUtil;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * autoscale submit task
 * @author wuyan
 * @date 2021/12/8
 */
@Slf4j
public class AutoScaleSubmitter extends BaseSubmitter {
    private KubernetesClient client;

    private String NO_ARGS_DEMO = "\"start-foreground\", \"--job-classname\", \"%s\"";

    private String SAVEPOINT_DEMO = ", \"--fromSavepoint\", \"%s\"";

    private String MAIN_ARGS_DEMO = ", %s";

    private String namespace;

    private Properties jobProps = new Properties();

    List<RuntimeConfig.Kv> params;

    protected static Map<String, K8SClusterInfo> clusterMap = new HashMap();

    private String PVC_VOLUMEMOUNTS = "\n" +
            "            - name: pvc-dir\n" +
            "              mountPath: /data/pvc";
    private String PVC_VOLUMES = "\n" +
            "        - name: pvc-dir\n" +
            "          persistentVolumeClaim:\n" +
            "            claimName: ";
    private String NODE_SELECTOR_TOLERATIONS = "\n" +
            "      nodeSelector:\n" +
            "        ${node_selector}\n" +
            "      tolerations:\n" +
            "        - effect: NoExecute\n" +
            "          key: app\n" +
            "          operator: Equal\n" +
            "          value: flink";

    static {

    }

    public AutoScaleSubmitter() {
        super();
    }

    public AutoScaleSubmitter(TaskServiceImpl taskServiceImp, Job job) {
        super(taskServiceImp, job);
        this.params = baseJob.runtimeConfig.getParams();
        initK8sClient();
        this.namespace = baseJob.cluster.getNameSpace();
        log.info("AutoScale namespace is " + namespace);
        initJobProps();
        Preconditions.checkNotNull(namespace, "namespace can not be null!");
    }

    public AutoScaleSubmitter(String namespace, String clusterAddr, String jobmanagerName) {
        super(null, null);
        initK8sClient(clusterAddr);
        this.namespace = namespace;
        log.info("AutoScale namespace is " + namespace);
        jobProps.setProperty("jobmanagerName", jobmanagerName);
        Preconditions.checkNotNull(namespace, "namespace can not be null!");
    }

    public KubernetesClient getClient() {
        return this.client;
    }

    @Override
    public void submit() {
        try {
            deleteConfigMaps();
            submitYaml();
        } catch (Exception e) {
            log.error("AutoScaleSubmitter: " + jobProps.getProperty("jobmanagerName") + " submitAsync error:", e);
            processException();
        }finally {
            log.info("close k8s client!");
            ReleaseResourceUtil.close(client);
        }
    }

    @Override
    public String getFlinkUiDns(String appName, String region) {
        return MessageFormat.format("http://{0}.{1}.flink.ushareit.org", appName, region);
    }

    @Override
    public void processException() {
        log.info("AutoScaleSubmitter processException");
        updateTaskAndInstance();
        deleteResource();
    }

    @Override
    public void deleteResource() {
        log.info("AutoScaleSubmitter deleteResource begin, jobmanagerName is " + jobProps.getProperty("jobmanagerName"));
        // 级联删除
        Boolean delete = client.batch().jobs().inNamespace(namespace).withName(jobProps.getProperty("jobmanagerName")).cascading(true).delete();
        if (!delete) {
            log.info("AutoScaleSubmitter deleteResource failed, jobmanagerName is " + jobProps.getProperty("jobmanagerName"));
            return;
        }
        log.info("AutoScaleSubmitter deleteResource success, jobmanagerName is " + jobProps.getProperty("jobmanagerName"));
    }

    private void deleteConfigMaps() {
        String jmConfigMapName = jobProps.getProperty("hAClusterName") + "-" + DsTaskConstant.DEFAULT_JOB_ID + "-jobmanager-leader";
        log.info("jmConfigMapName is " + jmConfigMapName);
        Boolean jmDelete = client.configMaps().inNamespace(namespace).withName(jmConfigMapName).delete();
        Boolean dispatcherDelete = client.configMaps().inNamespace(namespace).withName(jobProps.getProperty("hAClusterName") + "-dispatcher-leader").delete();
        Boolean rmDelete = client.configMaps().inNamespace(namespace).withName(jobProps.getProperty("hAClusterName") + "-resourcemanager-leader").delete();
        Boolean rsDelete = client.configMaps().inNamespace(namespace).withName(jobProps.getProperty("hAClusterName") + "-restserver-leader").delete();

        if (!jmDelete) {
            log.info("AutoScaleSubmitter deletejm failed, jm is " + jobProps.getProperty("hAClusterName"));
//            throw new ServiceException(BaseResponseCodeEnum.DELETE_FAIL, "delete autoscale jm is failed");
        }

        if (!dispatcherDelete) {
            log.info("AutoScaleSubmitter delete dispatcher failed, hAClusterName is " + jobProps.getProperty("hAClusterName"));
//            throw new ServiceException(BaseResponseCodeEnum.DELETE_FAIL, "delete autoscale dispatcher is failed");
        }

        if (!rmDelete) {
            log.info("AutoScaleSubmitter delete rm failed, hAClusterName is " + jobProps.getProperty("hAClusterName"));
//            throw new ServiceException(BaseResponseCodeEnum.DELETE_FAIL, "delete autoscale rm is failed");
        }

        if (!rsDelete) {
            log.info("AutoScaleSubmitter delete rs failed, hAClusterName is " + jobProps.getProperty("hAClusterName"));
//            throw new ServiceException(BaseResponseCodeEnum.DELETE_FAIL, "delete autoscale rs is failed");
        }
        log.info("AutoScaleSubmitter deleteConfigMap success, hAClusterName is " + jmConfigMapName);
    }

    @Override
    public void autoScaleTm(Integer count) {
        try {
            String taskmanagerName = jobProps.getProperty("taskmanagerName");
            log.info("taskmanagerName is " + taskmanagerName + ", auto scale count is " + count);
            client.apps().deployments().inNamespace(namespace).withName(jobProps.getProperty("taskmanagerName")).scale(count);
        } catch (Exception e) {
            log.error("scale flink-taskmanager-wuyan failed.");
            throw new RuntimeException(CommonUtil.printStackTraceToString(e));
        } finally {
            log.info("autoScaleTm client closed!");
            ReleaseResourceUtil.close(client);
        }
    }

    @Override
    public Integer getTmNum() {
        String taskmanagerName = jobProps.getProperty("taskmanagerName");
        try {
            Integer num = client.apps().deployments().inNamespace(namespace).withName(taskmanagerName).get().getSpec().getReplicas();
            log.info("taskmanagerName is " + taskmanagerName + ", tm num is " + num);
            return num;
        } catch (Exception e) {
            log.error("get tm pod num of taskmanagerName is " + taskmanagerName + ", failed!");
            throw new RuntimeException(CommonUtil.printStackTraceToString(e));
        } finally {
            log.info("get tm pod num client closed!");
            ReleaseResourceUtil.close(client);
        }
    }

    private void submitYaml() throws IOException {
       //File dir = new File("/Users/lcg/workspace_shareit/ds_task/ds_task-api/k8s");
        File dir = new File(DsTaskConstant.K8S_DIR);
        if (!dir.isDirectory()) {
            throw new RuntimeException("this is not dir!");
        }
        File[] files = dir.listFiles();
        List<File> collect = Arrays.stream(files).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for (File file : collect) {
            String fileContent = FileUtils.readFileToString(file, "GBK");
            Yaml yaml = YamlFactory.getYaml(file.getName(), jobProps, params);
            fileContent = yaml.replaceYamlVars(fileContent);
            log.info("----------------------------:"+fileContent);
            Boolean createSuccess = createResourceFromYaml(fileContent, file.getName());
            if (!createSuccess) {
                throw new RuntimeException(String.format("create %s failed...", file.getName()));
            }
        }
    }

    private void initJobProps() {
        String name = baseJob.runTimeTaskBase.getName().toLowerCase();
        jobProps.setProperty("clusterAddr", baseJob.cluster.getAddress());

        setInitContainerDependJars();

//        jobProps.setProperty("hAClusterName", "flink-ha-" + baseJob.task.getId() + "-" + UuidUtil.getUuid32());
        jobProps.setProperty("hAClusterName", "flink-ha-" + baseJob.task.getId());
        jobProps.setProperty("jobmanagerName", "jobmanager-" + name);
        jobProps.setProperty("taskmanagerName", "taskmanager-" + name);
        jobProps.setProperty("jmTmLabelName", name);
        jobProps.setProperty("namespace", namespace);
        jobProps.setProperty("owner", baseJob.task.getCreateBy());
        jobProps.setProperty("template", baseJob.task.getTemplateCode());
        jobProps.setProperty("id", baseJob.task.getId().toString());

        setJmArgs();

        if (baseJob.cluster != null) {
            jobProps.setProperty("region", baseJob.cluster.getRegion());
            jobProps.setProperty("env", baseJob.cluster.getEnv());
            // native模式和yaml模式，nodeSelector格式不同
            jobProps.setProperty("nodeSelector", baseJob.cluster.getNodeSelector().replace(":", ": "));
            jobProps.setProperty("tolerations", baseJob.cluster.getTolerations());
            jobProps.setProperty("checkpointDir", baseJob.cluster.getStatePath() + baseJob.cluster.getEnv() + "/" + baseJob.task.getId() + DsTaskConstant.CHECKPOINT_PATH);
            jobProps.setProperty("savepointDir", baseJob.cluster.getStatePath() + baseJob.cluster.getEnv() + "/" + baseJob.task.getId() + DsTaskConstant.SAVEPOINT_PATH);
            jobProps.setProperty("haStorageDir", baseJob.cluster.getStatePath() + DsTaskConstant.HA_PATH);
            jobProps.setProperty("flinkImage", baseJob.cluster.getContainerImage());
        }

        jobProps.setProperty("flinkConfigVolume", "flink-config-volume-" + name);
        jobProps.setProperty("flinkConfigName", "flink-config-" + name);
        jobProps.setProperty("restServiceName",  name);
        Double tmCpu = baseJob.runtimeConfig.getTmCpu();
        Double tmMemory = baseJob.runtimeConfig.getTmMemory();
        long tmCpuCon = Math.round(tmCpu == null ? 1 : tmCpu);
        long tmMemoryCon = Math.round(tmMemory == null ? 4 : tmMemory);
        jobProps.setProperty("tmCpu", String.valueOf(tmCpuCon));
        jobProps.setProperty("tmMemory", tmMemoryCon + "Gi");
//        jobProps.setProperty("tmCpu", tmCpu.toString());
//        jobProps.setProperty("tmMemory", tmMemory.toString() + "Gi");
        jobProps.setProperty("parallelism", baseJob.runtimeConfig.getParallelism().toString());

        String flinkExecutionPackages = StringUtils.isEmpty(baseJob.flinkExecutionPackages) ? "" : baseJob.flinkExecutionPackages;
        jobProps.setProperty("flinkExecutionPackages", flinkExecutionPackages);

        List<RuntimeConfig.Kv> params = baseJob.runtimeConfig.getParams();
        jobProps.setProperty("pvc_volumeMounts","");
        jobProps.setProperty("pvc_volumes","");
        for (RuntimeConfig.Kv kv:params) {
            if (kv.key.equalsIgnoreCase("k8s_pvc")){
                jobProps.setProperty("pvc_volumeMounts",PVC_VOLUMEMOUNTS);
                jobProps.setProperty("pvc_volumes",PVC_VOLUMES + kv.value);
                break;
            }
        }
        if (DsTaskConstant.PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv()) ||
               DsTaskConstant.CLOUD_PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv()) ){
            jobProps.setProperty("nodeSelector_tolerations", NODE_SELECTOR_TOLERATIONS.replace("${node_selector}",jobProps.getProperty("nodeSelector")));
        }else{
            jobProps.setProperty("nodeSelector_tolerations","");
        }
    }

    private void setInitContainerDependJars() {
        StringBuffer dependJars = new StringBuffer();
        if (!StringUtils.isEmpty(baseJob.runTimeTaskBase.getJarUrl())) {
            dependJars.append(baseJob.runTimeTaskBase.getJarUrl());

        }

        if (StringUtils.isNotEmpty(baseJob.onlineUdfJarObsUrl)) {
            dependJars.append(",").append(baseJob.onlineUdfJarObsUrl);
        }

        if (StringUtils.isNotEmpty(baseJob.artifactUdfJarObsUrl)) {
            dependJars.append(",").append(baseJob.artifactUdfJarObsUrl);
        }

        jobProps.setProperty("initContainerDependJars", dependJars.toString());
    }

    private void setJmArgs() {
        String mainClassArgs = baseJob.runTimeTaskBase.getMainClassArgs();
        if (job instanceof FlinkSqlJob) {
            mainClassArgs = ((FlinkSqlJob) job).getSqlJobArgsForAutoScale();
        }

        if (StringUtils.isEmpty(baseJob.runTimeTaskBase.getMainClass())) {
            return;
        }

        String jmArgs = String.format(NO_ARGS_DEMO, baseJob.runTimeTaskBase.getMainClass());
        if (!StringUtils.isEmpty(baseJob.savepointUrl)) {
            String savepointUrl = String.format(SAVEPOINT_DEMO, baseJob.savepointUrl);
            jmArgs = jmArgs + savepointUrl;
        }

        if (!StringUtils.isEmpty(mainClassArgs)) {
            String[] split = mainClassArgs.split("\\s");
            String args = Arrays.asList(split).stream().map(e -> "\"" + e + "\"").collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
            jmArgs = jmArgs + String.format(MAIN_ARGS_DEMO, args);
        }

        jobProps.setProperty("jmArgs", jmArgs);
        log.info("initJobProps jmArgs:" + jmArgs);
    }

    /**
     * create k8s resources through yaml file
     * @param yamlFileContent
     * @return
     */
    private Boolean createResourceFromYaml(String yamlFileContent, String yamlName) {
        log.info(String.format("create %s Start...", yamlName));
        Boolean createSuccess = true;
        try {
            List<HasMetadata> result = client.load(new ByteArrayInputStream(yamlFileContent.getBytes())).get();
            List<HasMetadata> createResult = client.resourceList(result).inNamespace(namespace).createOrReplace();
            if ("1-jobmanager-application.yaml".equals(yamlName)) {
                String uid = createResult.get(0).getMetadata().getUid();
                jobProps.setProperty("uid", uid);
            }
            log.info("k8s create response: " + createResult.toString());
        } catch (Throwable e) {
            createSuccess = false;
            log.error(String.format("create %s error...", yamlName), e);
        }

        log.info(String.format("create %s ...end. result: %s", yamlName, createSuccess));
        return createSuccess;
    }

    /**
     * init k8s client
     */
    private void initK8sClient() {
        K8SClusterInfo k8SClusterInfo = getK8SClusterInfo(baseJob.cluster.getAddress());
        initClient(k8SClusterInfo);
    }

    private void initK8sClient(String clusterAddr) {
        K8SClusterInfo k8SClusterInfo = getK8SClusterInfo(clusterAddr);
        initClient(k8SClusterInfo);
    }

    /**
     * init k8s client
     *
     * @param k8SClusterInfo
     * @return
     */
    private KubernetesClient initClient(K8SClusterInfo k8SClusterInfo) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.withMasterUrl(k8SClusterInfo.getHost());
        configBuilder.withTrustCerts(true);
        configBuilder.withOauthToken(k8SClusterInfo.getToken());
        configBuilder.withCaCertData(k8SClusterInfo.getCaCrt());
        client = new DefaultKubernetesClient(configBuilder.build());
        return client;
    }

    /**
     * get k8s cluster info
     *
     * @return
     */
    public K8SClusterInfo getK8SClusterInfo(String clusterAddr) {
        return clusterMap.get(clusterAddr);
    }
}
