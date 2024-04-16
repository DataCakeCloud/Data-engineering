package com.ushareit.dstask.web.factory.flink.submitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.FlinkVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.FlinkExecModeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.flink.FlinkRunCommand;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.kubernetes.*;
import com.ushareit.dstask.web.utils.*;
import com.ushareit.dstask.web.vo.BaseResponse;
import com.ushareit.engine.seatunnel.util.ShellUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
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
        boolean success = this.doProcessGateWay(command);
        log.info("Process completed " + (success ? "successfully" : "unsuccessfully") + " in " + (System.currentTimeMillis() - startMs) / 1000L + " seconds.");
    }

    private Boolean doProcess(String execCmd) throws Exception {
        ShellUtil.CommandResult response = ShellUtil.execCmd(execCmd, false);
        if (response == null || !response.result || response.errorMsg.length() > 0) {
            throw new ServiceException(BaseResponseCodeEnum.JOB_SUBMIT_TO_FLINK_FAIL, BaseResponseCodeEnum.JOB_SUBMIT_TO_FLINK_FAIL.getMessage(), response.errorMsg);
        }
        return response.result;
    }

    //改造新版吧提交命令交给gateway执行
    private Boolean doProcessGateWay(String execCmd) throws Exception {
        String user = new String(org.apache.commons.codec.binary.Base64.encodeBase64(baseJob.runTimeTaskBase.getCreateBy().getBytes()));
        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Basic " + user);
        log.info("k8s execCmd  config is :" + execCmd + " user is :" + baseJob.runTimeTaskBase.getCreateBy());

        String gatewayHost = DataCakeConfigUtil.getDataCakeConfig().getGatewayRestHost();
        //http://gateway-test.ushareit.org:100099
        BaseResponse response = HttpUtil.postWithJson(gatewayHost + "/api/v1/batches", execCmd, headers);
        JSONObject jsonObject = response.get();
        if (response.getCode() != 0 || jsonObject.get("id") == null) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_SUBMIT_FAIL);
        }
        Object id = jsonObject.get("id");

        log.info(" start flink task uuid is :" + id);

        //版本二
//        //获取UI  等5s
//        Thread.sleep(10000);
//        log.info(" sleep arfter ");
//        //'http://gateway-sg1.datacake.cloud/api/v1/batches/33f7be8b-6fed-432c-b561-e3fe215ff5af' \
//        String logUrl = MessageFormat.format("/api/v1/batches/{0}", id.toString());
//        log.info(" request is : " + gatewayHost + logUrl);
//        BaseResponse logResponse = HttpUtil.get(gatewayHost + logUrl);
//
//        Object webUIObject = logResponse.get().get("appUrl");
//        log.info(" frist appUrl is : " + webUIObject);
//        if (logResponse.getCode() != 0 || webUIObject == null) {
//            throw new ServiceException(BaseResponseCodeEnum.TASK_GET_LOG_FAIL);
//        }
//
//        //Found Web Interface
//        String ui = webUIObject.toString();
//
//        log.info(" webUi is : " + ui);
//        if (StringUtils.isEmpty(ui)) {
//            Thread.sleep(10000);
//            BaseResponse baseResponse = HttpUtil.get(gatewayHost + logUrl);
//            webUIObject = baseResponse.get().get("appUrl");
//            log.info(" two logRowSet is : " + webUIObject.toString());
//            ui = webUIObject.toString();
//        }
//
//        baseJob.setSubmitUUid(id.toString());
//        updateInstanceUI(id.toString());
//        if (ui != null) {
//            if (!ui.startsWith("http:")) {
//                ui = "http://" + ui;
//            }
//            if (!ui.endsWith("8081")) {
//                ui = ui + ":8081";
//            }
//            baseJob.setWebUi(ui);
//        }

        //版本一
        //获取UI  等5s
