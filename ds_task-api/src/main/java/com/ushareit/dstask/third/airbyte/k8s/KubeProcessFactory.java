/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.k8s;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.ushareit.dstask.third.airbyte.ProcessFactory;
import com.ushareit.dstask.third.airbyte.common.lang.Exceptions;
import com.ushareit.dstask.third.airbyte.common.map.MoreMaps;
import com.ushareit.dstask.third.airbyte.config.ResourceRequirements;
import com.ushareit.dstask.third.airbyte.config.WorkerConfigs;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.utils.ProcessFactoryUtil;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class KubeProcessFactory implements ProcessFactory {

    @VisibleForTesting
    public static final int KUBE_NAME_LEN_LIMIT = 63;

    private static final Logger LOGGER = LoggerFactory.getLogger(KubeProcessFactory.class);

    private static final String JOB_LABEL_KEY = "job_id";
    private static final String ATTEMPT_LABEL_KEY = "attempt_id";
    private static final String WORKER_POD_LABEL_KEY = "airbyte";
    private static final String WORKER_POD_LABEL_VALUE = "worker-pod";

    private final WorkerConfigs workerConfigs;
    private final String namespace;
    private final KubernetesClient fabricClient;
    private final String kubeHeartbeatUrl;
    private final String processRunnerHost;
    private final boolean isOrchestrator;

    /**
     * Sets up a process factory with the default processRunnerHost.
     */
    public KubeProcessFactory(final WorkerConfigs workerConfigs,
                              final String namespace,
                              final KubernetesClient fabricClient,
                              final String kubeHeartbeatUrl,
                              final boolean isOrchestrator) {
        this(
                workerConfigs,
                namespace,
                fabricClient,
                kubeHeartbeatUrl,
                Exceptions.toRuntime(() -> InetAddress.getLocalHost().getHostAddress()),
                //"192.168.8.67",
                isOrchestrator);
    }

    /**
     * @param namespace         kubernetes namespace where spawned pods will live
     * @param fabricClient      fabric8 kubernetes client
     * @param kubeHeartbeatUrl  a url where if the response is not 200 the spawned process will fail
     *                          itself
     * @param processRunnerHost is the local host or ip of the machine running the process factory.
     *                          injectable for testing.
     * @param isOrchestrator    determines if this should run as airbyte-admin
     */
    @VisibleForTesting
    public KubeProcessFactory(final WorkerConfigs workerConfigs,
                              final String namespace,
                              final KubernetesClient fabricClient,
                              final String kubeHeartbeatUrl,
                              final String processRunnerHost,
                              final boolean isOrchestrator) {
        this.workerConfigs = workerConfigs;
        this.namespace = namespace;
        this.fabricClient = fabricClient;
        this.kubeHeartbeatUrl = kubeHeartbeatUrl;
        this.processRunnerHost = processRunnerHost;
        this.isOrchestrator = isOrchestrator;
    }

    @Override
    public Process create(
            final String jobType,
            final String jobId,
            final int attempt,
            final Path jobRoot,
            final String kubeContext,
            final String imageName,
            final boolean usesStdin,
            final Map<String, String> files,
            final String entrypoint,
            final ResourceRequirements resourceRequirements,
            final Map<String, String> customLabels,
            final Map<String, String> jobMetadata,
            final Map<Integer, Integer> internalToExternalPorts,
            final String... args)
            throws WorkerException {
        try {
            // used to differentiate source and destination processes with the same id and attempt
            final String podName = ProcessFactoryUtil.createProcessName(imageName, jobType, jobId, attempt, KUBE_NAME_LEN_LIMIT);
            LOGGER.info("Attempting to start pod = {} for {} from namespace {}", podName, imageName, namespace);

            final int stdoutLocalPort = KubePortManagerSingleton.getInstance().take();
            LOGGER.info("{} stdoutLocalPort = {}", podName, stdoutLocalPort);

            final int stderrLocalPort = KubePortManagerSingleton.getInstance().take();
            LOGGER.info("{} stderrLocalPort = {}", podName, stderrLocalPort);

            final Map<String, String> allLabels = getLabels(jobId, attempt, customLabels);

            return new KubePodProcess(
                    isOrchestrator,
                    processRunnerHost,
                    fabricClient,
                    kubeContext,
                    podName,
                    namespace,
                    imageName,
                    workerConfigs.getJobImagePullPolicy(),
                    workerConfigs.getSidecarImagePullPolicy(),
                    stdoutLocalPort,
                    stderrLocalPort,
                    kubeHeartbeatUrl,
                    usesStdin,
                    files,
                    entrypoint,
                    resourceRequirements,
                    workerConfigs.getJobImagePullSecret(),
                    workerConfigs.getWorkerKubeTolerations(),
                    workerConfigs.getworkerKubeNodeSelectors(),
                    allLabels,
                    workerConfigs.getWorkerKubeAnnotations(),
                    workerConfigs.getJobSocatImage(),
                    workerConfigs.getJobBusyboxImage(),
                    workerConfigs.getJobCurlImage(),
                    MoreMaps.merge(jobMetadata, workerConfigs.getEnvMap()),
                    internalToExternalPorts,
                    args);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new WorkerException(e.getMessage(), e);
        }
    }

    public static Map<String, String> getLabels(final String jobId, final int attemptId, final Map<String, String> customLabels) {
        final Map<String, String> allLabels = new HashMap<>(customLabels);

        final Map<String, String> generalKubeLabels = ImmutableMap.of(
                JOB_LABEL_KEY, jobId,
                ATTEMPT_LABEL_KEY, String.valueOf(attemptId),
                WORKER_POD_LABEL_KEY, WORKER_POD_LABEL_VALUE);

        allLabels.putAll(generalKubeLabels);

        return allLabels;
    }

}
