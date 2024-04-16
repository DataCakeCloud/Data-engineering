/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker.impl;

import com.ushareit.dstask.third.airbyte.AirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.DefaultAirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.IntegrationLauncher;
import com.ushareit.dstask.third.airbyte.common.io.IOs;
import com.ushareit.dstask.third.airbyte.common.io.LineGobbler;
import com.ushareit.dstask.third.airbyte.config.AirbyteMessage;
import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;
import com.ushareit.dstask.third.airbyte.config.JobGetSpecConfig;
import com.ushareit.dstask.third.airbyte.config.WorkerConfigs;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.utils.WorkerUtils;
import com.ushareit.dstask.third.airbyte.worker.GetSpecWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DefaultGetSpecWorker implements GetSpecWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGetSpecWorker.class);

    private final WorkerConfigs workerConfigs;
    private final IntegrationLauncher integrationLauncher;
    private final AirbyteStreamFactory streamFactory;

    private Process process;

    public DefaultGetSpecWorker(final WorkerConfigs workerConfigs,
                                final IntegrationLauncher integrationLauncher,
                                final AirbyteStreamFactory streamFactory) {
        this.workerConfigs = workerConfigs;
        this.integrationLauncher = integrationLauncher;
        this.streamFactory = streamFactory;
    }

    public DefaultGetSpecWorker(final WorkerConfigs workerConfigs, final IntegrationLauncher integrationLauncher) {
        this(workerConfigs, integrationLauncher, new DefaultAirbyteStreamFactory());
    }

    @Override
    public ConnectorSpecification run(final JobGetSpecConfig config, final Path jobRoot) throws WorkerException {
        try {
            process = integrationLauncher.spec(jobRoot);

            LineGobbler.gobble(process.getErrorStream(), LOGGER::error);

            final Optional<ConnectorSpecification> spec;
            try (final InputStream stdout = process.getInputStream()) {
                spec = streamFactory.create(IOs.newBufferedReader(stdout))
                        .filter(message -> message.getType() == AirbyteMessage.Type.SPEC)
                        .map(AirbyteMessage::getSpec)
                        .findFirst();

                // todo (cgardens) - let's pre-fetch the images outside of the worker so we don't need account for
                // this.
                // retrieving spec should generally be instantaneous, but since docker images might not be pulled
                // it could take a while longer depending on internet conditions as well.
                WorkerUtils.gentleClose(workerConfigs, process, 30, TimeUnit.MINUTES);
            }

            final int exitCode = process.exitValue();
            if (exitCode == 0) {
                if (!spec.isPresent()) {
                    throw new WorkerException("integration failed to output a spec struct.");
                }

                return spec.get();

            } else {
                throw new WorkerException(String.format("Spec job subprocess finished with exit code %s", exitCode));
            }
        } catch (final Exception e) {
            throw new WorkerException(String.format("Error while getting spec from image %s", config.getDockerImage()), e);
        }

    }

    @Override
    public void cancel() {
        WorkerUtils.cancelProcess(process);
    }

}
