package com.ushareit.dstask.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.shaded.curator4.org.apache.curator.framework.CuratorFramework;
import org.apache.flink.shaded.curator4.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.flink.shaded.curator4.org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author wuyan
 * @date 2020/11/30
 */
@Slf4j
public class ZkClientUtil {
    private static CuratorFramework newClient(String zkServerAddress) {
        if (zkServerAddress == null || StringUtils.isBlank(zkServerAddress)) {
            throw new RuntimeException("No valid ZooKeeper quorum has been specified. ");
        }

        int sessionTimeout = HighAvailabilityOptions.ZOOKEEPER_SESSION_TIMEOUT.defaultValue();

        int connectionTimeout = HighAvailabilityOptions.ZOOKEEPER_CONNECTION_TIMEOUT.defaultValue();

        int retryWait = HighAvailabilityOptions.ZOOKEEPER_RETRY_WAIT.defaultValue();

        int maxRetryAttempts = HighAvailabilityOptions.ZOOKEEPER_MAX_RETRY_ATTEMPTS.defaultValue();

        CuratorFramework cf = CuratorFrameworkFactory.builder()
                .connectString(zkServerAddress)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(retryWait, maxRetryAttempts))
                .build();

        cf.start();
        return cf;
    }

    public static void deleteTaskNode(String zkServerAddress, String appName) {
        String zkPath = HighAvailabilityOptions.HA_ZOOKEEPER_ROOT.defaultValue() + "/" + appName.toLowerCase();
        deletePath(zkServerAddress, zkPath);
    }

    public static void deleteRunningJobRegistry(String zkServerAddress, String appName) {
        String zkPath = createZkPath(appName);
        deletePath(zkServerAddress, zkPath);
    }


    private static void deletePath(String zkServerAddress, String zkDeletePath) {
        CuratorFramework zkClient = null;
        try {
            zkClient = newClient(zkServerAddress);
            zkClient.delete().guaranteed().deletingChildrenIfNeeded().forPath(zkDeletePath);
            log.info("delete zookeeper node " + zkDeletePath + " success");
        } catch (Exception e) {
            log.error("delete zookeeper node " + zkDeletePath + " fail, the reason:" + e);
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

    private static String createZkPath(String appName) {
        String runningJobRegistryPath = HighAvailabilityOptions.ZOOKEEPER_RUNNING_JOB_REGISTRY_PATH.defaultValue();
        return HighAvailabilityOptions.HA_ZOOKEEPER_ROOT.defaultValue() + "/" + appName.toLowerCase() + runningJobRegistryPath.substring(0, runningJobRegistryPath.lastIndexOf("/"));
    }

}
