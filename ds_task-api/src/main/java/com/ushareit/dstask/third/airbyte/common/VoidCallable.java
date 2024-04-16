/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.common;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface VoidCallable extends Callable<Void> {

  VoidCallable NOOP = () -> {};

  default @Override Void call() throws Exception {
    voidCall();
    return null;
  }

  void voidCall() throws Exception;

}
