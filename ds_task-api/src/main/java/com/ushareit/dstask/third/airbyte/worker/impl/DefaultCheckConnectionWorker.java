/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker.impl;

import com.ushareit.dstask.third.airbyte.AirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.DefaultAirbyteStreamFactory;
import com.ushareit.dstask.third.airbyte.IntegrationLauncher;
import com.ushareit.dstask.third.airbyte.common.enums.Enums;
import com.ushareit.dstask.third.airbyte.common.io.IOs;
import com.ushareit.dstask.third.airbyte.common.io.LineGobbler;
import com.ushareit.dstask.third.airbyte.config.*;
import com.ushareit.dstask.third.airbyte.exception.WorkerException;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.airbyte.utils.WorkerConstants;
import com.ushareit.dstask.third.airbyte.utils.WorkerUtils;
import com.ushareit.dstask.third.airbyte.worker.CheckConnectionWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DefaultCheckConnectionWorker implements CheckConnectionWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCheckConnectionWorker.class);

  private final WorkerConfigs workerConfigs;
  private final IntegrationLauncher integrationLauncher;
  private final AirbyteStreamFactory streamFactory;

  private Process process;

  public DefaultCheckConnectionWorker(final WorkerConfigs workerConfigs,
                                      final IntegrationLauncher integrationLauncher,
                                      final AirbyteStreamFactory streamFactory) {
    this.workerConfigs = workerConfigs;
    this.integrationLauncher = integrationLauncher;
    this.streamFactory = streamFactory;
  }

  public DefaultCheckConnectionWorker(final WorkerConfigs workerConfigs, final IntegrationLauncher integrationLauncher) {
    this(workerConfigs, integrationLauncher, new DefaultAirbyteStreamFactory());
  }

  @Override
  public StandardCheckConnectionOutput run(final StandardCheckConnectionInput input, final Path jobRoot) throws WorkerException {

    try {
      process = integrationLauncher.check(
          jobRoot,
          WorkerConstants.SOURCE_CONFIG_JSON_FILENAME,
          Jsons.serialize(input.getConnectionConfiguration()));

      LineGobbler.gobble(process.getErrorStream(), LOGGER::error);

      final Optional<AirbyteConnectionStatus> status;
      try (final InputStream stdout = process.getInputStream()) {
        status = streamFactory.create(IOs.newBufferedReader(stdout))
            .filter(message -> message.getType() == AirbyteMessage.Type.CONNECTION_STATUS)
            .map(AirbyteMessage::getConnectionStatus).findFirst();

        WorkerUtils.gentleClose(workerConfigs, process, 1, TimeUnit.MINUTES);
      }

      final int exitCode = process.exitValue();

      if (status.isPresent() && exitCode == 0) {
        final StandardCheckConnectionOutput output = new StandardCheckConnectionOutput()
            .withStatus(Enums.convertTo(status.get().getStatus(), StandardCheckConnectionOutput.Status.class))
            .withMessage(status.get().getMessage());

        LOGGER.debug("Check connection job subprocess finished with exit code {}", exitCode);
        LOGGER.debug("Check connection job received output: {}", output);
        return output;
      } else {
        throw new WorkerException(String.format("Error checking connection, status: %s, exit code: %d", status, exitCode));
      }

    } catch (final Exception e) {
      throw new WorkerException("Error while getting checking connection.", e);
    }
  }

  @Override
  public void cancel() {
    WorkerUtils.cancelProcess(process);
  }

}
