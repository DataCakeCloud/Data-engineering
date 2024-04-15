package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ushareit.dstask.DsTaskApplication;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.configuration.AwsConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.AwsClientUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class TfJob extends ScheduledJob{
    private String dependencies;
    private String fileName = task.getName() + ".yaml";
    private String executeCommand;
    private String codeAddr;
    private String cpu;
    private String memory;
    private String gpu;
    private String paramFile;

    public TfJob(Task task, TaskServiceImpl taskServiceImpl){
        super(task, taskServiceImpl);
        String runtimeConfig = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        cpu = runtimeConfigObject.getString("cpu");
        gpu = runtimeConfigObject.getString("gpu");
        memory = runtimeConfigObject.getString("memory");
        codeAddr = runtimeConfigObject.getString("codeAddr");
        paramFile = runtimeConfigObject.getOrDefault("paramFile","").toString();
        executeCommand = runtimeConfigObject.getString("executeCommand");
    }

    String yaml = "apiVersion: kubeflow.org/v1\n" +
            "kind: TFJob\n" +
            "metadata:\n" +
            "  creationTimestamp: null\n" +
            "  labels:\n" +
            "    app: tf-operator\n" +
            "    env: %s\n" +
            "    group: %s\n" +
            "  name: %s\n" +
            "  namespace: kubeflow\n" +
            "spec:\n" +
            "  runPolicy: {}\n" +
            "  tfReplicaSpecs:\n" +
            "    Worker:\n" +
            "      replicas: 1\n" +
            "      restartPolicy: Never\n" +
            "      template:\n" +
            "        metadata:\n" +
            "          annotations:\n" +
            "            sidecar.istio.io/inject: \"false\"\n" +
            "          creationTimestamp: null\n" +
            "        spec:\n" +
            "          containers:\n" +
            "          - command:\n" +
            "            - bash\n" +
            "            - -c\n" +
            "            - %s\n" +
            "            image: 848318613114.dkr.ecr.ap-southeast-1.amazonaws.com/shareit-sprs/tensorflow:1.15.5-gpu-py3-jupyter-ext-tcmalloc\n" +
            "            imagePullPolicy: Always\n" +
            "            name: tensorflow\n" +
            "            resources:\n" +
            "              limits:\n" +
            "                cpu: \"%s\"\n" +
            "                memory: %sGi\n" +
            "                nvidia.com/gpu: \"%s\"\n" +
            "              requests:\n" +
            "                cpu: \"%s\"\n" +
            "                memory: %sGi\n" +
            "                nvidia.com/gpu: \"%s\"\n" +
            "            volumeMounts:\n" +
            "            - mountPath: /data\n" +
            "              name: data\n" +
            "          imagePullSecrets:\n" +
            "          - name: default-secret\n" +
            "          nodeSelector:\n" +
            "            project: kubeflow\n" +
            "          tolerations:\n" +
            "          - effect: NoExecute\n" +
            "            key: project\n" +
            "            operator: Equal\n" +
            "            value: kubeflow\n" +
            "          volumes:\n" +
            "          - hostPath:\n" +
            "              path: /data\n" +
            "            name: data\n";


    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        uploadYamlFile();
        HashMap<String, Object> taskItem = new HashMap<>();
        taskItems.add(taskItem);
        String clusterTags;
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, dataResource.getRegion(), clusterSLA, dataResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        taskItem.put("command_tags", "type:tf-operator-cli");
        taskItem.put("task_type", "GenieJobOperator");
        taskItem.put("dependencies",dependencies);
        taskItem.put("command", buildCommand());
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }

    public void uploadYamlFile(){
        try {
            String command = "rm -rf /data/* && bash /usr/bin/init_code.sh %s %s /data && cd /data/ && source platform_task_conf.sh && %s $extargs";
            String scriptCommand = String.format(command,codeAddr,paramFile,executeCommand);
            yaml = String.format(yaml,env,group,task.getName().replaceAll("_","-"),scriptCommand,cpu,memory,gpu,cpu,memory,gpu);
            log.info(yaml);
            String path = String.format("/tmp/%s",fileName);
            File file = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(yaml.getBytes());
            fileOutputStream.close();
            String key = String.format("hebe/test/yaml/%s",fileName);
//            String transformedRegion = RegionEnum.regionEnumMap.get(region).getRegion();
//            String bucketName = RegionEnum.regionEnumMap.get(region).getBucket();
            AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
            AmazonS3 client = AwsClientUtil.getClient(awsConfig);
            client.putObject(new PutObjectRequest("cbs.flink.us-east-1", key, new File(path)));
            dependencies = String.format("s3://%s/%s","cbs.flink.us-east-1",key);
//            if ("sg2".equals(region)){
//                ObsClient obsClient = new ObsClient(new EnvironmentVariableObsCredentialsProvider(), HuaweiConstant.END_POINT);
//                obsClient.putObject(bucketName,key,new File(path));
//                dependencies = String.format("obs://%s/%s",bucketName,key);
//            }else{
//                AmazonS3 client = AwsClientUtil.getClient(transformedRegion);
//                client.putObject(new PutObjectRequest(bucketName, key, new File(path)));
//                dependencies = String.format("s3://%s/%s",bucketName,key);
//            }
            log.info("yaml文件上传成功！");
        }catch (Exception e){
            throw new ServiceException(BaseResponseCodeEnum.FIlE_UPLOAD_FAIL);
        }

    }

    @Override
    protected String buildCommand() {
        return String.format("--job-config %s --container-name tensorflow --extargs {{(execution_date + macros.timedelta(days=-1)).strftime('%%Y%%m%%d')}}",fileName);
    }

    @Override
    public void beforeExec() throws Exception {


    }

    @Override
    public void afterExec() {

    }
}
