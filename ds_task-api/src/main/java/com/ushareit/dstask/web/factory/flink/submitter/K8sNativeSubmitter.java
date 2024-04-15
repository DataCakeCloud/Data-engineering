package com.ushareit.dstask.web.factory.flink.submitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.FlinkVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.flink.FlinkRunCommand;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.engine.seatunnel.util.ShellUtil;
import com.ushareit.dstask.web.utils.ZkClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2021/12/10
 */
@Slf4j
public class K8sNativeSubmitter extends BaseSubmitter {
    public K8sNativeSubmitter(TaskServiceImpl taskServiceImp, Job job) {
        super(taskServiceImp, job);
    }

    @Override
    public void submit() {
        try {
            doExec();
        } catch (Exception e) {
            log.error("K8sNativeSubmitter submitAsync error:", e);
            processException();
        }
    }

    @Override
    public void processException() {
        log.info("K8sNativeSubmitter processException");
        updateTaskAndInstance();
        deleteDeploymentAndZk();
    }

    public void doExec() throws Exception {
        long startMs = System.currentTimeMillis();
        String command = this.getCommand();

        boolean success = this.doProcess(command);

        log.info("Process completed " + (success ? "successfully" : "unsuccessfully") + " in " + (System.currentTimeMillis() - startMs) / 1000L + " seconds.");
    }

    private Boolean doProcess(String execCmd) throws Exception {
        ShellUtil.CommandResult response = ShellUtil.execCmd(execCmd, false);
        if (response == null || !response.result || response.errorMsg.length() > 0) {
            throw new ServiceException(BaseResponseCodeEnum.JOB_SUBMIT_TO_FLINK_FAIL, BaseResponseCodeEnum.JOB_SUBMIT_TO_FLINK_FAIL.getMessage(), response.errorMsg);
        }
        return response.result;
    }

    /**
     * 获取执行命令行
     *
     * @return
     */
    public String getCommand() {
        baseJob.command = getFlinkRunCommand();
        return baseJob.command.getCommandLine();
    }

    @Override
    public void deleteResource() {
        ZkClientUtil.deleteRunningJobRegistry(baseJob.cluster.getZookeeperQuorum(), baseJob.runTimeTaskBase.getName());
    }

    public void deleteDeploymentAndZk() {
        taskServiceImp.scmpUtil.deleteK8sDeployment(baseJob.cluster.getAddress(), baseJob.cluster.getNameSpace(), baseJob.runTimeTaskBase.getName());
        ZkClientUtil.deleteRunningJobRegistry(baseJob.cluster.getZookeeperQuorum(), baseJob.runTimeTaskBase.getName());
    }

