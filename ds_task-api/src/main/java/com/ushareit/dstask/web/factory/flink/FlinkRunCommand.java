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

    public String getCommandLine() {
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
            CloudResouce.DataResource cloudResource =DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);

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

