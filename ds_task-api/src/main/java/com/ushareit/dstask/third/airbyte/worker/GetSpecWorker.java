/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker;

import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;
import com.ushareit.dstask.third.airbyte.config.JobGetSpecConfig;

public interface GetSpecWorker extends Worker<JobGetSpecConfig, ConnectorSpecification> {
}