    private FlinkRunCommand getFlinkRunCommand() {
        String mainClass = baseJob.runTimeTaskBase.getMainClass();
        String args = baseJob.runTimeTaskBase.getMainClassArgs();

        Integer parallelism = baseJob.runtimeConfig.getParallelism();
        String huaweiIam = baseJob.runtimeConfig.getHuaweiIam();
        String awsIam = baseJob.runtimeConfig.getAwsIam();
        Double tmCpu = baseJob.runtimeConfig.getTmCpu();
        Double tmMemory = baseJob.runtimeConfig.getTmMemory();
        List<RuntimeConfig.Kv> params = getParams();

        String containerImage = baseJob.cluster.getContainerImage();

        StringBuffer dependJars = new StringBuffer(baseJob.runTimeTaskBase.getJarUrl());
        if (StringUtils.isNotEmpty(baseJob.onlineUdfJarObsUrl)) {
            dependJars.append(",").append(baseJob.onlineUdfJarObsUrl);
        }
        if (StringUtils.isNotEmpty(baseJob.artifactUdfJarObsUrl)) {
            dependJars.append(",").append(baseJob.artifactUdfJarObsUrl);
        }
        log.info("k8s per-job: dependJars=" + dependJars);

        String jarUrl = baseJob.runTimeTaskBase.getJarUrl();
        String[] jarArr = jarUrl.split("/");
        String jarNmae = jarArr[jarArr.length - 1];
        log.info("local:" + DsTaskConstant.LOCAL_PREFIX + jarNmae);
        String mainJar = DsTaskConstant.LOCAL_PREFIX + jarNmae;

        Integer tentanId = 1;
        Integer groupId = 0;
        JSONObject runtimeConfigObject = JSON.parseObject(baseJob.runTimeTaskBase.getRuntimeConfig());
        List<AccessGroup> accessGroupList = taskServiceImp.accessGroupService.selectByName(runtimeConfigObject.getString("owner"));
        List<Integer> collect = accessGroupList.stream().map(AccessGroup::getParentId).collect(Collectors.toList());

        List<AccessGroup> groupList = taskServiceImp.accessGroupService.listByIds(collect);
        for (AccessGroup accessGroup : groupList) {
            tentanId = accessGroup.getTenantId();
            AccessTenant accessTenant = taskServiceImp.accessTenantService.checkExist(tentanId);
            Integer id = accessGroup.getId();
            String eName = taskServiceImp.accessGroupService.getRootGroup(id).getEName();
            if (StringUtils.isNotEmpty(eName) && eName.equals(accessTenant.getName())) {
                groupId = id;
                if (accessGroup.getName().contains("default")) {
                    break;
                }
            }
        }


        FlinkRunCommand command = new FlinkRunCommand()
                .setTaskServiceimpl(taskServiceImp)
                .setCluster(baseJob.cluster.getAddress())
                .setAppName(baseJob.runTimeTaskBase.getName().toLowerCase())
                .setMainClass(mainClass)
                .setArgs(args)
                .setContainerImage(containerImage)
                .setDependJars(dependJars.toString())
                .setMainJar(mainJar)
                .setFromSavepoint(baseJob.savepointUrl)
                .setParallelism(parallelism)
                .setContext(baseJob.cluster.getAddress())
                .setHuaweiIam(huaweiIam)
                .setAwsIam(awsIam)
                .setStatePath(baseJob.cluster.getStatePath())
                .setTmCpu(tmCpu == null ? 1 : tmCpu)
                .setTmMemory(Math.round((tmMemory == null ? 4 : tmMemory) * 1024))
                .setParams(params)
                .setZookeeperQuorum(baseJob.cluster.getZookeeperQuorum())
                .setAppId(baseJob.runTimeTaskBase.getId())
                .setFlinkExecutionPackages(baseJob.flinkExecutionPackages)
                .setRegion(baseJob.cluster.getRegion())
                .setEnv(baseJob.cluster.getEnv())
                .setIsBatchTask(baseJob.runtimeConfig.getIsBatchTask())
                .setNamespace(baseJob.cluster.getNameSpace())
                .setNodeSelector(baseJob.cluster.getNodeSelector())
                .setTolerations(baseJob.cluster.getTolerations())
                .setId(baseJob.runTimeTaskBase.getId())
                .setOwner(baseJob.runTimeTaskBase.getCreateBy())
                .setTemplate(baseJob.runTimeTaskBase.getTemplateCode())
                .setGatewayUrl(taskServiceImp.getGatewayUrl())
                .setTenantId(tentanId)
                .setGroupId(groupId);
        command.setHomePath("/data/code");
        return command;
    }

    private List<RuntimeConfig.Kv> getParams() {
        List<RuntimeConfig.Kv> params = baseJob.runtimeConfig.getParams();

        boolean greaterThanFlink113 = FlinkVersion.fromVersionString(baseJob.cluster.getVersion()).isGreaterThanFlink113();
        if (greaterThanFlink113) {
            RuntimeConfig.Kv kv = baseJob.runtimeConfig.new Kv("kubernetes.entry.path", "/opt/flink/bin/kubernetes-entry.sh");
            params.add(kv);
        }
        return params;
    }

}
