/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.k8s;

import java.util.concurrent.TimeUnit;

public interface KubePod {

  int exitValue();

  void destroy();

  boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException;

  int waitFor() throws InterruptedException;

  KubePodInfo getInfo();

}
