/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker;

import com.ushareit.dstask.third.airbyte.config.StandardCheckConnectionInput;
import com.ushareit.dstask.third.airbyte.config.StandardCheckConnectionOutput;

public interface CheckConnectionWorker extends Worker<StandardCheckConnectionInput, StandardCheckConnectionOutput> {
}
