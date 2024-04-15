package com.ushareit.dstask.third.airbyte.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets JobConfigType
 */
public enum JobConfigType {
  
  CHECK_CONNECTION_SOURCE("check_connection_source"),
  
  CHECK_CONNECTION_DESTINATION("check_connection_destination"),
  
  DISCOVER_SCHEMA("discover_schema"),
  
  GET_SPEC("get_spec"),
  
  SYNC("sync"),
  
  RESET_CONNECTION("reset_connection");

  private String value;

  JobConfigType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static JobConfigType fromValue(String value) {
    for (JobConfigType b : JobConfigType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


