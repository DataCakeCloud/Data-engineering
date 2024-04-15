/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker.impl;

import com.ushareit.dstask.third.airbyte.AirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.DefaultAirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.IntegrationLauncher;
import com.ushareit.dstask.third.airbyte.common.io.IOs;
import com.ushareit.dstask.third.airbyte.common.io.LineGobbler;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.config.AirbyteMessage;
import com.ushareit.dstask.third.airbyte.config.StandardDiscoverCatalogInput;
import com.ushareit.dstask.third.airbyte.config.WorkerConfigs;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.airbyte.utils.WorkerConstants;
import com.ushareit.dstask.third.airbyte.utils.WorkerUtils;
import com.ushareit.dstask.third.airbyte.worker.DiscoverCatalogWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DefaultDiscoverCatalogWorker implements DiscoverCatalogWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDiscoverCatalogWorker.class);

    private final WorkerConfigs workerConfigs;
    private final IntegrationLauncher integrationLauncher;
    private final AirbyteStreamFactory streamFactory;

    private volatile Process process;

    public DefaultDiscoverCatalogWorker(final WorkerConfigs workerConfigs,
                                        final IntegrationLauncher integrationLauncher,
                                        final AirbyteStreamFactory streamFactory) {
        this.workerConfigs = workerConfigs;
        this.integrationLauncher = integrationLauncher;
        this.streamFactory = streamFactory;
    }

    public DefaultDiscoverCatalogWorker(final WorkerConfigs workerConfigs, final IntegrationLauncher integrationLauncher) {
        this(workerConfigs, integrationLauncher, new DefaultAirbyteStreamFactory());
    }

    @Override
    public AirbyteCatalog run(final StandardDiscoverCatalogInput discoverSchemaInput, final Path jobRoot) throws WorkerException {
        try {
            process = integrationLauncher.discover(
                    jobRoot,
                    WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
                    Jsons.serialize(discoverSchemaInput.getConnectionConfiguration()));

            LineGobbler.gobble(process.getErrorStream(), LOGGER::error);

            final Optional<AirbyteCatalog> catalog;
            try (final InputStream stdout = process.getInputStream()) {
                catalog = streamFactory.create(IOs.newBufferedReader(stdout))
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

                return catalog.get();
            } else {
                throw new WorkerException(String.format("Discover job subprocess finished with exit code %s", exitCode));
            }
        } catch (final WorkerException e) {
            throw e;
        } catch (final Exception e) {
            throw new WorkerException("Error while discovering schema", e);
        }
    }

    @Override
    public void cancel() {
        WorkerUtils.cancelProcess(process);
    }

}
