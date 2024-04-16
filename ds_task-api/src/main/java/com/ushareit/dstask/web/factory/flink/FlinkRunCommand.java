package com.ushareit.dstask.web.factory.flink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.ScmpUtil;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import javax.persistence.Entity;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.ushareit.dstask.constant.FlinkExecModeEnum.K8S;
import static com.ushareit.dstask.constant.FlinkExecModeEnum.YARN;

/**
 * @author: licg
 * @create: 2020-05-12 15:24
 **/
@AllArgsConstructor
@Data
@Entity
@Builder
@NoArgsConstructor
@Accessors(chain = true)
@Slf4j
public class FlinkRunCommand {

    public TaskServiceImpl taskServiceimpl;

    private static final String RUN = "/kubernetes_application.sh";
    private static final String REG = "{{(execution_date).strftime(\"%Y%m%dt%H\")}}";
    private String homePath;
    /**
     * [OPTIONS]
     */
    private String cluster;
    private String mode;
    private String region;
    private String appName;
    private Integer appId;
    private String mainClass;
    private String args;
    private String containerImage;
    /**
     * 主jar
     */
    private String dependJars;
    /**
     * 主jar的local地址
     */
    private String mainJar;
    private String fromSavepoint;
    private Integer parallelism;
    private String context;
    private String huaweiIam;
    private String awsIam;
    private String statePath;
    private String zookeeperQuorum;
    private String flinkExecutionPackages;
    private String dependentInformationJar;
    public List<String> execArgs;
    private Double tmCpu;
    private long tmMemory;
    private List<RuntimeConfig.Kv> params;
    private String env;
    private String namespace;
    private String nodeSelector;
    private String tolerations;
    private Boolean isBatchTask;
    private String owner;
    private String template;
    private Integer tenantId;
    private Integer groupId;
    private Integer id;
    private String gatewayUrl;
    private RuntimeConfig runtimeConfig;
    private String version;

    public String getCommandLine() {
        switch (FlinkExecModeEnum.valueOf(mode)) {
            case YARN:
                return getYarnCommandLine();
            case K8S:
                return getK8sCommandLineNew();
            default:
                return getK8sCommandLineNew();
        }
    }

