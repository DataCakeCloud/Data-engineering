package com.ushareit.dstask.airbyte;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.ushareit.dstask.third.airbyte.Configs;
import com.ushareit.dstask.third.airbyte.DefaultAirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.IntegrationLauncher;
import com.ushareit.dstask.third.airbyte.ProcessFactory;
import com.ushareit.dstask.third.airbyte.common.Constant;
import com.ushareit.dstask.third.airbyte.common.enums.Enums;
import com.ushareit.dstask.third.airbyte.common.io.IOs;
import com.ushareit.dstask.third.airbyte.config.*;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.impl.AirbyteIntegrationLauncher;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.airbyte.k8s.KubePortManagerSingleton;
import com.ushareit.dstask.third.airbyte.k8s.KubeProcessFactory;
import com.ushareit.dstask.third.airbyte.utils.PathUtil;
import com.ushareit.dstask.third.airbyte.utils.WorkerConstants;
import com.ushareit.dstask.third.airbyte.utils.WorkerUtils;
import com.ushareit.dstask.third.airbyte.worker.impl.DefaultGetSpecWorker;
import com.ushareit.dstask.web.ddl.model.K8SClusterInfo;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author fengxiao
 * @date 2022/6/22
 */
public class TestGetSpec {

    private static final Path TEST_ROOT = PathUtil.of("/tmp/airbyte_tests");
    private static final String DUMMY_IMAGE_NAME = "airbyte/source-mysql:0.5.11";
    public static final int KUBE_HEARTBEAT_PORT = 9000;
    private static final String JOB_ID = "0";
    private static final int JOB_ATTEMPT = 0;
    private static final Path JOB_ROOT = PathUtil.of("abc");
    private static final String LOCAL_IP = "192.168.12.71";

    private DefaultGetSpecWorker worker;
    private IntegrationLauncher integrationLauncher;
    private Process process;
    private Path jobRoot;
    private JobGetSpecConfig config;

    private static final JsonNode CREDS = Jsons.jsonNode(ImmutableMap.builder()
            .put("host", "test.inf-common.cbs.sg2.mysql")
            .put("port", 3306)
            .put("database", "ds_task")
            .put("jdbc_url_params", "autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai")
            .put("username", "CBS_Developer")
            .put("password", "dTaKBkzFHRnwSwfhLL^pXIJWbR%3INLl")
            .put("replication_method", "STANDARD")
            .put("tunnel_method", ImmutableMap.builder().put("tunnel_method", "NO_TUNNEL").build())
            .build());

    @Test
    public void testGetSepc() throws Exception {
        //InetAddress.getLocalHost().getHostAddress();
        System.out.println("local IP is " + LOCAL_IP);
        final String kubeHeartbeatUrl = LOCAL_IP + ":" + KUBE_HEARTBEAT_PORT;
        final KubernetesClient fabricClient = initClient(getTestClusterInfo());

        final Configs configs = new EnvConfigs();
        final WorkerConfigs workerConfigs = new WorkerConfigs(configs);

        KubePortManagerSingleton.init(Constant.PORTS);
        ProcessFactory processFactory = new KubeProcessFactory(workerConfigs,
                //configs.getJobKubeNamespace(),
                "cbs-flink",
                fabricClient,
                kubeHeartbeatUrl,
                false);

        System.out.println(processFactory);

        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, "internal", DUMMY_IMAGE_NAME,
                processFactory, workerConfigs.getResourceRequirements());

        process = launcher.spec(JOB_ROOT);

        final Optional<ConnectorSpecification> spec;
        try (final InputStream stdout = process.getInputStream()) {
            spec = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.SPEC)
                    .map(AirbyteMessage::getSpec)
                    .findFirst();

            spec.ifPresent(connectorSpecification -> {
                System.out.println("hello world");
                System.out.println(Jsons.serialize(connectorSpecification));
            });

