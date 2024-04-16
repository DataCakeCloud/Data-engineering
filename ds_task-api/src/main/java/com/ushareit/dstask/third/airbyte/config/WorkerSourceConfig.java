
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;


/**
 * WorkerSourceConfig
 * <p>
 * WorkerSourceConfig
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sourceConnectionConfiguration",
        "catalog",
        "state"
})
public class WorkerSourceConfig implements Serializable {

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConnectionConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode sourceConnectionConfiguration;
    /**
     * (Required)
     */
    @JsonProperty("catalog")
    private ConfiguredAirbyteCatalog catalog;
    /**
     * State
     * <p>
     * information output by the connection.
     */
    @JsonProperty("state")
    @JsonPropertyDescription("information output by the connection.")
    private State state;
    private final static long serialVersionUID = -3165164657904494525L;

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConnectionConfiguration")
    public JsonNode getSourceConnectionConfiguration() {
        return sourceConnectionConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConnectionConfiguration")
    public void setSourceConnectionConfiguration(JsonNode sourceConnectionConfiguration) {
        this.sourceConnectionConfiguration = sourceConnectionConfiguration;
    }

    public WorkerSourceConfig withSourceConnectionConfiguration(JsonNode sourceConnectionConfiguration) {
        this.sourceConnectionConfiguration = sourceConnectionConfiguration;
        return this;
    }

    /**
     * (Required)
     */
    @JsonProperty("catalog")
    public ConfiguredAirbyteCatalog getCatalog() {
        return catalog;
    }

    /**
     * (Required)
     */
    @JsonProperty("catalog")
    public void setCatalog(ConfiguredAirbyteCatalog catalog) {
        this.catalog = catalog;
    }

    public WorkerSourceConfig withCatalog(ConfiguredAirbyteCatalog catalog) {
        this.catalog = catalog;
        return this;
    }

    /**
     * State
     * <p>
     * information output by the connection.
     */
    @JsonProperty("state")
    public State getState() {
        return state;
    }

    /**
     * State
     * <p>
     * information output by the connection.
     */
    @JsonProperty("state")
    public void setState(State state) {
        this.state = state;
    }

    public WorkerSourceConfig withState(State state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(WorkerSourceConfig.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sourceConnectionConfiguration");
        sb.append('=');
        sb.append(((this.sourceConnectionConfiguration == null) ? "<null>" : this.sourceConnectionConfiguration));
        sb.append(',');
        sb.append("catalog");
        sb.append('=');
        sb.append(((this.catalog == null) ? "<null>" : this.catalog));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null) ? "<null>" : this.state));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.state == null) ? 0 : this.state.hashCode()));
        result = ((result * 31) + ((this.sourceConnectionConfiguration == null) ? 0 : this.sourceConnectionConfiguration.hashCode()));
        result = ((result * 31) + ((this.catalog == null) ? 0 : this.catalog.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkerSourceConfig) == false) {
            return false;
        }
        WorkerSourceConfig rhs = ((WorkerSourceConfig) other);
        return ((((this.state == rhs.state) || ((this.state != null) && this.state.equals(rhs.state))) && ((this.sourceConnectionConfiguration == rhs.sourceConnectionConfiguration) || ((this.sourceConnectionConfiguration != null) && this.sourceConnectionConfiguration.equals(rhs.sourceConnectionConfiguration)))) && ((this.catalog == rhs.catalog) || ((this.catalog != null) && this.catalog.equals(rhs.catalog))));
    }

}