    public String getK8sCommandLine() {
        StringBuilder sb = new StringBuilder();
        String taskName = appName;
        String haClusterId = appName;
        if (!isBatchTask) {
            sb.append("./bin/flink run-application -n -t kubernetes-application");
        } else {
            appName = appName + REG;
            haClusterId = taskName + "-" + REG;
            sb.append(" -n -t kubernetes-application");
        }

        if (parallelism != null && parallelism > 0) {
            sb.append(" -p " + parallelism);
        }

        if (StringUtils.isNotEmpty(appName)) {
            sb.append(" -Dkubernetes.cluster-id=" + appName);
        }

        if (StringUtils.isNotEmpty(namespace)) {
            sb.append(" -Dkubernetes.namespace=" + namespace);
        }

        if (StringUtils.isNotEmpty(context)) {
            sb.append(" -Dkubernetes.context=" + context);
        }

        if (StringUtils.isNotEmpty(region)) {
            sb.append(" -Dcontainerized.master.env.region=" + region);
            sb.append(" -Dcontainerized.taskmanager.env.region=" + region);
        }

        if (StringUtils.isNotEmpty(env)) {
            sb.append(" -Dcontainerized.master.env.env=" + env);
            sb.append(" -Dcontainerized.taskmanager.env.env=" + env);
        }

        if (StringUtils.isEmpty(huaweiIam)) {
            huaweiIam = DsTaskConstant.DEFAULT_HUAWEI_IAM;
        }

        if (StringUtils.isEmpty(awsIam)) {
            awsIam = DsTaskConstant.DEFAULT_AWS_IAM;
        }
        String annotations = "";
        String newCluster = ScmpUtil.clusterMap.get(context);
        if (!DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
            if (StringUtils.isEmpty(newCluster)) {
                annotations = MessageFormat.format(DsTaskConstant.JOBMANAGER_ANNOTATIONS_MODEL_NOAWS, huaweiIam);
            } else {
                annotations = MessageFormat.format(DsTaskConstant.JOBMANAGER_ANNOTATIONS_MODEL, huaweiIam, awsIam);
            }
        }

        String lables = MessageFormat.format(DsTaskConstant.JOBMANAGER_LABLES, owner, template, id.toString(), taskName.toLowerCase(), tenantId.toString(), groupId.toString());
        List<Pair<String, String>> labelList = stringToPairList(lables);

        List<Pair<String, String>> jobManagerLabels = new ArrayList<>(labelList);
        List<Pair<String, String>> taskManagerLabels = new ArrayList<>(labelList);
        if (CollectionUtils.isNotEmpty(params)) {
            Optional<RuntimeConfig.Kv> jobLabelsOptional = params.stream().filter(kv -> StringUtils.equalsIgnoreCase("kubernetes.jobmanager.labels", kv.key)).findFirst();
            jobLabelsOptional.ifPresent(kv -> jobManagerLabels.addAll(stringToPairList(kv.value)));

            Optional<RuntimeConfig.Kv> taskLabelsOptional = params.stream().filter(kv -> StringUtils.equalsIgnoreCase("kubernetes.taskmanager.labels", kv.key)).findFirst();
            taskLabelsOptional.ifPresent(kv -> taskManagerLabels.addAll(stringToPairList(kv.value)));

            params.removeIf(kv -> StringUtils.equalsIgnoreCase("kubernetes.jobmanager.labels", kv.key) || StringUtils.equalsIgnoreCase("kubernetes.taskmanager.labels", kv.key));
        }

        sb.append(" -Dtaskmanager.memory.process.size=" + tmMemory + "m")
                .append(" -Dkubernetes.taskmanager.cpu=" + tmCpu)
                .append(" -Dkubernetes.jobmanager.annotations=" + annotations)
                .append(" -Dkubernetes.taskmanager.annotations=" + annotations)
                .append(" -Dkubernetes.jobmanager.labels=" + pairListToString(jobManagerLabels))
                .append(" -Dkubernetes.taskmanager.labels=" + pairListToString(taskManagerLabels))
                .append(" -Dstate.checkpoints.dir=" + statePath + env + "/" + appId + DsTaskConstant.CHECKPOINT_PATH)
                .append(" -Dstate.savepoints.dir=" + statePath + env + "/" + appId + DsTaskConstant.SAVEPOINT_PATH);

        if (StringUtils.isNotEmpty(zookeeperQuorum)) {
            sb.append(" -Dhigh-availability.jobmanager.port=6123")
                    .append(" -Dhigh-availability=zookeeper")
                    .append(" -Dhigh-availability.storageDir=" + statePath + DsTaskConstant.HA_PATH)
                    .append(" -Dhigh-availability.cluster-id=" + haClusterId)
                    .append(" -Dhigh-availability.zookeeper.quorum=" + zookeeperQuorum)
                    .append(" -Dhigh-availability.zookeeper.path.root=/flink");
        }

        if (isBatchTask) {
            String callbackUrl = DataCakeConfigUtil.getDataCakeServiceConfig().getPipelineHost() + "/pipeline/success?name=" + taskName + "&execution_date=" + REG;
            sb.append(" -Dds.callback.url=" + callbackUrl);
        } else {
            String callbackUrl = gatewayUrl + "ds_task/task/flinkstatushook?name=" + taskName;
            sb.append(" -Dds.callback.url=" + callbackUrl);
        }

        String hadoopUserName = InfTraceContextHolder.get().getTenantName() + "#" + owner;
        sb.append(" -Dcontainerized.master.env.HADOOP_USER_NAME=" + hadoopUserName)
                .append(" -Dcontainerized.taskmanager.env.HADOOP_USER_NAME=" + hadoopUserName);

        if (DsTaskConstant.PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())
                || DsTaskConstant.CLOUD_PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())) {
            sb.append(" -Dkubernetes.jobmanager.node-selector=" + nodeSelector)
                    .append(" -Dkubernetes.taskmanager.node-selector=" + nodeSelector)
                    .append(" -Dkubernetes.jobmanager.tolerations=" + tolerations)
                    .append(" -Dkubernetes.taskmanager.tolerations=" + tolerations);
        }

        for (RuntimeConfig.Kv kv : params) {
            sb.append(" -D" + kv.key + "=" + kv.value);
        }

        if (StringUtils.isNotEmpty(containerImage)) {
            sb.append(" -Dkubernetes.container.image=" + containerImage);
        }

        if (StringUtils.isNotEmpty(dependJars)) {
            sb.append(" -DInitContainer.dependJars=" + dependJars);
        }

        if (StringUtils.isNotEmpty(flinkExecutionPackages)) {
            sb.append(" -DFlink.execution.packages=" + flinkExecutionPackages);
        }

        //添加role
        String cloudResourceRole = getCloudResourceRole();
        if (StringUtils.isNotEmpty(cloudResourceRole)) {
            sb.append(" -DAws.client.role=" + cloudResourceRole);
        }

        //判断是新集群就加sa
        Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
        if (StringUtils.isEmpty(newCluster)) {
            CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);

            String RoleName = getCloudResourceRoleByRegion(cloudResource.getRegion(), cloudResource.getProvider());
            sb.append(" -Dfs.s3a.aws.credentials.provider=org.apache.hadoop.fs.s3a.auth.AssumedRoleCredentialProvider ");
            sb.append(" -Dfs.s3a.assumed.role.credentials.provider=com.amazonaws.auth.DefaultAWSCredentialsProviderChain ");
            sb.append(" -Dfs.s3a.assumed.role.arn=" + RoleName);

            String cloudResourceSa = DataCakeConfigUtil.getCloudResourcesService().getCloudResourceSa(cloudResource.getProvider(), cloudResource.getRegion());
            sb.append(" -Dkubernetes.jobmanager.service-account=" + cloudResourceSa);
            sb.append(" -Dkubernetes.taskmanager.service-account=" + cloudResourceSa);


            if (dcRole) {
                sb.append(" -Dkubernetes.config.file=" + System.getProperty("user.home") + "/flink/config");
            }
        }

        String defaultScheduler = "default-scheduler";
        if (!dcRole) {
            defaultScheduler = DataCakeConfigUtil.getDataCakeConfig().getFlinkScheduler();
        }
        sb.append(" -Dkubernetes.jobmanager.schedule.name=" + defaultScheduler);

