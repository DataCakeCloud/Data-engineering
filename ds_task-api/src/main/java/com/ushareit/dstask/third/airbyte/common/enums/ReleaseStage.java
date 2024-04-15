package com.ushareit.dstask.third.airbyte.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ReleaseStage
 */
public enum ReleaseStage {
  
  ALPHA("alpha"),
  
  BETA("beta"),
  
  GENERALLY_AVAILABLE("generally_available"),
  
  CUSTOM("custom");

  private String value;

  ReleaseStage(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ReleaseStage fromValue(String value) {
    for (ReleaseStage b : ReleaseStage.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


