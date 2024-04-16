
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * Airbyte connection status
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "status",
    "message"
})
public class AirbyteConnectionStatus {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    private Status status;
    @JsonProperty("message")
    private String message;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    public AirbyteConnectionStatus withStatus(Status status) {
        this.status = status;
        return this;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    public AirbyteConnectionStatus withMessage(String message) {
        this.message = message;
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

    public AirbyteConnectionStatus withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteConnectionStatus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
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
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteConnectionStatus) == false) {
            return false;
        }
        AirbyteConnectionStatus rhs = ((AirbyteConnectionStatus) other);
        return ((((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties)))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

    public enum Status {

        SUCCEEDED("SUCCEEDED"),
        FAILED("FAILED");
        private final String value;
        private final static Map<String, Status> CONSTANTS = new HashMap<String, Status>();

        static {
            for (Status c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Status(String value) {
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
        public static Status fromValue(String value) {
            Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