//        sb.append(" -D\\$internal.pipeline.job-id=" + new JobID().toHexString());

        if (StringUtils.isNotEmpty(mainClass)) {
            sb.append(" -c " + mainClass);
        }

        if (StringUtils.isNotEmpty(fromSavepoint)) {
            sb.append(" -s " + fromSavepoint);
        }

        if (StringUtils.isNotEmpty(mainJar)) {
            sb.append(" " + mainJar);
        }

        if (StringUtils.isNotEmpty(args)) {
            sb.append(" " + args);
        }

        String command = sb.toString();
        log.info("K8S per-job提交时动态参数组装结果：" + command);
        if (isBatchTask) {
            return command;
        }
        String encodeCommand = encode(command);
        String finalCommand = homePath + RUN + " " + encodeCommand;
        return finalCommand;
    }

    /**
     * gateway提交k8sflink任务
     * ./bin/flink run-application -n -t kubernetes-application
     *
     * @return
     */
    public String getK8sCommandLineNew() {

        String taskName = appName;
        String haClusterId = appName;

        HashMap<String, Object> paramMap = new HashMap<>(10);
        List<String> argList = new ArrayList<>();
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);

        paramMap.put("name", appName);
        paramMap.put("batchType", "flink");

//        paramMap.put("resource", mainJar);

        HashMap<String, String> config = new HashMap<>();
        String formatTag = MessageFormat.format("type:{0},region:{1},provider:{2}",
                K8S.toString().toLowerCase(), cloudResource.getRegion(), cloudResource.getProvider());
        config.put("kyuubi.session.cluster.tags", formatTag);
        config.put("kyuubi.session.command.tags", "type:flink,version:" + version);
        config.put("kyuubi.session.group", InfTraceContextHolder.get().getCurrentGroup());
        config.put("kyuubi.session.tenant", InfTraceContextHolder.get().getTenantName());


        for (RuntimeConfig.Kv kv : params) {
            config.put(kv.key, kv.value);
        }


        if (StringUtils.isNotEmpty(appName)) {
            config.put("kubernetes.cluster-id", appName);
        }

        if (StringUtils.isNotEmpty(namespace)) {
            config.put("kubernetes.namespace", namespace);
        }

