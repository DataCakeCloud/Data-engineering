
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * per stream state data
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "state",
    "namespace"
})
public class AirbyteStreamState {

    /**
     * Stream name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Stream name")
    private String name;
    /**
     * the state data
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("the state data")
    private JsonNode state;
    /**
     * Optional Source-defined namespace.
     * 
     */
    @JsonProperty("namespace")
    @JsonPropertyDescription("Optional Source-defined namespace.")
    private String namespace;

    /**
     * Stream name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Stream name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public AirbyteStreamState withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * the state data
     * (Required)
     * 
     */
    @JsonProperty("state")
    public JsonNode getState() {
        return state;
    }

    /**
     * the state data
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(JsonNode state) {
        this.state = state;
    }

    public AirbyteStreamState withState(JsonNode state) {
        this.state = state;
        return this;
    }

    /**
     * Optional Source-defined namespace.
     * 
     */
    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    /**
     * Optional Source-defined namespace.
     * 
     */
    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public AirbyteStreamState withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AirbyteStreamState.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("namespace");
        sb.append('=');
        sb.append(((this.namespace == null)?"<null>":this.namespace));
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
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.namespace == null)? 0 :this.namespace.hashCode()));
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AirbyteStreamState) == false) {
            return false;
        }
        AirbyteStreamState rhs = ((AirbyteStreamState) other);
        return ((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.namespace == rhs.namespace)||((this.namespace!= null)&&this.namespace.equals(rhs.namespace))))&&((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state))));
    }

}
