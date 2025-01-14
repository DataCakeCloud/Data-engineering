/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * The {@link JavaTimeModule} allows mappers to accommodate different varieties of serialised date
 * time strings.
 *
 * All jackson mapper creation should use the following methods for instantiation.
 */
public class MoreMappers {

  public static ObjectMapper initMapper() {
    final ObjectMapper result = new ObjectMapper().registerModule(new JavaTimeModule());
    result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return result;
  }

  public static ObjectMapper initYamlMapper(final YAMLFactory factory) {
    return new ObjectMapper(factory).registerModule(new JavaTimeModule());
  }

}
