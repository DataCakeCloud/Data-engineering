
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;


/**
 * StandardCheckConnectionInput
 * <p>
 * information required for connection.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "connectionConfiguration"
})
public class StandardCheckConnectionInput implements Serializable
{

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode connectionConfiguration;
    private final static long serialVersionUID = -4015042999527983734L;

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionConfiguration")
    public JsonNode getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionConfiguration")
    public void setConnectionConfiguration(JsonNode connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    public StandardCheckConnectionInput withConnectionConfiguration(JsonNode connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StandardCheckConnectionInput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("connectionConfiguration");
        sb.append('=');
        sb.append(((this.connectionConfiguration == null)?"<null>":this.connectionConfiguration));
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
        result = ((result* 31)+((this.connectionConfiguration == null)? 0 :this.connectionConfiguration.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StandardCheckConnectionInput) == false) {
            return false;
        }
        StandardCheckConnectionInput rhs = ((StandardCheckConnectionInput) other);
        return ((this.connectionConfiguration == rhs.connectionConfiguration)||((this.connectionConfiguration!= null)&&this.connectionConfiguration.equals(rhs.connectionConfiguration)));
    }

}