//        Thread.sleep(10000);
//        log.info(" sleep arfter ");
//        String logUrl = MessageFormat.format("/api/v1/batches/{0}/localLog?from=0&size=2000", id.toString());
//        log.info(" request is : " + gatewayHost + logUrl);
//        BaseResponse logResponse = HttpUtil.get(gatewayHost + logUrl);
//
//        Object logRowSet = logResponse.get().get("logRowSet");
//        log.info(" frist logRowSet is : " + logRowSet);
//        if (logResponse.getCode() != 0 || logRowSet == null) {
//            throw new ServiceException(BaseResponseCodeEnum.TASK_GET_LOG_FAIL);
//        }
//
//        //Found Web Interface
//        String logs = logRowSet.toString();
//        String webUi = parseLogV2(logs);
//
//        log.info(" webUi is : " + webUi);
//        if (webUi == null) {
//            Thread.sleep(10000);
//            BaseResponse baseResponse = HttpUtil.get(gatewayHost + logUrl);
//            logRowSet = baseResponse.get().get("logRowSet");
//            log.info(" two logRowSet is : " + logRowSet);
//            webUi = parseLogV2(logRowSet.toString());
//        }
//
//        baseJob.setSubmitUUid(id.toString());
//        updateInstanceUI(id.toString());
//        if (webUi != null) {
//            if (!webUi.startsWith("http")) {
//                webUi = "http://" + webUi;
//            }
//            if (!webUi.endsWith("8081")) {
//                webUi = webUi + ":8081";
//            }
//            baseJob.setWebUi(webUi);
//        }
        String region = baseJob.getCluster().getRegion();
        String address = "http://{0}-rest.ack.xdata.staff.xdf.cn";
        if(region.contains("aliyun")){
            address = "http://{0}-rest.ack.xdata.staff.xdf.cn";
        }
        if(region.contains("tencent")){
            address = "http://{0}-rest.tke.xdata.staff.xdf.cn";
        }
        baseJob.setWebUi(MessageFormat.format(address, baseJob.task.getName()));
        return true;
    }

    public static String parseLog(String log) {
        String webUi;
        //JobManager Web Interface: http://test-flink-sql01-rest.datacake:8081"]
        try {
            String UI_LOG_PATTERN = "(.+) - Found Web Interface (.+) of application (.+)";
            Matcher matcher = UrlUtil.getMatcher(log, UI_LOG_PATTERN);
            String group = matcher.group(2);
            System.out.println(group);

            if(group.contains(".")){
                return group;
            }
            String[] webUiArr = group.split(":");
            String[] IPStr = webUiArr[0].split("\\.");
            String[] IPArr = IPStr[0].split("-");
            webUi = IPArr[1] + "." + IPArr[2] + "." + IPArr[3] + "." + IPArr[4] + ":" + webUiArr[1];
        } catch (Exception e) {
            return null;
        }
        return webUi;
    }

    public static String parseLogV2(String log) {
        String webUi;
        //JobManager Web Interface: http://test-flink-sql01-rest.datacake:8081"]
        try {
            String UI_LOG_PATTERN = "(.+) JobManager Web Interface: (.+)\"(.+)";
            Matcher matcher = UrlUtil.getMatcher(log, UI_LOG_PATTERN);
            webUi = matcher.group(2);
        } catch (Exception e) {
            return null;
        }
        return webUi;
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
//        taskServiceImp.scmpUtil.deleteK8sDeployment(baseJob.cluster.getAddress(), baseJob.cluster.getNameSpace(), baseJob.runTimeTaskBase.getName());
        taskServiceImp.scmpUtil.deleteKDeploymentByGateWay(baseJob.getSubmitUUid(), baseJob.task.getCreateBy());
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
                .setRuntimeConfig(baseJob.runtimeConfig)
                .setTaskServiceimpl(taskServiceImp)
                .setCluster(baseJob.cluster.getAddress())
                .setAppName(baseJob.runTimeTaskBase.getName().toLowerCase())
                .setMainClass(mainClass)
                .setArgs(args)
                .setExecArgs(baseJob.execArgs)
                .setContainerImage(containerImage)
                .setDependJars(dependJars.toString())
                .setMainJar(mainJar)
                .setVersion(baseJob.getCluster().getVersion())
                .setFromSavepoint(baseJob.savepointUrl)
                .setMode(baseJob.cluster.getTypeCode())
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
