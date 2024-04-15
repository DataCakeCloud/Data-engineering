
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JobSyncConfig
 * <p>
 * job sync config
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "namespaceDefinition",
    "namespaceFormat",
    "prefix",
    "sourceConfiguration",
    "destinationConfiguration",
    "configuredAirbyteCatalog",
    "sourceDockerImage",
    "destinationDockerImage",
    "sourceResourceRequirements",
    "destinationResourceRequirements",
    "operationSequence",
    "state",
    "resourceRequirements"
})
public class JobSyncConfig implements Serializable
{

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     * 
     */
    @JsonProperty("namespaceDefinition")
    @JsonPropertyDescription("Method used for computing final namespace in destination")
    private NamespaceDefinitionType namespaceDefinition = NamespaceDefinitionType.fromValue("source");
    @JsonProperty("namespaceFormat")
    private String namespaceFormat = null;
    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     * 
     */
    @JsonProperty("prefix")
    @JsonPropertyDescription("Prefix that will be prepended to the name of each stream when it is written to the destination.")
    private String prefix;
    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("sourceConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode sourceConfiguration;
    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("destinationConfiguration")
    @JsonPropertyDescription("Integration specific blob. Must be a valid JSON string.")
    private JsonNode destinationConfiguration;
    /**
     * the configured airbyte catalog
     * (Required)
     * 
     */
    @JsonProperty("configuredAirbyteCatalog")
    @JsonPropertyDescription("the configured airbyte catalog")
    private ConfiguredAirbyteCatalog configuredAirbyteCatalog;
    /**
     * Image name of the source with tag.
     * (Required)
     * 
     */
    @JsonProperty("sourceDockerImage")
    @JsonPropertyDescription("Image name of the source with tag.")
    private String sourceDockerImage;
    /**
     * Image name of the destination with tag.
     * (Required)
     * 
     */
    @JsonProperty("destinationDockerImage")
    @JsonPropertyDescription("Image name of the destination with tag.")
    private String destinationDockerImage;
    /**
     * optional resource requirements to use in source container - this is used instead of `resourceRequirements` for the source container
     * 
     */
    @JsonProperty("sourceResourceRequirements")
    @JsonPropertyDescription("optional resource requirements to use in source container - this is used instead of `resourceRequirements` for the source container")
    private ResourceRequirements sourceResourceRequirements;
    /**
     * optional resource requirements to use in dest container - this is used instead of `resourceRequirements` for the dest container
     * 
     */
    @JsonProperty("destinationResourceRequirements")
    @JsonPropertyDescription("optional resource requirements to use in dest container - this is used instead of `resourceRequirements` for the dest container")
    private ResourceRequirements destinationResourceRequirements;
    /**
     * Sequence of configurations of operations to apply as part of the sync
     * 
     */
    @JsonProperty("operationSequence")
    @JsonPropertyDescription("Sequence of configurations of operations to apply as part of the sync")
    private List<StandardSyncOperation> operationSequence = new ArrayList<StandardSyncOperation>();
    /**
     * State
     * <p>
     * information output by the connection.
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("information output by the connection.")
    private State state;
    /**
     * optional resource requirements to run sync workers - this is used for containers other than the source/dest containers
     * 
     */
    @JsonProperty("resourceRequirements")
    @JsonPropertyDescription("optional resource requirements to run sync workers - this is used for containers other than the source/dest containers")
    private ResourceRequirements resourceRequirements;
    private final static long serialVersionUID = -6615228805817545910L;

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     * 
     */
    @JsonProperty("namespaceDefinition")
    public NamespaceDefinitionType getNamespaceDefinition() {
        return namespaceDefinition;
    }

    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     * 
     */
    @JsonProperty("namespaceDefinition")
    public void setNamespaceDefinition(NamespaceDefinitionType namespaceDefinition) {
        this.namespaceDefinition = namespaceDefinition;
    }

    public JobSyncConfig withNamespaceDefinition(NamespaceDefinitionType namespaceDefinition) {
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

    public JobSyncConfig withNamespaceFormat(String namespaceFormat) {
        this.namespaceFormat = namespaceFormat;
        return this;
    }

    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     * 
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Prefix that will be prepended to the name of each stream when it is written to the destination.
     * 
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public JobSyncConfig withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("sourceConfiguration")
    public JsonNode getSourceConfiguration() {
        return sourceConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("sourceConfiguration")
    public void setSourceConfiguration(JsonNode sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public JobSyncConfig withSourceConfiguration(JsonNode sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
        return this;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("destinationConfiguration")
    public JsonNode getDestinationConfiguration() {
        return destinationConfiguration;
    }

    /**
     * Integration specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("destinationConfiguration")
    public void setDestinationConfiguration(JsonNode destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
    }

    public JobSyncConfig withDestinationConfiguration(JsonNode destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
        return this;
    }

    /**
     * the configured airbyte catalog
     * (Required)
     * 
     */
    @JsonProperty("configuredAirbyteCatalog")
    public ConfiguredAirbyteCatalog getConfiguredAirbyteCatalog() {
        return configuredAirbyteCatalog;
    }

    /**
     * the configured airbyte catalog
     * (Required)
     * 
     */
    @JsonProperty("configuredAirbyteCatalog")
    public void setConfiguredAirbyteCatalog(ConfiguredAirbyteCatalog configuredAirbyteCatalog) {
        this.configuredAirbyteCatalog = configuredAirbyteCatalog;
    }

    public JobSyncConfig withConfiguredAirbyteCatalog(ConfiguredAirbyteCatalog configuredAirbyteCatalog) {
        this.configuredAirbyteCatalog = configuredAirbyteCatalog;
        return this;
    }

    /**
     * Image name of the source with tag.
     * (Required)
     * 
     */
    @JsonProperty("sourceDockerImage")
    public String getSourceDockerImage() {
        return sourceDockerImage;
    }

    /**
     * Image name of the source with tag.
     * (Required)
     * 
     */
    @JsonProperty("sourceDockerImage")
    public void setSourceDockerImage(String sourceDockerImage) {
        this.sourceDockerImage = sourceDockerImage;
    }

    public JobSyncConfig withSourceDockerImage(String sourceDockerImage) {
        this.sourceDockerImage = sourceDockerImage;
        return this;
    }

    /**
     * Image name of the destination with tag.
     * (Required)
     * 
     */
    @JsonProperty("destinationDockerImage")
    public String getDestinationDockerImage() {
        return destinationDockerImage;
    }

    /**
     * Image name of the destination with tag.
     * (Required)
     * 
     */
    @JsonProperty("destinationDockerImage")
    public void setDestinationDockerImage(String destinationDockerImage) {
        this.destinationDockerImage = destinationDockerImage;
    }

    public JobSyncConfig withDestinationDockerImage(String destinationDockerImage) {
        this.destinationDockerImage = destinationDockerImage;
        return this;
    }

    /**
     * optional resource requirements to use in source container - this is used instead of `resourceRequirements` for the source container
     * 
     */
    @JsonProperty("sourceResourceRequirements")
    public ResourceRequirements getSourceResourceRequirements() {
        return sourceResourceRequirements;
    }

    /**
     * optional resource requirements to use in source container - this is used instead of `resourceRequirements` for the source container
     * 
     */
    @JsonProperty("sourceResourceRequirements")
    public void setSourceResourceRequirements(ResourceRequirements sourceResourceRequirements) {
        this.sourceResourceRequirements = sourceResourceRequirements;
    }

    public JobSyncConfig withSourceResourceRequirements(ResourceRequirements sourceResourceRequirements) {
        this.sourceResourceRequirements = sourceResourceRequirements;
        return this;
    }

    /**
     * optional resource requirements to use in dest container - this is used instead of `resourceRequirements` for the dest container
     * 
     */
    @JsonProperty("destinationResourceRequirements")
    public ResourceRequirements getDestinationResourceRequirements() {
        return destinationResourceRequirements;
    }

    /**
     * optional resource requirements to use in dest container - this is used instead of `resourceRequirements` for the dest container
     * 
     */
    @JsonProperty("destinationResourceRequirements")
    public void setDestinationResourceRequirements(ResourceRequirements destinationResourceRequirements) {
        this.destinationResourceRequirements = destinationResourceRequirements;
    }

    public JobSyncConfig withDestinationResourceRequirements(ResourceRequirements destinationResourceRequirements) {
        this.destinationResourceRequirements = destinationResourceRequirements;
        return this;
    }

    /**
     * Sequence of configurations of operations to apply as part of the sync
     * 
     */
    @JsonProperty("operationSequence")
    public List<StandardSyncOperation> getOperationSequence() {
        return operationSequence;
    }

    /**
     * Sequence of configurations of operations to apply as part of the sync
     * 
     */
    @JsonProperty("operationSequence")
    public void setOperationSequence(List<StandardSyncOperation> operationSequence) {
        this.operationSequence = operationSequence;
    }

    public JobSyncConfig withOperationSequence(List<StandardSyncOperation> operationSequence) {
        this.operationSequence = operationSequence;
        return this;
    }

    /**
     * State
     * <p>
     * information output by the connection.
     * 
     */
    @JsonProperty("state")
    public State getState() {
        return state;
    }

    /**
     * State
     * <p>
     * information output by the connection.
     * 
     */
    @JsonProperty("state")
    public void setState(State state) {
        this.state = state;
    }

    public JobSyncConfig withState(State state) {
        this.state = state;
        return this;
    }

    /**
     * optional resource requirements to run sync workers - this is used for containers other than the source/dest containers
     * 
     */
    @JsonProperty("resourceRequirements")
    public ResourceRequirements getResourceRequirements() {
        return resourceRequirements;
    }

    /**
     * optional resource requirements to run sync workers - this is used for containers other than the source/dest containers
     * 
     */
    @JsonProperty("resourceRequirements")
    public void setResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public JobSyncConfig withResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JobSyncConfig.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("namespaceDefinition");
        sb.append('=');
        sb.append(((this.namespaceDefinition == null)?"<null>":this.namespaceDefinition));
        sb.append(',');
        sb.append("namespaceFormat");
        sb.append('=');
        sb.append(((this.namespaceFormat == null)?"<null>":this.namespaceFormat));
        sb.append(',');
        sb.append("prefix");
        sb.append('=');
        sb.append(((this.prefix == null)?"<null>":this.prefix));
        sb.append(',');
        sb.append("sourceConfiguration");
        sb.append('=');
        sb.append(((this.sourceConfiguration == null)?"<null>":this.sourceConfiguration));
        sb.append(',');
        sb.append("destinationConfiguration");
        sb.append('=');
        sb.append(((this.destinationConfiguration == null)?"<null>":this.destinationConfiguration));
        sb.append(',');
        sb.append("configuredAirbyteCatalog");
        sb.append('=');
        sb.append(((this.configuredAirbyteCatalog == null)?"<null>":this.configuredAirbyteCatalog));
        sb.append(',');
        sb.append("sourceDockerImage");
        sb.append('=');
        sb.append(((this.sourceDockerImage == null)?"<null>":this.sourceDockerImage));
        sb.append(',');
        sb.append("destinationDockerImage");
        sb.append('=');
        sb.append(((this.destinationDockerImage == null)?"<null>":this.destinationDockerImage));
        sb.append(',');
        sb.append("sourceResourceRequirements");
        sb.append('=');
        sb.append(((this.sourceResourceRequirements == null)?"<null>":this.sourceResourceRequirements));
        sb.append(',');
        sb.append("destinationResourceRequirements");
        sb.append('=');
        sb.append(((this.destinationResourceRequirements == null)?"<null>":this.destinationResourceRequirements));
        sb.append(',');
        sb.append("operationSequence");
        sb.append('=');
        sb.append(((this.operationSequence == null)?"<null>":this.operationSequence));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("resourceRequirements");
        sb.append('=');
        sb.append(((this.resourceRequirements == null)?"<null>":this.resourceRequirements));
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
        result = ((result* 31)+((this.destinationResourceRequirements == null)? 0 :this.destinationResourceRequirements.hashCode()));
        result = ((result* 31)+((this.operationSequence == null)? 0 :this.operationSequence.hashCode()));
        result = ((result* 31)+((this.prefix == null)? 0 :this.prefix.hashCode()));
        result = ((result* 31)+((this.configuredAirbyteCatalog == null)? 0 :this.configuredAirbyteCatalog.hashCode()));
        result = ((result* 31)+((this.namespaceDefinition == null)? 0 :this.namespaceDefinition.hashCode()));
        result = ((result* 31)+((this.destinationDockerImage == null)? 0 :this.destinationDockerImage.hashCode()));
        result = ((result* 31)+((this.resourceRequirements == null)? 0 :this.resourceRequirements.hashCode()));
        result = ((result* 31)+((this.destinationConfiguration == null)? 0 :this.destinationConfiguration.hashCode()));
        result = ((result* 31)+((this.sourceConfiguration == null)? 0 :this.sourceConfiguration.hashCode()));
        result = ((result* 31)+((this.sourceResourceRequirements == null)? 0 :this.sourceResourceRequirements.hashCode()));
        result = ((result* 31)+((this.namespaceFormat == null)? 0 :this.namespaceFormat.hashCode()));
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.sourceDockerImage == null)? 0 :this.sourceDockerImage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSyncConfig) == false) {
            return false;
        }
        JobSyncConfig rhs = ((JobSyncConfig) other);
        return ((((((((((((((this.destinationResourceRequirements == rhs.destinationResourceRequirements)||((this.destinationResourceRequirements!= null)&&this.destinationResourceRequirements.equals(rhs.destinationResourceRequirements)))&&((this.operationSequence == rhs.operationSequence)||((this.operationSequence!= null)&&this.operationSequence.equals(rhs.operationSequence))))&&((this.prefix == rhs.prefix)||((this.prefix!= null)&&this.prefix.equals(rhs.prefix))))&&((this.configuredAirbyteCatalog == rhs.configuredAirbyteCatalog)||((this.configuredAirbyteCatalog!= null)&&this.configuredAirbyteCatalog.equals(rhs.configuredAirbyteCatalog))))&&((this.namespaceDefinition == rhs.namespaceDefinition)||((this.namespaceDefinition!= null)&&this.namespaceDefinition.equals(rhs.namespaceDefinition))))&&((this.destinationDockerImage == rhs.destinationDockerImage)||((this.destinationDockerImage!= null)&&this.destinationDockerImage.equals(rhs.destinationDockerImage))))&&((this.resourceRequirements == rhs.resourceRequirements)||((this.resourceRequirements!= null)&&this.resourceRequirements.equals(rhs.resourceRequirements))))&&((this.destinationConfiguration == rhs.destinationConfiguration)||((this.destinationConfiguration!= null)&&this.destinationConfiguration.equals(rhs.destinationConfiguration))))&&((this.sourceConfiguration == rhs.sourceConfiguration)||((this.sourceConfiguration!= null)&&this.sourceConfiguration.equals(rhs.sourceConfiguration))))&&((this.sourceResourceRequirements == rhs.sourceResourceRequirements)||((this.sourceResourceRequirements!= null)&&this.sourceResourceRequirements.equals(rhs.sourceResourceRequirements))))&&((this.namespaceFormat == rhs.namespaceFormat)||((this.namespaceFormat!= null)&&this.namespaceFormat.equals(rhs.namespaceFormat))))&&((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state))))&&((this.sourceDockerImage == rhs.sourceDockerImage)||((this.sourceDockerImage!= null)&&this.sourceDockerImage.equals(rhs.sourceDockerImage))));
    }


    /**
     * Namespace Definition
     * <p>
     * Method used for computing final namespace in destination
     * 
     */
    public enum NamespaceDefinitionType {

        SOURCE("source"),
        DESTINATION("destination"),
        CUSTOMFORMAT("customformat");
        private final String value;
        private final static Map<String, NamespaceDefinitionType> CONSTANTS = new HashMap<String, NamespaceDefinitionType>();

        static {
            for (NamespaceDefinitionType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private NamespaceDefinitionType(String value) {
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
        public static NamespaceDefinitionType fromValue(String value) {
            NamespaceDefinitionType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
