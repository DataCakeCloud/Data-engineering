
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "json_schema",
    "supported_sync_modes",
    "source_defined_cursor",
    "default_cursor_field",
    "source_defined_primary_key",
    "namespace"
})
public class AirbyteStream {

    /**
     * Stream's name.
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Stream's name.")
    private String name;
    /**
     * Stream schema using Json Schema specs.
     * (Required)
     * 
     */
    @JsonProperty("json_schema")
    @JsonPropertyDescription("Stream schema using Json Schema specs.")
    private JsonNode jsonSchema;
    @JsonProperty("supported_sync_modes")
    private List<SyncMode> supportedSyncModes = new ArrayList<SyncMode>();
    /**
     * If the source defines the cursor field, then any other cursor field inputs will be ignored. If it does not, either the user_provided one is used, or the default one is used as a backup.
     * 
     */
    @JsonProperty("source_defined_cursor")
    @JsonPropertyDescription("If the source defines the cursor field, then any other cursor field inputs will be ignored. If it does not, either the user_provided one is used, or the default one is used as a backup.")
    private Boolean sourceDefinedCursor;
    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. If not provided by the source, the end user will have to specify the comparable themselves.
     * 
     */
    @JsonProperty("default_cursor_field")
    @JsonPropertyDescription("Path to the field that will be used to determine if a record is new or modified since the last sync. If not provided by the source, the end user will have to specify the comparable themselves.")
    private List<String> defaultCursorField = new ArrayList<String>();
    /**
     * If the source defines the primary key, paths to the fields that will be used as a primary key. If not provided by the source, the end user will have to specify the primary key themselves.
     * 
     */
    @JsonProperty("source_defined_primary_key")
    @JsonPropertyDescription("If the source defines the primary key, paths to the fields that will be used as a primary key. If not provided by the source, the end user will have to specify the primary key themselves.")
    private List<List<String>> sourceDefinedPrimaryKey = new ArrayList<List<String>>();
    /**
     * Optional Source-defined namespace. Currently only used by JDBC destinations to determine what schema to write to. Airbyte streams from the same sources should have the same namespace.
     * 
     */
    @JsonProperty("namespace")
    @JsonPropertyDescription("Optional Source-defined namespace. Currently only used by JDBC destinations to determine what schema to write to. Airbyte streams from the same sources should have the same namespace.")
    private String namespace;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Stream's name.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Stream's name.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public AirbyteStream withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Stream schema using Json Schema specs.
     * (Required)
     * 
     */
    @JsonProperty("json_schema")
    public JsonNode getJsonSchema() {
        return jsonSchema;
    }

