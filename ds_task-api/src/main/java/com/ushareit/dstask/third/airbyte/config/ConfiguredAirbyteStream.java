
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "stream",
    "sync_mode",
    "cursor_field",
    "destination_sync_mode",
    "primary_key"
})
public class ConfiguredAirbyteStream {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stream")
    private AirbyteStream stream;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sync_mode")
    private SyncMode syncMode;
    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. This field is REQUIRED if `sync_mode` is `incremental`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("cursor_field")
    @JsonPropertyDescription("Path to the field that will be used to determine if a record is new or modified since the last sync. This field is REQUIRED if `sync_mode` is `incremental`. Otherwise it is ignored.")
    private List<String> cursorField = new ArrayList<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("destination_sync_mode")
    private DestinationSyncMode destinationSyncMode;
    /**
     * Paths to the fields that will be used as primary key. This field is REQUIRED if `destination_sync_mode` is `*_dedup`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("primary_key")
    @JsonPropertyDescription("Paths to the fields that will be used as primary key. This field is REQUIRED if `destination_sync_mode` is `*_dedup`. Otherwise it is ignored.")
    private List<List<String>> primaryKey = new ArrayList<List<String>>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stream")
    public AirbyteStream getStream() {
        return stream;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stream")
    public void setStream(AirbyteStream stream) {
        this.stream = stream;
    }

    public ConfiguredAirbyteStream withStream(AirbyteStream stream) {
        this.stream = stream;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sync_mode")
    public SyncMode getSyncMode() {
        return syncMode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sync_mode")
    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public ConfiguredAirbyteStream withSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
        return this;
    }

    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. This field is REQUIRED if `sync_mode` is `incremental`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("cursor_field")
    public List<String> getCursorField() {
        return cursorField;
    }

    /**
     * Path to the field that will be used to determine if a record is new or modified since the last sync. This field is REQUIRED if `sync_mode` is `incremental`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("cursor_field")
    public void setCursorField(List<String> cursorField) {
        this.cursorField = cursorField;
    }

    public ConfiguredAirbyteStream withCursorField(List<String> cursorField) {
        this.cursorField = cursorField;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("destination_sync_mode")
    public DestinationSyncMode getDestinationSyncMode() {
        return destinationSyncMode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("destination_sync_mode")
    public void setDestinationSyncMode(DestinationSyncMode destinationSyncMode) {
        this.destinationSyncMode = destinationSyncMode;
    }

    public ConfiguredAirbyteStream withDestinationSyncMode(DestinationSyncMode destinationSyncMode) {
        this.destinationSyncMode = destinationSyncMode;
        return this;
    }

    /**
     * Paths to the fields that will be used as primary key. This field is REQUIRED if `destination_sync_mode` is `*_dedup`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("primary_key")
    public List<List<String>> getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Paths to the fields that will be used as primary key. This field is REQUIRED if `destination_sync_mode` is `*_dedup`. Otherwise it is ignored.
     * 
     */
    @JsonProperty("primary_key")
    public void setPrimaryKey(List<List<String>> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public ConfiguredAirbyteStream withPrimaryKey(List<List<String>> primaryKey) {
        this.primaryKey = primaryKey;
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

    public ConfiguredAirbyteStream withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ConfiguredAirbyteStream.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("stream");
        sb.append('=');
        sb.append(((this.stream == null)?"<null>":this.stream));
        sb.append(',');
        sb.append("syncMode");
        sb.append('=');
        sb.append(((this.syncMode == null)?"<null>":this.syncMode));
        sb.append(',');
        sb.append("cursorField");
        sb.append('=');
        sb.append(((this.cursorField == null)?"<null>":this.cursorField));
        sb.append(',');
        sb.append("destinationSyncMode");
        sb.append('=');
        sb.append(((this.destinationSyncMode == null)?"<null>":this.destinationSyncMode));
        sb.append(',');
        sb.append("primaryKey");
        sb.append('=');
        sb.append(((this.primaryKey == null)?"<null>":this.primaryKey));
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
        result = ((result* 31)+((this.stream == null)? 0 :this.stream.hashCode()));
        result = ((result* 31)+((this.syncMode == null)? 0 :this.syncMode.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.destinationSyncMode == null)? 0 :this.destinationSyncMode.hashCode()));
        result = ((result* 31)+((this.cursorField == null)? 0 :this.cursorField.hashCode()));
        result = ((result* 31)+((this.primaryKey == null)? 0 :this.primaryKey.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfiguredAirbyteStream) == false) {
            return false;
        }
        ConfiguredAirbyteStream rhs = ((ConfiguredAirbyteStream) other);
        return (((((((this.stream == rhs.stream)||((this.stream!= null)&&this.stream.equals(rhs.stream)))&&((this.syncMode == rhs.syncMode)||((this.syncMode!= null)&&this.syncMode.equals(rhs.syncMode))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.destinationSyncMode == rhs.destinationSyncMode)||((this.destinationSyncMode!= null)&&this.destinationSyncMode.equals(rhs.destinationSyncMode))))&&((this.cursorField == rhs.cursorField)||((this.cursorField!= null)&&this.cursorField.equals(rhs.cursorField))))&&((this.primaryKey == rhs.primaryKey)||((this.primaryKey!= null)&&this.primaryKey.equals(rhs.primaryKey))));
    }

}
