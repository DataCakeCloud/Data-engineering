/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.third.airbyte.Configs;
import com.ushareit.dstask.third.airbyte.config.*;
import com.ushareit.dstask.third.airbyte.k8s.KubePodProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// TODO:(Issue-4824): Figure out how to log Docker process information.
public class WorkerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerUtils.class);

    public static void gentleClose(final WorkerConfigs workerConfigs, final Process process, final long timeout, final TimeUnit timeUnit) {

        if (process == null) {
            return;
        }

        if (workerConfigs.getWorkerEnvironment().equals(Configs.WorkerEnvironment.KUBERNETES) && process instanceof KubePodProcess) {
            LOGGER.info("Gently closing process {}.", ((KubePodProcess) process).info().commandLine().get());
        }

        try {
            if (process.isAlive()) {
                process.waitFor(timeout, timeUnit);
            }
        } catch (final InterruptedException e) {
            LOGGER.error("Exception while while waiting for process to finish", e);
        }

        closeProcess(process, Duration.of(1, ChronoUnit.MINUTES));
    }

    public static void closeProcess(final Process process, final Duration lastChanceDuration) {
        if (process == null) {
            return;
        }
        try {
            process.destroy();
            process.waitFor(lastChanceDuration.toMillis(), TimeUnit.MILLISECONDS);
            if (process.isAlive()) {
                LOGGER.warn("Process is still alive after calling destroy. Attempting to destroy forcibly...");
                process.destroyForcibly();
            }
        } catch (final InterruptedException e) {
            LOGGER.error("Exception when closing process.", e);
        }
    }

    public static void wait(final Process process) {
        try {
            process.waitFor();
        } catch (final InterruptedException e) {
            LOGGER.error("Exception while while waiting for process to finish", e);
        }
    }

    public static void cancelProcess(final Process process) {
        closeProcess(process, Duration.of(10, ChronoUnit.SECONDS));
    }

    /**
     * Translates a StandardSyncInput into a WorkerSourceConfig. WorkerSourceConfig is a subset of
     * StandardSyncInput.
     */
    public static WorkerSourceConfig syncToWorkerSourceConfig(final StandardSyncInput sync) {
        return new WorkerSourceConfig()
                .withSourceConnectionConfiguration(sync.getSourceConfiguration())
                .withCatalog(sync.getCatalog())
                .withState(sync.getState());
    }

    /**
     * Translates a StandardSyncInput into a WorkerDestinationConfig. WorkerDestinationConfig is a
     * subset of StandardSyncInput.
     */
    public static WorkerDestinationConfig syncToWorkerDestinationConfig(final StandardSyncInput sync) {
        return new WorkerDestinationConfig()
                .withDestinationConnectionConfiguration(sync.getDestinationConfiguration())
                .withCatalog(sync.getCatalog())
                .withState(sync.getState());
    }

    public static Map<String, JsonNode> mapStreamNamesToSchemas(final StandardSyncInput syncInput) {
        return syncInput.getCatalog().getStreams().stream().collect(
                Collectors.toMap(
                        k -> {
                            return streamNameWithNamespace(k.getStream().getNamespace(), k.getStream().getName());
                        },
                        v -> v.getStream().getJsonSchema()));

    }

    public static String streamNameWithNamespace(final @Nullable String namespace, final String streamName) {
        return Objects.toString(namespace, "").trim() + streamName.trim();
    }

    // todo (cgardens) - there are 2 sources of truth for job path. we need to reduce this down to one,
    // once we are fully on temporal.
    public static Path getJobRoot(final Path workspaceRoot, final JobRunConfig jobRunConfig) {
        return getJobRoot(workspaceRoot, jobRunConfig.getJobId(), jobRunConfig.getAttemptId());
    }

    public static Path getJobRoot(final Path workspaceRoot, final String jobId, final long attemptId) {
        return getJobRoot(workspaceRoot, jobId, Math.toIntExact(attemptId));
    }

    public static Path getJobRoot(final Path workspaceRoot, final String jobId, final int attemptId) {
        return workspaceRoot
                .resolve(String.valueOf(jobId))
                .resolve(String.valueOf(attemptId));
    }

}
