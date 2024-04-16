/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.json;

public class JsonValidationException extends Exception {

  public JsonValidationException(final String message) {
    super(message);
  }

  public JsonValidationException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
