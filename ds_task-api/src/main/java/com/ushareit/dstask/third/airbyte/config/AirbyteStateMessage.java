
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "state_type",
    "data",
    "global",
    "streams"
})
public class AirbyteStateMessage {

    /**
     * The type of state the other fields represent. If not set, the state data is interpreted as GLOBAL and should be read from the `data` field for backwards compatibility. GLOBAL means that the state should be read from `global` and means that it represents the state for all the streams. PER_STREAM means that the state should be read from `streams`. Each item in the list represents the state for the associated stream.
     * 
     * 
     */
    @JsonProperty("state_type")
    @JsonPropertyDescription("The type of state the other fields represent. If not set, the state data is interpreted as GLOBAL and should be read from the `data` field for backwards compatibility. GLOBAL means that the state should be read from `global` and means that it represents the state for all the streams. PER_STREAM means that the state should be read from `streams`. Each item in the list represents the state for the associated stream.\n")
    private AirbyteStateType stateType;
    /**
     * (Deprecated) the state data
     * 
     */
    @JsonProperty("data")
    @JsonPropertyDescription("(Deprecated) the state data")
    private JsonNode data;
    /**
     * the state data
     * 
     */
    @JsonProperty("global")
    @JsonPropertyDescription("the state data")
    private JsonNode global;
    @JsonProperty("streams")
    private List<AirbyteStreamState> streams = new ArrayList<AirbyteStreamState>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The type of state the other fields represent. If not set, the state data is interpreted as GLOBAL and should be read from the `data` field for backwards compatibility. GLOBAL means that the state should be read from `global` and means that it represents the state for all the streams. PER_STREAM means that the state should be read from `streams`. Each item in the list represents the state for the associated stream.
     * 
     * 
     */
    @JsonProperty("state_type")
    public AirbyteStateType getStateType() {
        return stateType;
    }

    /**
     * The type of state the other fields represent. If not set, the state data is interpreted as GLOBAL and should be read from the `data` field for backwards compatibility. GLOBAL means that the state should be read from `global` and means that it represents the state for all the streams. PER_STREAM means that the state should be read from `streams`. Each item in the list represents the state for the associated stream.
     * 
     * 
     */
    @JsonProperty("state_type")
    public void setStateType(AirbyteStateType stateType) {
        this.stateType = stateType;
    }

    public AirbyteStateMessage withStateType(AirbyteStateType stateType) {
        this.stateType = stateType;
        return this;
    }

    /**
     * (Deprecated) the state data
     * 
     */
    @JsonProperty("data")
    public JsonNode getData() {
        return data;
    }

    /**
     * (Deprecated) the state data
     * 
     */
    @JsonProperty("data")
    public void setData(JsonNode data) {
        this.data = data;
    }

    public AirbyteStateMessage withData(JsonNode data) {
        this.data = data;
        return this;
    }

    /**
     * the state data
     * 
     */
    @JsonProperty("global")
    public JsonNode getGlobal() {
        return global;
    }

    /**
     * the state data
     * 
     */
    @JsonProperty("global")
    public void setGlobal(JsonNode global) {
        this.global = global;
    }

    public AirbyteStateMessage withGlobal(JsonNode global) {
        this.global = global;
        return this;
    }

    @JsonProperty("streams")
    public List<AirbyteStreamState> getStreams() {
        return streams;
    }

    @JsonProperty("streams")
    public void setStreams(List<AirbyteStreamState> streams) {
        this.streams = streams;
    }

    public AirbyteStateMessage withStreams(List<AirbyteStreamState> streams) {
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

    public AirbyteStateMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteStateMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("stateType");
        sb.append('=');
        sb.append(((this.stateType == null)?"<null>":this.stateType));
        sb.append(',');
        sb.append("data");
        sb.append('=');
        sb.append(((this.data == null)?"<null>":this.data));
        sb.append(',');
        sb.append("global");
        sb.append('=');
        sb.append(((this.global == null)?"<null>":this.global));
        sb.append(',');
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
        result = ((result* 31)+((this.global == null)? 0 :this.global.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.data == null)? 0 :this.data.hashCode()));
        result = ((result* 31)+((this.stateType == null)? 0 :this.stateType.hashCode()));
        result = ((result* 31)+((this.streams == null)? 0 :this.streams.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteStateMessage) == false) {
            return false;
        }
        AirbyteStateMessage rhs = ((AirbyteStateMessage) other);
        return ((((((this.global == rhs.global)||((this.global!= null)&&this.global.equals(rhs.global)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.data == rhs.data)||((this.data!= null)&&this.data.equals(rhs.data))))&&((this.stateType == rhs.stateType)||((this.stateType!= null)&&this.stateType.equals(rhs.stateType))))&&((this.streams == rhs.streams)||((this.streams!= null)&&this.streams.equals(rhs.streams))));
    }


    /**
     * The type of state the other fields represent. If not set, the state data is interpreted as GLOBAL and should be read from the `data` field for backwards compatibility. GLOBAL means that the state should be read from `global` and means that it represents the state for all the streams. PER_STREAM means that the state should be read from `streams`. Each item in the list represents the state for the associated stream.
     * 
     * 
     */
    public enum AirbyteStateType {

        GLOBAL("GLOBAL"),
        PER_STREAM("PER_STREAM");
        private final String value;
        private final static Map<String, AirbyteStateType> CONSTANTS = new HashMap<String, AirbyteStateType>();

        static {
            for (AirbyteStateType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private AirbyteStateType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static AirbyteStateType fromValue(String value) {
            AirbyteStateType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