//        if (StringUtils.isNotEmpty(context)) {
//            config.put("kubernetes.context", context);
//        }

        if (StringUtils.isNotEmpty(region)) {
            config.put("containerized.master.env.region", region);
            config.put("containerized.taskmanager.env.region", region);
        }

        if (StringUtils.isNotEmpty(env)) {
            config.put("containerized.master.env.env", env);
            config.put("containerized.taskmanager.env.env", env);
        }


        String lables = MessageFormat.format(DsTaskConstant.JOBMANAGER_LABLES, owner, template, id.toString(), taskName.toLowerCase(), tenantId.toString(), groupId.toString());
        List<Pair<String, String>> labelList = stringToPairList(lables);

        List<Pair<String, String>> jobManagerLabels = new ArrayList<>(labelList);
        List<Pair<String, String>> taskManagerLabels = new ArrayList<>(labelList);
        if (CollectionUtils.isNotEmpty(params)) {
            Optional<RuntimeConfig.Kv> jobLabelsOptional = params.stream().filter(kv -> StringUtils.equalsIgnoreCase("kubernetes.jobmanager.labels", kv.key)).findFirst();
            jobLabelsOptional.ifPresent(kv -> jobManagerLabels.addAll(stringToPairList(kv.value)));

            Optional<RuntimeConfig.Kv> taskLabelsOptional = params.stream().filter(kv -> StringUtils.equalsIgnoreCase("kubernetes.taskmanager.labels", kv.key)).findFirst();
            taskLabelsOptional.ifPresent(kv -> taskManagerLabels.addAll(stringToPairList(kv.value)));

            params.removeIf(kv -> StringUtils.equalsIgnoreCase("kubernetes.jobmanager.labels", kv.key) || StringUtils.equalsIgnoreCase("kubernetes.taskmanager.labels", kv.key));
        }

        config.put("taskmanager.memory.process.size", tmMemory + "m");
        config.put("kubernetes.taskmanager.cpu", tmCpu.toString());
        config.put("kubernetes.jobmanager.labels", pairListToString(jobManagerLabels));
        config.put("kubernetes.taskmanager.labels", pairListToString(jobManagerLabels));
        config.put("kubernetes.rest-service.annotations", "ushareit.me/svc-gen-httpproxy:flink");
//        config.put("kubernetes.rest-service.exposed.type", "LoadBalancer");
//        if(StringUtils.isNotEmpty(region) && region.contains("aliyun") ){
//            config.put("kubernetes.rest-service.annotations", "service.beta.kubernetes.io/alibaba-cloud-loadbalancer-address-type:intranet,service.beta.kubernetes.io/alibaba-cloud-loadbalancer-ip-version:ipv4,service.beta.kubernetes.io/alibaba-cloud-loadbalancer-scheduler:rr");
            config.put("fs.oss.impl", "com.aliyun.jindodata.oss.JindoOssFileSystem");
            config.put("fs.allowed-fallback-filesystems", "oss");
