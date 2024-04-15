
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * StandardSyncInput
 * <p>
 * job sync config
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "namespaceDefinition",
        "namespaceFormat",
        "prefix",
        "sourceConfiguration",
        "destinationConfiguration",
        "operationSequence",
        "catalog",
        "state",
        "resourceRequirements",
        "sourceResourceRequirements",
        "destinationResourceRequirements"
})
public class StandardSyncInput implements Serializable {

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     */
    @JsonProperty("namespaceDefinition")
    @JsonPropertyDescription("Method used for computing final namespace in destination")
    private JobSyncConfig.NamespaceDefinitionType namespaceDefinition = JobSyncConfig.NamespaceDefinitionType.fromValue("source");
    @JsonProperty("namespaceFormat")
    private String namespaceFormat = null;
    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     */
    @JsonProperty("prefix")
    @JsonPropertyDescription("Prefix that will be prepended to the name of each stream when it is written to the destination.")
    private String prefix;
    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode sourceConfiguration;
    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("destinationConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode destinationConfiguration;
    /**
     * Sequence of configurations of operations to apply as part of the sync
     */
    @JsonProperty("operationSequence")
    @JsonPropertyDescription("Sequence of configurations of operations to apply as part of the sync")
    private List<StandardSyncOperation> operationSequence = new ArrayList<StandardSyncOperation>();
    /**
     * the configured airbyte catalog
     * (Required)
     */
    @JsonProperty("catalog")
    @JsonPropertyDescription("the configured airbyte catalog")
    private ConfiguredAirbyteCatalog catalog;
    /**
     * State
     * <p>
     * information output by the connection.
     */
    @JsonProperty("state")
    @JsonPropertyDescription("information output by the connection.")
    private State state;
    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("resourceRequirements")
    @JsonPropertyDescription("generic configuration for pod source requirements")
    private ResourceRequirements resourceRequirements;
    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("sourceResourceRequirements")
    @JsonPropertyDescription("generic configuration for pod source requirements")
    private ResourceRequirements sourceResourceRequirements;
    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("destinationResourceRequirements")
    @JsonPropertyDescription("generic configuration for pod source requirements")
    private ResourceRequirements destinationResourceRequirements;
    private final static long serialVersionUID = 669775061174791292L;

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     */
    @JsonProperty("namespaceDefinition")
    public JobSyncConfig.NamespaceDefinitionType getNamespaceDefinition() {
        return namespaceDefinition;
    }

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     */
    @JsonProperty("namespaceDefinition")
    public void setNamespaceDefinition(JobSyncConfig.NamespaceDefinitionType namespaceDefinition) {
        this.namespaceDefinition = namespaceDefinition;
    }

    public StandardSyncInput withNamespaceDefinition(JobSyncConfig.NamespaceDefinitionType namespaceDefinition) {
        this.namespaceDefinition = namespaceDefinition;
        return this;
    }

    @JsonProperty("namespaceFormat")
    public String getNamespaceFormat() {
        return namespaceFormat;
    }

    @JsonProperty("namespaceFormat")
    public void setNamespaceFormat(String namespaceFormat) {
        this.namespaceFormat = namespaceFormat;
    }

    public StandardSyncInput withNamespaceFormat(String namespaceFormat) {
        this.namespaceFormat = namespaceFormat;
        return this;
    }

    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public StandardSyncInput withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConfiguration")
    public JsonNode getSourceConfiguration() {
        return sourceConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("sourceConfiguration")
    public void setSourceConfiguration(JsonNode sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public StandardSyncInput withSourceConfiguration(JsonNode sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
        return this;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("destinationConfiguration")
    public JsonNode getDestinationConfiguration() {
        return destinationConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     */
    @JsonProperty("destinationConfiguration")
    public void setDestinationConfiguration(JsonNode destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
    }

    public StandardSyncInput withDestinationConfiguration(JsonNode destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
        return this;
    }

    /**
     * Sequence of configurations of operations to apply as part of the sync
     */
    @JsonProperty("operationSequence")
    public List<StandardSyncOperation> getOperationSequence() {
        return operationSequence;
    }

    /**
     * Sequence of configurations of operations to apply as part of the sync
     */
    @JsonProperty("operationSequence")
    public void setOperationSequence(List<StandardSyncOperation> operationSequence) {
        this.operationSequence = operationSequence;
    }

    public StandardSyncInput withOperationSequence(List<StandardSyncOperation> operationSequence) {
        this.operationSequence = operationSequence;
        return this;
    }

    /**
     * the configured airbyte catalog
     * (Required)
     */
    @JsonProperty("catalog")
    public ConfiguredAirbyteCatalog getCatalog() {
        return catalog;
    }

    /**
     * the configured airbyte catalog
     * (Required)
     */
    @JsonProperty("catalog")
    public void setCatalog(ConfiguredAirbyteCatalog catalog) {
        this.catalog = catalog;
    }

    public StandardSyncInput withCatalog(ConfiguredAirbyteCatalog catalog) {
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

    public StandardSyncInput withState(State state) {
        this.state = state;
        return this;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("resourceRequirements")
    public ResourceRequirements getResourceRequirements() {
        return resourceRequirements;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("resourceRequirements")
    public void setResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public StandardSyncInput withResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
        return this;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("sourceResourceRequirements")
    public ResourceRequirements getSourceResourceRequirements() {
        return sourceResourceRequirements;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("sourceResourceRequirements")
    public void setSourceResourceRequirements(ResourceRequirements sourceResourceRequirements) {
        this.sourceResourceRequirements = sourceResourceRequirements;
    }

    public StandardSyncInput withSourceResourceRequirements(ResourceRequirements sourceResourceRequirements) {
        this.sourceResourceRequirements = sourceResourceRequirements;
        return this;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("destinationResourceRequirements")
    public ResourceRequirements getDestinationResourceRequirements() {
        return destinationResourceRequirements;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     */
    @JsonProperty("destinationResourceRequirements")
    public void setDestinationResourceRequirements(ResourceRequirements destinationResourceRequirements) {
        this.destinationResourceRequirements = destinationResourceRequirements;
    }

    public StandardSyncInput withDestinationResourceRequirements(ResourceRequirements destinationResourceRequirements) {
        this.destinationResourceRequirements = destinationResourceRequirements;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StandardSyncInput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("namespaceDefinition");
        sb.append('=');
        sb.append(((this.namespaceDefinition == null) ? "<null>" : this.namespaceDefinition));
        sb.append(',');
        sb.append("namespaceFormat");
        sb.append('=');
        sb.append(((this.namespaceFormat == null) ? "<null>" : this.namespaceFormat));
        sb.append(',');
        sb.append("prefix");
        sb.append('=');
        sb.append(((this.prefix == null) ? "<null>" : this.prefix));
        sb.append(',');
        sb.append("sourceConfiguration");
        sb.append('=');
        sb.append(((this.sourceConfiguration == null) ? "<null>" : this.sourceConfiguration));
        sb.append(',');
        sb.append("destinationConfiguration");
        sb.append('=');
        sb.append(((this.destinationConfiguration == null) ? "<null>" : this.destinationConfiguration));
        sb.append(',');
        sb.append("operationSequence");
        sb.append('=');
        sb.append(((this.operationSequence == null) ? "<null>" : this.operationSequence));
        sb.append(',');
        sb.append("catalog");
        sb.append('=');
        sb.append(((this.catalog == null) ? "<null>" : this.catalog));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null) ? "<null>" : this.state));
        sb.append(',');
        sb.append("resourceRequirements");
        sb.append('=');
        sb.append(((this.resourceRequirements == null) ? "<null>" : this.resourceRequirements));
        sb.append(',');
        sb.append("sourceResourceRequirements");
        sb.append('=');
        sb.append(((this.sourceResourceRequirements == null) ? "<null>" : this.sourceResourceRequirements));
        sb.append(',');
        sb.append("destinationResourceRequirements");
        sb.append('=');
        sb.append(((this.destinationResourceRequirements == null) ? "<null>" : this.destinationResourceRequirements));
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
        result = ((result * 31) + ((this.resourceRequirements == null) ? 0 : this.resourceRequirements.hashCode()));
        result = ((result * 31) + ((this.operationSequence == null) ? 0 : this.operationSequence.hashCode()));
        result = ((result * 31) + ((this.destinationResourceRequirements == null) ? 0 : this.destinationResourceRequirements.hashCode()));
        result = ((result * 31) + ((this.destinationConfiguration == null) ? 0 : this.destinationConfiguration.hashCode()));
        result = ((result * 31) + ((this.sourceConfiguration == null) ? 0 : this.sourceConfiguration.hashCode()));
        result = ((result * 31) + ((this.prefix == null) ? 0 : this.prefix.hashCode()));
        result = ((result * 31) + ((this.catalog == null) ? 0 : this.catalog.hashCode()));
        result = ((result * 31) + ((this.sourceResourceRequirements == null) ? 0 : this.sourceResourceRequirements.hashCode()));
        result = ((result * 31) + ((this.namespaceFormat == null) ? 0 : this.namespaceFormat.hashCode()));
        result = ((result * 31) + ((this.state == null) ? 0 : this.state.hashCode()));
        result = ((result * 31) + ((this.namespaceDefinition == null) ? 0 : this.namespaceDefinition.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StandardSyncInput) == false) {
            return false;
        }
        StandardSyncInput rhs = ((StandardSyncInput) other);
        return ((((((((((((this.resourceRequirements == rhs.resourceRequirements) || ((this.resourceRequirements != null) && this.resourceRequirements.equals(rhs.resourceRequirements))) && ((this.operationSequence == rhs.operationSequence) || ((this.operationSequence != null) && this.operationSequence.equals(rhs.operationSequence)))) && ((this.destinationResourceRequirements == rhs.destinationResourceRequirements) || ((this.destinationResourceRequirements != null) && this.destinationResourceRequirements.equals(rhs.destinationResourceRequirements)))) && ((this.destinationConfiguration == rhs.destinationConfiguration) || ((this.destinationConfiguration != null) && this.destinationConfiguration.equals(rhs.destinationConfiguration)))) && ((this.sourceConfiguration == rhs.sourceConfiguration) || ((this.sourceConfiguration != null) && this.sourceConfiguration.equals(rhs.sourceConfiguration)))) && ((this.prefix == rhs.prefix) || ((this.prefix != null) && this.prefix.equals(rhs.prefix)))) && ((this.catalog == rhs.catalog) || ((this.catalog != null) && this.catalog.equals(rhs.catalog)))) && ((this.sourceResourceRequirements == rhs.sourceResourceRequirements) || ((this.sourceResourceRequirements != null) && this.sourceResourceRequirements.equals(rhs.sourceResourceRequirements)))) && ((this.namespaceFormat == rhs.namespaceFormat) || ((this.namespaceFormat != null) && this.namespaceFormat.equals(rhs.namespaceFormat)))) && ((this.state == rhs.state) || ((this.state != null) && this.state.equals(rhs.state)))) && ((this.namespaceDefinition == rhs.namespaceDefinition) || ((this.namespaceDefinition != null) && this.namespaceDefinition.equals(rhs.namespaceDefinition))));
    }

}
