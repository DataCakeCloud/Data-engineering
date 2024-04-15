/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.third.airbyte.json.JsonSchemaValidator;
import com.ushareit.dstask.third.airbyte.model.AirbyteProtocolSchema;

import java.util.function.Predicate;

/**
 * Verify that the provided JsonNode is a valid AirbyteMessage. Any AirbyteMessage type is allowed
 * (e.g. Record, State, Log, etc).
 */
public class AirbyteProtocolPredicate implements Predicate<JsonNode> {

    private final JsonSchemaValidator jsonSchemaValidator;
    private final JsonNode schema;

    public AirbyteProtocolPredicate() {
        jsonSchemaValidator = new JsonSchemaValidator();
        schema = JsonSchemaValidator.getSchema(AirbyteProtocolSchema.PROTOCOL.getFile(), "AirbyteMessage");
    }

    @Override
    public boolean test(final JsonNode s) {
        return jsonSchemaValidator.test(schema, s);
    }

}
