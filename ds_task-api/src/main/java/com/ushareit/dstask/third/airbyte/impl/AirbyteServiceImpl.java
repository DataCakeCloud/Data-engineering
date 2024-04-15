package com.ushareit.dstask.third.airbyte.impl;

import com.ushareit.dstask.configuration.K8sConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.airbyte.*;
import com.ushareit.dstask.third.airbyte.common.Constant;
import com.ushareit.dstask.third.airbyte.common.enums.Enums;
import com.ushareit.dstask.third.airbyte.common.io.IOs;
import com.ushareit.dstask.third.airbyte.config.*;
import com.ushareit.dstask.third.airbyte.k8s.KubePortManagerSingleton;
import com.ushareit.dstask.third.airbyte.k8s.KubeProcessFactory;
import com.ushareit.dstask.third.airbyte.utils.PathUtil;
import com.ushareit.dstask.third.airbyte.utils.WorkerConstants;
import com.ushareit.dstask.third.airbyte.utils.WorkerUtils;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author fengxiao
 * @date 2022/7/15
 */
@Slf4j
@Service
public class AirbyteServiceImpl implements AirbyteService {

    public static final int KUBE_HEARTBEAT_PORT = 8088;
    private static final String JOB_ID = "0";
    private static final int JOB_ATTEMPT = 0;
    private static final Path JOB_ROOT = PathUtil.of("/data/code/");
    private ProcessFactory processFactory;
    private WorkerConfigs workerConfigs;

    @Value("${spring.profiles.active}")
    private String env;
    @Autowired
    private K8sConfig k8sConfig;

    @Autowired
    private KubernetesClient fabricClient;

    @PostConstruct
    public void init() {
        KubePortManagerSingleton.init(Constant.PORTS);
        try {
            final String localIp = InetAddress.getLocalHost().getHostAddress();
            final String kubeHeartbeatUrl = localIp + ":" + KUBE_HEARTBEAT_PORT;

            final Configs configs = new EnvConfigs();
            workerConfigs = new WorkerConfigs(configs);

            processFactory = new KubeProcessFactory(workerConfigs,
                    k8sConfig.getNamespace(),
                    fabricClient,
                    kubeHeartbeatUrl,
                    false);

            log.info("initialize process factory success!!, env is{}, current namespace is {}", env, k8sConfig.getNamespace());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ConnectorSpecification spec(String imageName) throws Exception {
        log.info("process factory is {}", processFactory);
        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, k8sConfig.getKubeContext(),
                imageName, processFactory, workerConfigs.getResourceRequirements());

        Process process = launcher.spec(JOB_ROOT);

        final Optional<ConnectorSpecification> spec;
        try (final InputStream stdout = process.getInputStream()) {
            spec = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.SPEC)
                    .map(AirbyteMessage::getSpec)
                    .findFirst();
        } finally {
            WorkerUtils.gentleClose(workerConfigs, process, 30, TimeUnit.MINUTES);
        }
        return spec.orElse(null);
    }

    @Override
    public boolean check(String imageName, String connectConfiguration) throws Exception {
        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, k8sConfig.getKubeContext(),
                imageName, processFactory, workerConfigs.getResourceRequirements());

        log.info(" connectConfiguration is :" + connectConfiguration);
        Process process = launcher.check(
                JOB_ROOT,
                WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
                connectConfiguration);

        final Optional<AirbyteConnectionStatus> status;
        try (final InputStream stdout = process.getInputStream()) {
            status = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.CONNECTION_STATUS)
                    .map(AirbyteMessage::getConnectionStatus).findFirst();

            final int exitCode = process.exitValue();
            if (status.isPresent() && exitCode == 0) {
                final StandardCheckConnectionOutput output = new StandardCheckConnectionOutput()
                        .withStatus(Enums.convertTo(status.get().getStatus(), StandardCheckConnectionOutput.Status.class))
                        .withMessage(status.get().getMessage());

                return output.getStatus() == StandardCheckConnectionOutput.Status.SUCCEEDED;
            } else {
                throw new RuntimeException(String.format("连接失败, 状态: %s, 错误码: %d", status, exitCode));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), e.getMessage());
        } finally {
            WorkerUtils.gentleClose(workerConfigs, process, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public AirbyteCatalog discover(String imageName, String connectConfiguration) throws Exception {
        IntegrationLauncher launcher = new AirbyteIntegrationLauncher(JOB_ID, JOB_ATTEMPT, k8sConfig.getKubeContext(),
                imageName, processFactory, workerConfigs.getResourceRequirements());

        Process process = launcher.discover(
                JOB_ROOT,
                WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
                connectConfiguration);

        final Optional<AirbyteCatalog> catalog;
        try (final InputStream stdout = process.getInputStream()) {
            catalog = new DefaultAirbyteStreamFactory().create(IOs.newBufferedReader(stdout))
                    .filter(message -> message.getType() == AirbyteMessage.Type.CATALOG)
                    .map(AirbyteMessage::getCatalog)
                    .findFirst();

            final int exitCode = process.exitValue();
            if (exitCode == 0) {
                if (!catalog.isPresent()) {
                    throw new RuntimeException("获取数据源表结构失败");
                }

                return catalog.get();
            } else {
                throw new RuntimeException(String.format("获取数据源表结构失败, 错误码 %s", exitCode));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), e.getMessage());
        } finally {
            WorkerUtils.gentleClose(workerConfigs, process, 30, TimeUnit.MINUTES);
        }
    }
}
