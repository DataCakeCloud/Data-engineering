/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.worker;

import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.config.StandardDiscoverCatalogInput;

public interface DiscoverCatalogWorker extends Worker<StandardDiscoverCatalogInput, AirbyteCatalog> {
}
