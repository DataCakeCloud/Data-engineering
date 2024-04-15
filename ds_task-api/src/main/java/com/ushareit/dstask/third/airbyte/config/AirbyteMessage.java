
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "log",
    "spec",
    "connectionStatus",
    "catalog",
    "record",
    "state",
    "trace"
})
public class AirbyteMessage {

    /**
     * Message type
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Message type")
    private Type type;
    @JsonProperty("log")
    private AirbyteLogMessage log;
    /**
     * Specification of a connector (source/destination)
     * 
     */
    @JsonProperty("spec")
    @JsonPropertyDescription("Specification of a connector (source/destination)")
    private ConnectorSpecification spec;
    /**
     * Airbyte connection status
     * 
     */
    @JsonProperty("connectionStatus")
    @JsonPropertyDescription("Airbyte connection status")
    private AirbyteConnectionStatus connectionStatus;
    /**
     * Airbyte stream schema catalog
     * 
     */
    @JsonProperty("catalog")
    @JsonPropertyDescription("Airbyte stream schema catalog")
    private AirbyteCatalog catalog;
    @JsonProperty("record")
    private AirbyteRecordMessage record;
    @JsonProperty("state")
    private AirbyteStateMessage state;
    @JsonProperty("trace")
    private AirbyteTraceMessage trace;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Message type
     * (Required)
     * 
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * Message type
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    public AirbyteMessage withType(Type type) {
        this.type = type;
        return this;
    }

    @JsonProperty("log")
    public AirbyteLogMessage getLog() {
        return log;
    }

    @JsonProperty("log")
    public void setLog(AirbyteLogMessage log) {
        this.log = log;
    }

    public AirbyteMessage withLog(AirbyteLogMessage log) {
        this.log = log;
        return this;
    }

    /**
     * Specification of a connector (source/destination)
     * 
     */
    @JsonProperty("spec")
    public ConnectorSpecification getSpec() {
        return spec;
    }

    /**
     * Specification of a connector (source/destination)
     * 
     */
    @JsonProperty("spec")
    public void setSpec(ConnectorSpecification spec) {
        this.spec = spec;
    }

    public AirbyteMessage withSpec(ConnectorSpecification spec) {
        this.spec = spec;
        return this;
    }

    /**
     * Airbyte connection status
     * 
     */
    @JsonProperty("connectionStatus")
    public AirbyteConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * Airbyte connection status
     * 
     */
    @JsonProperty("connectionStatus")
    public void setConnectionStatus(AirbyteConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public AirbyteMessage withConnectionStatus(AirbyteConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        return this;
    }

    /**
     * Airbyte stream schema catalog
     * 
     */
    @JsonProperty("catalog")
    public AirbyteCatalog getCatalog() {
        return catalog;
    }

    /**
     * Airbyte stream schema catalog
     * 
     */
    @JsonProperty("catalog")
    public void setCatalog(AirbyteCatalog catalog) {
        this.catalog = catalog;
    }

    public AirbyteMessage withCatalog(AirbyteCatalog catalog) {
        this.catalog = catalog;
        return this;
    }

    @JsonProperty("record")
    public AirbyteRecordMessage getRecord() {
        return record;
    }

    @JsonProperty("record")
    public void setRecord(AirbyteRecordMessage record) {
        this.record = record;
    }

    public AirbyteMessage withRecord(AirbyteRecordMessage record) {
        this.record = record;
        return this;
    }

    @JsonProperty("state")
    public AirbyteStateMessage getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(AirbyteStateMessage state) {
        this.state = state;
    }

    public AirbyteMessage withState(AirbyteStateMessage state) {
        this.state = state;
        return this;
    }

    @JsonProperty("trace")
    public AirbyteTraceMessage getTrace() {
        return trace;
    }

    @JsonProperty("trace")
    public void setTrace(AirbyteTraceMessage trace) {
        this.trace = trace;
    }

    public AirbyteMessage withTrace(AirbyteTraceMessage trace) {
        this.trace = trace;
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

    public AirbyteMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("log");
        sb.append('=');
        sb.append(((this.log == null)?"<null>":this.log));
        sb.append(',');
        sb.append("spec");
        sb.append('=');
        sb.append(((this.spec == null)?"<null>":this.spec));
        sb.append(',');
        sb.append("connectionStatus");
        sb.append('=');
        sb.append(((this.connectionStatus == null)?"<null>":this.connectionStatus));
        sb.append(',');
        sb.append("catalog");
        sb.append('=');
        sb.append(((this.catalog == null)?"<null>":this.catalog));
        sb.append(',');
        sb.append("record");
        sb.append('=');
        sb.append(((this.record == null)?"<null>":this.record));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("trace");
        sb.append('=');
        sb.append(((this.trace == null)?"<null>":this.trace));
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
        result = ((result* 31)+((this.trace == null)? 0 :this.trace.hashCode()));
        result = ((result* 31)+((this.log == null)? 0 :this.log.hashCode()));
        result = ((result* 31)+((this.catalog == null)? 0 :this.catalog.hashCode()));
        result = ((result* 31)+((this.record == null)? 0 :this.record.hashCode()));
        result = ((result* 31)+((this.connectionStatus == null)? 0 :this.connectionStatus.hashCode()));
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.spec == null)? 0 :this.spec.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteMessage) == false) {
            return false;
        }
        AirbyteMessage rhs = ((AirbyteMessage) other);
        return ((((((((((this.trace == rhs.trace)||((this.trace!= null)&&this.trace.equals(rhs.trace)))&&((this.log == rhs.log)||((this.log!= null)&&this.log.equals(rhs.log))))&&((this.catalog == rhs.catalog)||((this.catalog!= null)&&this.catalog.equals(rhs.catalog))))&&((this.record == rhs.record)||((this.record!= null)&&this.record.equals(rhs.record))))&&((this.connectionStatus == rhs.connectionStatus)||((this.connectionStatus!= null)&&this.connectionStatus.equals(rhs.connectionStatus))))&&((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.spec == rhs.spec)||((this.spec!= null)&&this.spec.equals(rhs.spec))));
    }


    /**
     * Message type
     * 
     */
    public enum Type {

        RECORD("RECORD"),
        STATE("STATE"),
        LOG("LOG"),
        SPEC("SPEC"),
        CONNECTION_STATUS("CONNECTION_STATUS"),
        CATALOG("CATALOG"),
        TRACE("TRACE");
        private final String value;
        private final static Map<String, Type> CONSTANTS = new HashMap<String, Type>();

        static {
            for (Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
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
        public static Type fromValue(String value) {
            Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