    /**
     * Stream schema using Json Schema specs.
     * (Required)
     * 
     */
    @JsonProperty("json_schema")
    public void setJsonSchema(JsonNode jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public AirbyteStream withJsonSchema(JsonNode jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

    @JsonProperty("supported_sync_modes")
    public List<SyncMode> getSupportedSyncModes() {
        return supportedSyncModes;
    }

    @JsonProperty("supported_sync_modes")
    public void setSupportedSyncModes(List<SyncMode> supportedSyncModes) {
        this.supportedSyncModes = supportedSyncModes;
    }

    public AirbyteStream withSupportedSyncModes(List<SyncMode> supportedSyncModes) {
        this.supportedSyncModes = supportedSyncModes;
        return this;
    }

    /**
     * If the source defines the cursor field, then any other cursor field inputs will be ignored. If it does not, either the user_provided one is used, or the default one is used as a backup.
     * 
     */
    @JsonProperty("source_defined_cursor")
    public Boolean getSourceDefinedCursor() {
        return sourceDefinedCursor;
    }

    /**
     * If the source defines the cursor field, then any other cursor field inputs will be ignored. If it does not, either the user_provided one is used, or the default one is used as a backup.
     * 
     */
    @JsonProperty("source_defined_cursor")
    public void setSourceDefinedCursor(Boolean sourceDefinedCursor) {
        this.sourceDefinedCursor = sourceDefinedCursor;
    }

    public AirbyteStream withSourceDefinedCursor(Boolean sourceDefinedCursor) {
        this.sourceDefinedCursor = sourceDefinedCursor;
        return this;
    }

    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. If not provided by the source, the end user will have to specify the comparable themselves.
     * 
     */
    @JsonProperty("default_cursor_field")
    public List<String> getDefaultCursorField() {
        return defaultCursorField;
    }

    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. If not provided by the source, the end user will have to specify the comparable themselves.
     * 
     */
    @JsonProperty("default_cursor_field")
    public void setDefaultCursorField(List<String> defaultCursorField) {
        this.defaultCursorField = defaultCursorField;
    }

    public AirbyteStream withDefaultCursorField(List<String> defaultCursorField) {
        this.defaultCursorField = defaultCursorField;
        return this;
    }

    /**
     * If the source defines the primary key, paths to the fields that will be used as a primary key. If not provided by the source, the end user will have to specify the primary key themselves.
     * 
     */
    @JsonProperty("source_defined_primary_key")
    public List<List<String>> getSourceDefinedPrimaryKey() {
        return sourceDefinedPrimaryKey;
    }

    /**
     * If the source defines the primary key, paths to the fields that will be used as a primary key. If not provided by the source, the end user will have to specify the primary key themselves.
     * 
     */
    @JsonProperty("source_defined_primary_key")
    public void setSourceDefinedPrimaryKey(List<List<String>> sourceDefinedPrimaryKey) {
        this.sourceDefinedPrimaryKey = sourceDefinedPrimaryKey;
    }

    public AirbyteStream withSourceDefinedPrimaryKey(List<List<String>> sourceDefinedPrimaryKey) {
        this.sourceDefinedPrimaryKey = sourceDefinedPrimaryKey;
        return this;
    }

    /**
     * Optional Source-defined namespace. Currently only used by JDBC destinations to determine what schema to write to. Airbyte streams from the same sources should have the same namespace.
     * 
     */
    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    /**
     * Optional Source-defined namespace. Currently only used by JDBC destinations to determine what schema to write to. Airbyte streams from the same sources should have the same namespace.
     * 
     */
    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public AirbyteStream withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public AirbyteStream withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteStream.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("jsonSchema");
        sb.append('=');
        sb.append(((this.jsonSchema == null)?"<null>":this.jsonSchema));
        sb.append(',');
        sb.append("supportedSyncModes");
        sb.append('=');
        sb.append(((this.supportedSyncModes == null)?"<null>":this.supportedSyncModes));
        sb.append(',');
        sb.append("sourceDefinedCursor");
        sb.append('=');
        sb.append(((this.sourceDefinedCursor == null)?"<null>":this.sourceDefinedCursor));
        sb.append(',');
        sb.append("defaultCursorField");
        sb.append('=');
        sb.append(((this.defaultCursorField == null)?"<null>":this.defaultCursorField));
        sb.append(',');
        sb.append("sourceDefinedPrimaryKey");
        sb.append('=');
        sb.append(((this.sourceDefinedPrimaryKey == null)?"<null>":this.sourceDefinedPrimaryKey));
        sb.append(',');
        sb.append("namespace");
        sb.append('=');
        sb.append(((this.namespace == null)?"<null>":this.namespace));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.sourceDefinedPrimaryKey == null)? 0 :this.sourceDefinedPrimaryKey.hashCode()));
        result = ((result* 31)+((this.supportedSyncModes == null)? 0 :this.supportedSyncModes.hashCode()));
        result = ((result* 31)+((this.sourceDefinedCursor == null)? 0 :this.sourceDefinedCursor.hashCode()));
        result = ((result* 31)+((this.jsonSchema == null)? 0 :this.jsonSchema.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.namespace == null)? 0 :this.namespace.hashCode()));
        result = ((result* 31)+((this.defaultCursorField == null)? 0 :this.defaultCursorField.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteStream) == false) {
            return false;
        }
        AirbyteStream rhs = ((AirbyteStream) other);
        return (((((((((this.sourceDefinedPrimaryKey == rhs.sourceDefinedPrimaryKey)||((this.sourceDefinedPrimaryKey!= null)&&this.sourceDefinedPrimaryKey.equals(rhs.sourceDefinedPrimaryKey)))&&((this.supportedSyncModes == rhs.supportedSyncModes)||((this.supportedSyncModes!= null)&&this.supportedSyncModes.equals(rhs.supportedSyncModes))))&&((this.sourceDefinedCursor == rhs.sourceDefinedCursor)||((this.sourceDefinedCursor!= null)&&this.sourceDefinedCursor.equals(rhs.sourceDefinedCursor))))&&((this.jsonSchema == rhs.jsonSchema)||((this.jsonSchema!= null)&&this.jsonSchema.equals(rhs.jsonSchema))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.namespace == rhs.namespace)||((this.namespace!= null)&&this.namespace.equals(rhs.namespace))))&&((this.defaultCursorField == rhs.defaultCursorField)||((this.defaultCursorField!= null)&&this.defaultCursorField.equals(rhs.defaultCursorField))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
