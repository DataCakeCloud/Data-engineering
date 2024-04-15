/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.third.airbyte.common.logging.MdcScope;
import com.ushareit.dstask.third.airbyte.config.AirbyteLogMessage;
import com.ushareit.dstask.third.airbyte.config.AirbyteMessage;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Creates a stream from an input stream. The produced stream attempts to parse each line of the
 * InputStream into a AirbyteMessage. If the line cannot be parsed into a AirbyteMessage it is
 * dropped. Each record MUST be new line separated.
 *
 * <p>
 * If a line starts with a AirbyteMessage and then has other characters after it, that
 * AirbyteMessage will still be parsed. If there are multiple AirbyteMessage records on the same
 * line, only the first will be parsed.
 */
public class DefaultAirbyteStreamFactory implements AirbyteStreamFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAirbyteStreamFactory.class);

    private final MdcScope.Builder containerLogMdcBuilder;
    private final AirbyteProtocolPredicate protocolValidator;
    private final Logger logger;

    public DefaultAirbyteStreamFactory() {
        this(MdcScope.DEFAULT_BUILDER);
    }

    public DefaultAirbyteStreamFactory(final MdcScope.Builder containerLogMdcBuilder) {
        this(new AirbyteProtocolPredicate(), LOGGER, containerLogMdcBuilder);
    }

    DefaultAirbyteStreamFactory(final AirbyteProtocolPredicate protocolPredicate, final Logger logger, final MdcScope.Builder containerLogMdcBuilder) {
        protocolValidator = protocolPredicate;
        this.logger = logger;
        this.containerLogMdcBuilder = containerLogMdcBuilder;
    }

    @Override
    public Stream<AirbyteMessage> create(final BufferedReader bufferedReader) {
        return bufferedReader.lines().flatMap(line -> {
                    final Optional<JsonNode> jsonLine = Jsons.tryDeserialize(line);
                    if (!jsonLine.isPresent()) {
                        // we log as info all the lines that are not valid json
                        // some sources actually log their process on stdout, we
                        // want to make sure this info is available in the logs.
                        try (final MdcScope mdcScope = containerLogMdcBuilder.build()) {
                            logger.info(line);
                        }
                    }
                    return jsonLine.map(Stream::of).orElseGet(Stream::empty);
                })
                // filter invalid messages
                .filter(jsonLine -> {
                    final boolean res = protocolValidator.test(jsonLine);
                    if (!res) {
                        logger.error("Validation failed: {}", Jsons.serialize(jsonLine));
                    }
                    return res;
                }).flatMap(jsonLine -> {
                    final Optional<AirbyteMessage> m = Jsons.tryObject(jsonLine, AirbyteMessage.class);
                    if (!m.isPresent()) {
                        logger.error("Deserialization failed: {}", Jsons.serialize(jsonLine));
                    }
                    return m.map(Stream::of).orElseGet(Stream::empty);
                })
                // filter logs
                .filter(airbyteMessage -> {
                    final boolean isLog = airbyteMessage.getType() == AirbyteMessage.Type.LOG;
                    if (isLog) {
                        try (final MdcScope mdcScope = containerLogMdcBuilder.build()) {
                            internalLog(airbyteMessage.getLog());
                        }
                    }
                    return !isLog;
                });
    }

    private void internalLog(final AirbyteLogMessage logMessage) {
        switch (logMessage.getLevel()) {
            case FATAL:
            case ERROR:
                logger.error(logMessage.getMessage());
                break;
            case WARN:
                logger.warn(logMessage.getMessage());
                break;
            case INFO:
                logger.info(logMessage.getMessage());
                break;
            case DEBUG:
                logger.debug(logMessage.getMessage());
                break;
            case TRACE:
                logger.trace(logMessage.getMessage());
                break;
        }
    }

}
