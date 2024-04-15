/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ushareit.dstask.third.airbyte.IntegrationLauncher;
import com.ushareit.dstask.third.airbyte.ProcessFactory;
import com.ushareit.dstask.third.airbyte.common.WorkerEnvConstants;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.config.ResourceRequirements;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirbyteIntegrationLauncher implements IntegrationLauncher {

    /**
     * The following variables help, either via names or labels, add metadata to processes actually
     * running operations. These are more readable forms of
     * {@link com.ushareit.dstask.third.airbyte.config.JobTypeResourceLimit.JobType}.
     */
    public static final String JOB_TYPE = "job_type";
    public static final String SYNC_JOB = "entrypoints/sync";
    public static final String SPEC_JOB = "spec";
    public static final String CHECK_JOB = "check";
    public static final String DISCOVER_JOB = "discover";

    /**
     * A sync job can actually be broken down into the following steps. Try to be as precise as possible
     * with naming/labels to help operations.
     */
    public static final String SYNC_STEP = "sync_step";
    public static final String READ_STEP = "read";
    public static final String WRITE_STEP = "write";
    public static final String NORMALIZE_STEP = "normalize";
    public static final String CUSTOM_STEP = "custom";

    private final String jobId;
    private final int attempt;
    private final String imageName;
    private final ProcessFactory processFactory;
    private final ResourceRequirements resourceRequirement;
    private final String kubeContext;

    public AirbyteIntegrationLauncher(final String jobId,
                                      final int attempt,
                                      final String kubeContext,
                                      final String imageName,
                                      final ProcessFactory processFactory,
                                      final ResourceRequirements resourceRequirement) {
        this.jobId = jobId;
        this.attempt = attempt;
        this.kubeContext = kubeContext;
        this.imageName = imageName;
        this.processFactory = processFactory;
        this.resourceRequirement = resourceRequirement;
    }

    @Override
    public Process spec(final Path jobRoot) throws WorkerException {
        return processFactory.create(
                SPEC_JOB,
                jobId,
                attempt,
                jobRoot,
                kubeContext,
                imageName,
                false,
                Collections.emptyMap(),
                null,
                resourceRequirement,
                ImmutableMap.of(JOB_TYPE, SPEC_JOB),
                getWorkerMetadata(),
                Collections.emptyMap(),
                "spec");
    }

    @Override
    public Process check(final Path jobRoot, final String configFilename, final String configContents) throws WorkerException {
        return processFactory.create(
                CHECK_JOB,
                jobId,
                attempt,
                jobRoot,
                kubeContext,
                imageName,
                false,
                ImmutableMap.of(configFilename, configContents),
                null,
                resourceRequirement,
                ImmutableMap.of(JOB_TYPE, CHECK_JOB),
                getWorkerMetadata(),
                Collections.emptyMap(),
                "check",
                "--config", configFilename);
    }

    @Override
    public Process discover(final Path jobRoot, final String configFilename, final String configContents) throws WorkerException {
        return processFactory.create(
                DISCOVER_JOB,
                jobId,
                attempt,
                jobRoot,
                kubeContext,
                imageName,
                false,
                ImmutableMap.of(configFilename, configContents),
                null,
                resourceRequirement,
                ImmutableMap.of(JOB_TYPE, DISCOVER_JOB),
                getWorkerMetadata(),
                Collections.emptyMap(),
                "discover",
                "--config", configFilename);
    }

    @Override
    public Process read(final Path jobRoot,
                        final String configFilename,
                        final String configContents,
                        final String catalogFilename,
                        final String catalogContents,
                        final String stateFilename,
                        final String stateContents)
            throws WorkerException {
        final List<String> arguments = Lists.newArrayList(
                "read",
                "--config", configFilename,
                "--catalog", catalogFilename);

        final Map<String, String> files = new HashMap<>();
        files.put(configFilename, configContents);
        files.put(catalogFilename, catalogContents);

        if (stateFilename != null) {
            arguments.add("--state");
            arguments.add(stateFilename);

            Preconditions.checkNotNull(stateContents);
            files.put(stateFilename, stateContents);
        }

        return processFactory.create(
                READ_STEP,
                jobId,
                attempt,
                jobRoot,
                kubeContext,
                imageName,
                false,
                files,
                null,
                resourceRequirement,
                ImmutableMap.of(JOB_TYPE, SYNC_JOB, SYNC_STEP, READ_STEP),
                getWorkerMetadata(),
                Collections.emptyMap(),
                arguments.toArray(new String[arguments.size()]));
    }

    @Override
    public Process write(final Path jobRoot,
                         final String configFilename,
                         final String configContents,
                         final String catalogFilename,
                         final String catalogContents)
            throws WorkerException {
        final Map<String, String> files = ImmutableMap.of(
                configFilename, configContents,
                catalogFilename, catalogContents);

        return processFactory.create(
                WRITE_STEP,
                jobId,
                attempt,
                jobRoot,
                kubeContext,
                imageName,
                true,
                files,
                null,
                resourceRequirement,
                ImmutableMap.of(JOB_TYPE, SYNC_JOB, SYNC_STEP, WRITE_STEP),
                getWorkerMetadata(),
                Collections.emptyMap(),
                "write",
                "--config", configFilename,
                "--catalog", catalogFilename);
    }

    private Map<String, String> getWorkerMetadata() {
        Map<String, String> tmp = new HashMap<>();
        tmp.put(WorkerEnvConstants.WORKER_CONNECTOR_IMAGE, imageName);
        tmp.put(WorkerEnvConstants.WORKER_JOB_ID, jobId);
        tmp.put(WorkerEnvConstants.WORKER_JOB_ATTEMPT, String.valueOf(attempt));
        return tmp;
    }

}
