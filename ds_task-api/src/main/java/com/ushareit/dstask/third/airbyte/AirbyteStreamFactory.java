/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte;

import com.ushareit.dstask.third.airbyte.config.AirbyteMessage;

import java.io.BufferedReader;
import java.util.stream.Stream;

public interface AirbyteStreamFactory {

    Stream<AirbyteMessage> create(BufferedReader bufferedReader);

}
