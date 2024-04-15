package com.ushareit.dstask.web.factory.flink;

import com.ushareit.dstask.web.ddl.model.K8SClusterInfo;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2021/12/6
 */
@Slf4j
public class FlinkK8SJob {
    private KubernetesClient client;
    private String namespace;
    private Properties jobProps = new Properties();

    public FlinkK8SJob() {
        initFlinkOnK8SJob();
        namespace = getK8sNamespace();
    }

    public PodList list() {
        PodList list = client.pods().inNamespace("bdp-flink").list();
        return list;
    }

    /**
     * 提交flink on k8s任务
     *
     * @param
     * @param
     */
    public void submitFlinkCluster() {
        File dir = new File("/Users/wuyan/IdeaProjects/shareit/ds_task/k8s/");
        if (!dir.isDirectory()) {
            throw new RuntimeException("this is not dir!");
        }
        File[] files = dir.listFiles();
        List<File> collect = Arrays.stream(files).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for (File file : collect) {
            String fileContent = null;
            try {
                fileContent = FileUtils.readFileToString(file, "GBK");

                if (!file.getName().contains("1-")) {
                    fileContent = replaceYamlVars(fileContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Boolean createSuccess = createResourceFromYaml(namespace, fileContent, file.getName());
            if (!createSuccess) {
                throw new RuntimeException(String.format("create %s failed...", file.getName()));
            }
        }

    }

    public void createJob() throws FileNotFoundException {
        Job tmp = client.batch().v1().jobs().load(new FileInputStream("/Users/wuyan/IdeaProjects/shareit/ds_task/k8s/1-jobmanager-application.yaml")).get();
        Job job = client.batch().v1().jobs().inNamespace("cbs-flink").createOrReplace(tmp);
        String uid = job.getMetadata().getUid();
        System.out.println("create Job");
    }

    public void createDeployment() throws IOException {
        String fileContent = FileUtils.readFileToString(new File("/Users/wuyan/IdeaProjects/shareit/ds_task/k8s/5-taskmanager-job-deployment.yaml"), "GBK");
        jobProps.setProperty("uid", "ddffb684-46b7-4e06-aa82-82f193727r82");
        jobProps.setProperty("jmTmLabelName", "flink-wuyan");
        String newFileContent = replaceYamlVars(fileContent);
        FileUtils.writeStringToFile(new File("/Users/wuyan/IdeaProjects/shareit/ds_task/k8s/5-taskmanager-job-deployment-copy.yaml"), newFileContent, "GBK");
        Deployment tmp = client.apps().deployments().load(new FileInputStream("/Users/wuyan/IdeaProjects/shareit/ds_task/k8s/5-taskmanager-job-deployment-copy.yaml")).get();
        Deployment job = client.apps().deployments().inNamespace("cbs-flink").createOrReplace(tmp);
        List<OwnerReference> ownerReferences = job.getMetadata().getOwnerReferences();
        for (OwnerReference ownerReference : ownerReferences) {
            System.out.println(ownerReference.getUid());
        }
        System.out.println("create Job");
    }

    public void listDeployment() {
        Deployment deployment = client.apps().deployments().inNamespace("cbs-flink").withName("flink-taskmanager-wuyan").get();
        System.out.println("deployment name:" + deployment.getMetadata().getName());
        List<OwnerReference> ownerReferences = deployment.getMetadata().getOwnerReferences();
        for (OwnerReference ownerReference : ownerReferences) {
            System.out.println(ownerReference.getUid());
        }
    }

    public void autoScaleTm(Integer count) {
        Deployment scale = client.apps().deployments().inNamespace("cbs-flink").withName("flink-taskmanager-wuyan").scale(count);
        if (scale == null) {
            throw new RuntimeException("scale flink-taskmanager-wuyan failed.");
        }
        System.out.println("list");
    }

    public void listConfig() {
        ConfigMapList list = client.configMaps().inNamespace("cbs-flink").list();
        list.getItems().stream().forEach(item -> {
            System.out.println(item.getMetadata().getName());
        });
        System.out.println("list");
    }

    public void deleteConfig() {
        Boolean delete = client.configMaps().inNamespace("cbs-flink").withName("flink-config-wuyan").delete();
        if (delete) {
            System.out.println("delete config is success");
        }
        System.out.println("list");
    }

    public void deleteConfigMap() {
        Boolean delete = client.configMaps().inNamespace("cbs-flink").withName("flink-ha-185-bc5e2adbcb237426aaa00b6253b85ca6-jobmanager-leader").delete();
        if (delete) {
            System.out.println("delete config is success");
        }
        System.out.println("list");
    }

    public void deleteSvc() {
        Boolean delete = client.services().inNamespace("cbs-flink").withName("flink-jobmanager-rest-wuyan").delete();
        if (delete) {
            System.out.println("delete config is success");
        }

        Boolean delete1 = client.services().inNamespace("cbs-flink").withName("flink-jobmanager-wuyan").delete();
        if (delete1) {
            System.out.println("delete config is success");
        }

        System.out.println("list");
    }

    public void deletePod() {
        // 级联删除
        Boolean delete = client.pods().inNamespace("cbs-flink").withName("taskmanager-noparamjob-test").delete();
        if (delete) {
            System.out.println("delete job is success");
        }
        System.out.println("list");
    }

    public void deleteJob() {
        // 级联删除
        Boolean delete = client.batch().jobs().inNamespace("bdp-flink").withName("jobmanager-medusacrash-test111").cascading(true).delete();
        if (delete) {
            System.out.println("delete job is success");
        }
        System.out.println("list");
    }

    public void deleteDeployment() {
        Boolean delete = client.apps().deployments().inNamespace("cbs-flink").withName("taskmanager-noparamjob-test").delete();
        if (delete) {
            System.out.println("delete deployment is success");
        }
        System.out.println("list");
    }

    /**
     * 通过yaml文件在k8上创建资源
     *
     * @param namespace
     * @param yamlFileContent
     * @return
     */
    private Boolean createResourceFromYaml(String namespace, String yamlFileContent, String yamlName) {
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
     * 将yaml文件中的变量替换成值
     *
     * @param fileContent
     * @return
     */
    private String replaceYamlVars(String fileContent) {
        fileContent = fileContent
                .replace("${UID}", jobProps.getProperty("uid"))
                .replace("${jm_tm_label_name}", jobProps.getProperty("jmTmLabelName"));
        return fileContent;
    }

    private String getK8sNamespace() {
        return "cbs-flink";
    }

    /**
     * k8s任务初始化
     *
     * @throws UnsupportedEncodingException
     */
    private void initFlinkOnK8SJob() {
        // 初始化客户端
        k8sClientInit();
    }

    /**
     * k8s任务初始化
     */
    public void k8sClientInit() {
        K8SClusterInfo k8SClusterInfo = getK8SClusterInfo().get("sg2");
        initClient(k8SClusterInfo);
    }

    /**
     * 初始化k8s客户端
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
     * 获取k8s集群信息
     *
     * @return
     */
    public Map<String, K8SClusterInfo> getK8SClusterInfo() {

        Map<String, K8SClusterInfo> k8SClusterInfoMap = new HashMap<String, K8SClusterInfo>();


        return k8SClusterInfoMap;
    }
}