            // todo (cgardens) - let's pre-fetch the images outside of the worker so we don't need account for
            // this.
            // retrieving spec should generally be instantaneous, but since docker images might not be pulled
            // it could take a while longer depending on internet conditions as well.
            WorkerUtils.gentleClose(workerConfigs, process, 30, TimeUnit.MINUTES);
        }
    }

    @Test
    public void testCheck() throws Exception {
        //InetAddress.getLocalHost().getHostAddress();
        System.out.println("local IP is " + LOCAL_IP);
        final String kubeHeartbeatUrl = LOCAL_IP + ":" + KUBE_HEARTBEAT_PORT;
        final KubernetesClient fabricClient = initClient(getTestClusterInfo());

        final Configs configs = new EnvConfigs();
        final WorkerConfigs workerConfigs = new WorkerConfigs(configs);

        KubePortManagerSingleton.init(Constant.PORTS);
        ProcessFactory processFactory = new KubeProcessFactory(workerConfigs,
                //configs.getJobKubeNamespace(),
                "cbs-flink",
                fabricClient,
                kubeHeartbeatUrl,
                false);

        System.out.println(processFactory);

        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, "internal", DUMMY_IMAGE_NAME,
                processFactory, workerConfigs.getResourceRequirements());

        StandardCheckConnectionInput input = new StandardCheckConnectionInput()
                .withConnectionConfiguration(CREDS);

        process = launcher.check(
                jobRoot,
                WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
                Jsons.serialize(input.getConnectionConfiguration()));


        final Optional<AirbyteConnectionStatus> status;
        try (final InputStream stdout = process.getInputStream()) {
            status = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.CONNECTION_STATUS)
                    .map(AirbyteMessage::getConnectionStatus).findFirst();

            WorkerUtils.gentleClose(workerConfigs, process, 1, TimeUnit.MINUTES);
        }

        final int exitCode = process.exitValue();

        if (status.isPresent() && exitCode == 0) {
            final StandardCheckConnectionOutput output = new StandardCheckConnectionOutput()
                    .withStatus(Enums.convertTo(status.get().getStatus(), StandardCheckConnectionOutput.Status.class))
                    .withMessage(status.get().getMessage());

            //return output;
            System.out.println("output is " + output);
        } else {
            throw new WorkerException(String.format("Error checking connection, status: %s, exit code: %d", status, exitCode));
        }
    }

    @Test
    public void testDiscover() throws Exception {
        //InetAddress.getLocalHost().getHostAddress();
        System.out.println("local IP is " + LOCAL_IP);
        final String kubeHeartbeatUrl = LOCAL_IP + ":" + KUBE_HEARTBEAT_PORT;
        final KubernetesClient fabricClient = initClient(getTestClusterInfo());

        final Configs configs = new EnvConfigs();
        final WorkerConfigs workerConfigs = new WorkerConfigs(configs);

        KubePortManagerSingleton.init(Constant.PORTS);
        ProcessFactory processFactory = new KubeProcessFactory(workerConfigs,
                //configs.getJobKubeNamespace(),
                "cbs-flink",
                fabricClient,
                kubeHeartbeatUrl,
                false);

        System.out.println(processFactory);

        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, "internal", DUMMY_IMAGE_NAME,
                processFactory, workerConfigs.getResourceRequirements());

        StandardCheckConnectionInput input = new StandardCheckConnectionInput()
                .withConnectionConfiguration(CREDS);

        process = launcher.discover(
                jobRoot,
                WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
                Jsons.serialize(input.getConnectionConfiguration()));


        final Optional<AirbyteCatalog> catalog;
        try (final InputStream stdout = process.getInputStream()) {
            catalog = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.CATALOG)
                    .map(AirbyteMessage::getCatalog)
                    .findFirst();

            WorkerUtils.gentleClose(workerConfigs, process, 30, TimeUnit.MINUTES);
        }

        final int exitCode = process.exitValue();
        if (exitCode == 0) {
            if (!catalog.isPresent()) {
                throw new WorkerException("Integration failed to output a catalog struct.");
            }

            System.out.println("out put is " + Jsons.serialize(catalog.get()));
        } else {
            throw new WorkerException(String.format("Discover job subprocess finished with exit code %s", exitCode));
        }
    }

    @Test
    public void testConnect() {
        System.out.println(CREDS.toString());
    }


    private KubernetesClient initClient(K8SClusterInfo k8SClusterInfo) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.withMasterUrl(k8SClusterInfo.getHost());
        configBuilder.withTrustCerts(true);
        configBuilder.withOauthToken(k8SClusterInfo.getToken());
        configBuilder.withCaCertData(k8SClusterInfo.getCaCrt());
        return new DefaultKubernetesClient(configBuilder.build());
    }

    private K8SClusterInfo getTestClusterInfo() {
        K8SClusterInfo test = new K8SClusterInfo();
        return test;
    }

}