//        }
        if(StringUtils.isNotEmpty(statePath)){
            config.put("state.checkpoints.dir", statePath + env + "/" + appId + DsTaskConstant.CHECKPOINT_PATH);
            config.put("state.savepoints.dir", statePath + env + "/" + appId + DsTaskConstant.SAVEPOINT_PATH);
        }

        if (runtimeConfig.getCheckpoint()) {
            config.put("execution.checkpointing.interval", runtimeConfig.getCheckpointInterval().toString());
            config.put("execution.checkpointing.mode", runtimeConfig.getCheckpointMode());
            if (runtimeConfig.getCheckpointTimeout() != null) {
                config.put("execution.checkpointing.timeout", runtimeConfig.getCheckpointTimeout().toString());
            }
        }

        if (StringUtils.isNotEmpty(zookeeperQuorum)) {
            config.put("high-availability.jobmanager.port", "6123");
            config.put("high-availability", "zookeeper");
            config.put("high-availability.storageDir=", statePath + DsTaskConstant.HA_PATH);
            config.put("high-availability.cluster-id", appName);
            config.put("high-availability.zookeeper.quorum", zookeeperQuorum);
            config.put("high-availability.zookeeper.path.root", "/flink");
        }


        String hadoopUserName = InfTraceContextHolder.get().getTenantName() + "#" + owner;
        config.put("containerized.master.env.HADOOP_USER_NAME", hadoopUserName);
        config.put("containerized.taskmanager.env.HADOOP_USER_NAME", hadoopUserName);


        if (DsTaskConstant.PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())
                || DsTaskConstant.CLOUD_PROD.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())) {
            config.put("kubernetes.jobmanager.node-selector", nodeSelector);
            config.put("kubernetes.taskmanager.node-selector", nodeSelector);
            config.put("kubernetes.jobmanager.tolerations", tolerations);
            config.put("kubernetes.taskmanager.tolerations", tolerations);
        }


        String cloudResourceSa = DataCakeConfigUtil.getCloudResourcesService().getCloudResourceSa(cloudResource.getProvider(), cloudResource.getRegion());
        if (StringUtils.isNotEmpty(cloudResourceSa)) {
            config.put("kubernetes.jobmanager.service-account", cloudResourceSa);
            config.put("kubernetes.taskmanager.service-account", cloudResourceSa);
        }

        for (RuntimeConfig.Kv kv : params) {
            config.put(kv.key, kv.value);
        }

        if (StringUtils.isNotEmpty(containerImage)) {
            config.put("kubernetes.container.image", containerImage);
        }

        if (StringUtils.isNotEmpty(dependJars)) {
            config.put("InitContainer.dependJars", dependJars);
        }

        if (StringUtils.isNotEmpty(flinkExecutionPackages)) {
            config.put("Flink.execution.packages", flinkExecutionPackages);
        }


        String defaultScheduler = "default-scheduler";
        config.put("kubernetes.jobmanager.schedule.name", defaultScheduler);


        if (parallelism != null && parallelism > 0) {
            argList.add("-p");
            argList.add(parallelism.toString());
        }

        if (StringUtils.isNotEmpty(mainClass)) {
            argList.add("-c");
            argList.add(mainClass);
        }

        if (StringUtils.isNotEmpty(fromSavepoint)) {
            argList.add("-s");
            argList.add(fromSavepoint);
        }

        if (StringUtils.isNotEmpty(mainJar)) {
            argList.add(mainJar);
        }

        if (execArgs != null && !execArgs.isEmpty()) {
            argList.addAll(execArgs);
        } else if (StringUtils.isNotEmpty(args)) {
            argList.add(args);
        }


        paramMap.put("args", argList.toArray());
        paramMap.put("conf", config);
        return JSONObject.toJSONString(paramMap);
    }


    public String getYarnCommandLine() {
        HashMap<String, Object> paramMap = new HashMap<>(10);
        List<String> argList = new ArrayList<>();
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);

        paramMap.put("name", appName);
        paramMap.put("batchType", "flink");

