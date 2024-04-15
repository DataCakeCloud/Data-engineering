
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Airbyte stream schema catalog
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "streams"
})
public class ConfiguredAirbyteCatalog {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("streams")
    private List<ConfiguredAirbyteStream> streams = new ArrayList<ConfiguredAirbyteStream>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("streams")
    public List<ConfiguredAirbyteStream> getStreams() {
        return streams;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("streams")
    public void setStreams(List<ConfiguredAirbyteStream> streams) {
        this.streams = streams;
    }

    public ConfiguredAirbyteCatalog withStreams(List<ConfiguredAirbyteStream> streams) {
        this.streams = streams;
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

    public ConfiguredAirbyteCatalog withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ConfiguredAirbyteCatalog.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("streams");
        sb.append('=');
        sb.append(((this.streams == null)?"<null>":this.streams));
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
        result = ((result* 31)+((this.streams == null)? 0 :this.streams.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfiguredAirbyteCatalog) == false) {
            return false;
        }
        ConfiguredAirbyteCatalog rhs = ((ConfiguredAirbyteCatalog) other);
        return (((this.streams == rhs.streams)||((this.streams!= null)&&this.streams.equals(rhs.streams)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
