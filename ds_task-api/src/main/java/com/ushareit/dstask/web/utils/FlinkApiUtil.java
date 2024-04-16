package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * @author: licg
 * @create: 2020-06-30
 **/
public class FlinkApiUtil {
    /**
     * 组装取消flinkjob的URL
     *
     * @param flinkJmAddress
     * @param flinkJobId
     * @return
     */
    public static String getCancel(String flinkJmAddress, String flinkJobId) {
        String url = flinkJmAddress + "/jobs/" + flinkJobId + "/yarn-cancel";
        return url;
    }

    /**
     * 组装获取flink-job详细信息的接口
     *
     * @param flinkJmAddress
     * @param flinkJobId
     * @return
     */
    public static String getJob(String flinkJmAddress, String flinkJobId) {
        String url = flinkJmAddress + "/jobs";
//        String url = flinkJmAddress + "/jobs/" + flinkJobId;
        return url;
    }

    /**
     * 组装提交job的URL
     *
     * @param flinkJobManagerUrl
     * @param jarId
     * @return
     */
    public static String submitJob(String flinkJobManagerUrl, String jarId) {
        String url = flinkJobManagerUrl + "/jars/" + jarId + "/run";
        return url;
    }

    /**
     * 组装上传jar的URL
     *
     * @param flinkJobManagerUrl
     * @return
     */
    public static String uploadJar(String flinkJobManagerUrl) {
        String url = flinkJobManagerUrl + "/jars/upload";
        return url;
    }

    /**
     * 组装手动触发保存点地址
     *
     * @param serviceAddress
     * @param instanceId
     * @return
     */
    public static String triggerSavepoint(String serviceAddress, String instanceId) {
        String url = serviceAddress + "/jobs/" + instanceId + "/savepoints";
        return url;
    }

    /**
     * 组装stopWithSavepoint地址
     *
     * @param serviceAddress
     * @param instanceId
     * @return
     */
    public static String stopWithSavepoint(String serviceAddress, String instanceId) {
        return serviceAddress + "/jobs/" + instanceId + "/stop";
    }

    /**
     * 组装查询保存点结果的URL
     *
     * @param serviceAddress
     * @param instanceId
     * @param triggerId
     * @return
     */
    public static String getSavepointResult(String serviceAddress, String instanceId, String triggerId) {
        String url = serviceAddress + "/jobs/" + instanceId + "/savepoints/" + triggerId;
        return url;
    }

    /**
     * 获取集群总览信息URL
     *
     * @param flinkJobManagerUrl
     * @return
     */
    public static String getOverview(String flinkJobManagerUrl) {
        return flinkJobManagerUrl + "/overview";
    }

    /**
     * 获取删除flink jar的url
     *
     * @param flinkJobManagerUrl
     * @param flinkJarId
     * @return
     */
    public static String deleteFlinkJar(String flinkJobManagerUrl, String flinkJarId) {
        return flinkJobManagerUrl + "/jars/" + flinkJarId;
    }

    /**
     * 获取查看flink job的页面地址
     *
     * @param flinkJobManagerUrl
     * @param flinkJobId
     * @return
     */
    public static String getFlinkJobDetailUrl(String flinkJobManagerUrl, String flinkJobId) {
        return flinkJobManagerUrl + "/#/job";
//        if (StringUtils.isBlank(flinkJobId)) {
//            return flinkJobManagerUrl + "/#/job";
//        } else {
//            return flinkJobManagerUrl + "/#/job/" + flinkJobId + "/overview";
//        }
    }


    /**
     * 获取job列表
     *
     * @param flinkJobManagerUrl
     * @return
     */
    public static String getJobs(String flinkJobManagerUrl) {
        return flinkJobManagerUrl + "/jobs/overview";
    }

    public static String getMetricsUrl(String flinkJobId) {
        return MessageFormat.format(DsTaskConstant.METRICS_URL, flinkJobId);
    }

    /**
     * 获取session集群配置的checkpoint or savepoint path
     *
     * @param flinkJobManagerUrl
     * @return
     */
    public static String getFlinkSessionStatePath(String flinkJobManagerUrl) {
        return MessageFormat.format("{0}/jobmanager/config", flinkJobManagerUrl);
    }

    /**
     * 获取job的并行度
     *
     * @param flinkJobManagerUrl
     * @return
     */
    public static String getFlinkJobParallism(String flinkJobManagerUrl, String flinkJobId) {
        return flinkJobManagerUrl + "/jobs/" + flinkJobId + "/config";
    }

    public static String getPerjobMetricsUrl(String app, String flinkJobId) {
        return MessageFormat.format(DsTaskConstant.PERJOB_METRICS_URL, app, flinkJobId);
    }

    public static String getLogsUrl(String esSource, String appName) {
        String format = MessageFormat.format(DsTaskConstant.LOG_URL_BEHIN, esSource, appName);
        String replace = format.replace("{", "%7B")
                .replace("}", "%7D")
                .replace("(", "%5C%22")
                .replace(")", "%5C%22")
                .replace("[", "%5B")
                .replace("]", "%5D")
                .replace("\"", "%22");
        return DsTaskConstant.LOG_URL_FRONT + replace;
    }

    public static String getOfflineTaskUrl(String name) {
        return MessageFormat.format(DsTaskConstant.OFFLINE_TASK_URL, InfTraceContextHolder.get().getEnv(), name);
    }
}
