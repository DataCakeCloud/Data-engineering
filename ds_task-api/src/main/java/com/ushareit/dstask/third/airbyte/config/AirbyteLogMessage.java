
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "level",
    "message"
})
public class AirbyteLogMessage {

    /**
     * the type of logging
     * (Required)
     * 
     */
    @JsonProperty("level")
    @JsonPropertyDescription("the type of logging")
    private Level level;
    /**
     * the log message
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("the log message")
    private String message;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * the type of logging
     * (Required)
     * 
     */
    @JsonProperty("level")
    public Level getLevel() {
        return level;
    }

    /**
     * the type of logging
     * (Required)
     * 
     */
    @JsonProperty("level")
    public void setLevel(Level level) {
        this.level = level;
    }

    public AirbyteLogMessage withLevel(Level level) {
        this.level = level;
        return this;
    }

    /**
     * the log message
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * the log message
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    public AirbyteLogMessage withMessage(String message) {
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

    public AirbyteLogMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteLogMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("level");
        sb.append('=');
        sb.append(((this.level == null)?"<null>":this.level));
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
        result = ((result* 31)+((this.level == null)? 0 :this.level.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteLogMessage) == false) {
            return false;
        }
        AirbyteLogMessage rhs = ((AirbyteLogMessage) other);
        return ((((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties)))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.level == rhs.level)||((this.level!= null)&&this.level.equals(rhs.level))));
    }


    /**
     * the type of logging
     * 
     */
    public enum Level {

        FATAL("FATAL"),
        ERROR("ERROR"),
        WARN("WARN"),
        INFO("INFO"),
        DEBUG("DEBUG"),
        TRACE("TRACE");
        private final String value;
        private final static Map<String, Level> CONSTANTS = new HashMap<String, Level>();

        static {
            for (Level c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Level(String value) {
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
        public static Level fromValue(String value) {
            Level constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