//        paramMap.put("resource", mainJar);

        HashMap<String, String> config = new HashMap<>();
        String formatTag = MessageFormat.format("type:{0},region:{1},sla:normal,rbac.cluster:bdp-prod,provider:{2}",
                YARN.toString().toLowerCase(), cloudResource.getRegion(), cloudResource.getProvider());
        config.put("yarn.application.name", appName);
        config.put("kyuubi.session.cluster.tags", formatTag);
        config.put("kyuubi.session.command.tags", "type:flink,version:" + version);
        config.put("kyuubi.session.group", InfTraceContextHolder.get().getCurrentGroup());
        config.put("kyuubi.session.tenant", InfTraceContextHolder.get().getTenantName());
        config.put("state.checkpoints.dir", statePath + env + "/" + appId + DsTaskConstant.CHECKPOINT_PATH);
        config.put("state.savepoints.dir", statePath + env + "/" + appId + DsTaskConstant.SAVEPOINT_PATH);

        if (runtimeConfig.getCheckpoint()) {
            config.put("execution.checkpointing.interval", runtimeConfig.getCheckpointInterval().toString());
            config.put("execution.checkpointing.mode", runtimeConfig.getCheckpointMode());
            if (runtimeConfig.getCheckpointTimeout() != null) {
                config.put("execution.checkpointing.timeout", runtimeConfig.getCheckpointTimeout().toString());
            }
        }

        if (StringUtils.isNotEmpty(zookeeperQuorum)) {
            config.put("high-availability.jobmanager.port", "6123");
            config.put("high-availability", "zookeeper");
            config.put("high-availability.storageDir=", statePath + DsTaskConstant.HA_PATH);
            config.put("high-availability.cluster-id", appName);
            config.put("high-availability.zookeeper.quorum", zookeeperQuorum);
            config.put("high-availability.zookeeper.path.root", "/flink");
        }

        for (RuntimeConfig.Kv kv : params) {
            config.put(kv.key, kv.value);
        }

        //作业名称
        argList.add("-nm");
        argList.add(appName);

        //TaskManager的内存大小
        argList.add("-ytm");
        argList.add(tmMemory + "m");

        //JobManager的内存大小
        argList.add("-yjm");
        argList.add(tmMemory + "m");

        //一个taskManager的slot大小
        argList.add("-ys");
        argList.add("1");


        String ytJar = "";
        if (StringUtils.isNotEmpty(dependJars)) {
            ytJar = ytJar + dependJars;
        }

        if (StringUtils.isNotEmpty(dependentInformationJar)) {
            if (StringUtils.isNotEmpty(ytJar)) {
                ytJar = ytJar + "," + dependentInformationJar;
            } else {
                ytJar = dependentInformationJar;
            }
        }

        //依赖jar
        if (StringUtils.isNotEmpty(ytJar)) {
            argList.add("-yt");
            argList.add(ytJar);
        }

        //主类
        if (StringUtils.isNotEmpty(mainClass)) {
            argList.add("-c");
            argList.add(mainClass);
        }

        //保存点
        if (StringUtils.isNotEmpty(fromSavepoint)) {
            argList.add("-s");
            argList.add(fromSavepoint);
        }

        if (StringUtils.isNotEmpty(mainJar)) {
            argList.add(mainJar);
        }

        if (execArgs != null && !execArgs.isEmpty()) {
            argList.addAll(execArgs);
        } else if (StringUtils.isNotEmpty(args)) {
            argList.add(args);
        }

        paramMap.put("args", argList.toArray());
        paramMap.put("conf", config);
        return JSONObject.toJSONString(paramMap);
    }


    private String getCloudResourceRole() {
        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<CloudResouce.DataResource> list = cloudResource.getList();
        StringBuilder role = new StringBuilder("'");
        for (CloudResouce.DataResource dataResource : list) {
            role.append(dataResource.getRegion()).append("&").append(dataResource.getRoleName());
            if (list.size() - 1 != list.indexOf(dataResource)) {
                role.append(",");
            }
        }
        role.append("'");
        return role.toString();
    }

    private String getCloudResourceRoleByRegion(String region, String cloud) {
        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<CloudResouce.DataResource> list = cloudResource.getList();
        for (CloudResouce.DataResource dataResource : list) {
            if (dataResource.getRegion().equals(region) && dataResource.getProvider().equals(cloud)) {
                return dataResource.getRoleName();
            }
        }
        return "default";
    }

    private String encode(String str) {
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    private List<Pair<String, String>> stringToPairList(String labelStr) {
        if (StringUtils.isBlank(labelStr)) {
            return new ArrayList<>();
        }

        return Arrays.stream(labelStr.split(SymbolEnum.COMMA.getSymbol()))
                .filter(item -> item.contains(SymbolEnum.COLON.getSymbol()))
                .map(item -> {
                    String[] kvs = item.split(SymbolEnum.COLON.getSymbol());
                    return Pair.create(kvs[0], kvs[1]);
                }).collect(Collectors.toList());
    }

    private String pairListToString(List<Pair<String, String>> pairList) {
        if (CollectionUtils.isEmpty(pairList)) {
            return StringUtils.EMPTY;
        }

        Set<String> keys = new HashSet<>();
        return pairList.stream().filter(item -> {
                    boolean exist = keys.contains(item.getKey());
                    keys.add(item.getKey());
                    return !exist;
                }).map(item -> String.join(SymbolEnum.COLON.getSymbol(), item.getKey(), item.getValue()))
                .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));

    }

}
