
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Specification of a connector (source/destination)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentationUrl",
    "changelogUrl",
    "connectionSpecification",
    "supportsIncremental",
    "supportsNormalization",
    "supportsDBT",
    "supported_destination_sync_modes",
    "authSpecification",
    "advanced_auth"
})
public class ConnectorSpecification {

    @JsonProperty("documentationUrl")
    private URI documentationUrl;
    @JsonProperty("changelogUrl")
    private URI changelogUrl;
    /**
     * ConnectorDefinition specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionSpecification")
    @JsonPropertyDescription("ConnectorDefinition specific blob. Must be a valid JSON string.")
    private JsonNode connectionSpecification;
    /**
     * If the connector supports incremental mode or not.
     * 
     */
    @JsonProperty("supportsIncremental")
    @JsonPropertyDescription("If the connector supports incremental mode or not.")
    private Boolean supportsIncremental;
    /**
     * If the connector supports normalization or not.
     * 
     */
    @JsonProperty("supportsNormalization")
    @JsonPropertyDescription("If the connector supports normalization or not.")
    private Boolean supportsNormalization = false;
    /**
     * If the connector supports DBT or not.
     * 
     */
    @JsonProperty("supportsDBT")
    @JsonPropertyDescription("If the connector supports DBT or not.")
    private Boolean supportsDBT = false;
    /**
     * List of destination sync modes supported by the connector
     * 
     */
    @JsonProperty("supported_destination_sync_modes")
    @JsonPropertyDescription("List of destination sync modes supported by the connector")
    private List<DestinationSyncMode> supportedDestinationSyncModes = new ArrayList<DestinationSyncMode>();
    /**
     * deprecated, switching to advanced_auth instead
     * 
     */
    @JsonProperty("authSpecification")
    @JsonPropertyDescription("deprecated, switching to advanced_auth instead")
    private AuthSpecification authSpecification;
    /**
     * Additional and optional specification object to describe what an 'advanced' Auth flow would need to function.
     *   - A connector should be able to fully function with the configuration as described by the ConnectorSpecification in a 'basic' mode.
     *   - The 'advanced' mode provides easier UX for the user with UI improvements and automations. However, this requires further setup on the
     *   server side by instance or workspace admins beforehand. The trade-off is that the user does not have to provide as many technical
     *   inputs anymore and the auth process is faster and easier to complete.
     * 
     */
    @JsonProperty("advanced_auth")
    @JsonPropertyDescription("Additional and optional specification object to describe what an 'advanced' Auth flow would need to function.\n  - A connector should be able to fully function with the configuration as described by the ConnectorSpecification in a 'basic' mode.\n  - The 'advanced' mode provides easier UX for the user with UI improvements and automations. However, this requires further setup on the\n  server side by instance or workspace admins beforehand. The trade-off is that the user does not have to provide as many technical\n  inputs anymore and the auth process is faster and easier to complete.")
    private AdvancedAuth advancedAuth;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("documentationUrl")
    public URI getDocumentationUrl() {
        return documentationUrl;
    }

    @JsonProperty("documentationUrl")
    public void setDocumentationUrl(URI documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public ConnectorSpecification withDocumentationUrl(URI documentationUrl) {
        this.documentationUrl = documentationUrl;
        return this;
    }

    @JsonProperty("changelogUrl")
    public URI getChangelogUrl() {
        return changelogUrl;
    }

    @JsonProperty("changelogUrl")
    public void setChangelogUrl(URI changelogUrl) {
        this.changelogUrl = changelogUrl;
    }

    public ConnectorSpecification withChangelogUrl(URI changelogUrl) {
        this.changelogUrl = changelogUrl;
        return this;
    }

    /**
     * ConnectorDefinition specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionSpecification")
    public JsonNode getConnectionSpecification() {
        return connectionSpecification;
    }

    /**
     * ConnectorDefinition specific blob. Must be a valid JSON string.
     * (Required)
     * 
     */
    @JsonProperty("connectionSpecification")
    public void setConnectionSpecification(JsonNode connectionSpecification) {
        this.connectionSpecification = connectionSpecification;
    }

    public ConnectorSpecification withConnectionSpecification(JsonNode connectionSpecification) {
        this.connectionSpecification = connectionSpecification;
        return this;
    }

    /**
     * If the connector supports incremental mode or not.
     * 
     */
    @JsonProperty("supportsIncremental")
    public Boolean getSupportsIncremental() {
        return supportsIncremental;
    }

    /**
     * If the connector supports incremental mode or not.
     * 
     */
    @JsonProperty("supportsIncremental")
    public void setSupportsIncremental(Boolean supportsIncremental) {
        this.supportsIncremental = supportsIncremental;
    }

    public ConnectorSpecification withSupportsIncremental(Boolean supportsIncremental) {
        this.supportsIncremental = supportsIncremental;
        return this;
    }

    /**
     * If the connector supports normalization or not.
     * 
     */
    @JsonProperty("supportsNormalization")
    public Boolean getSupportsNormalization() {
        return supportsNormalization;
    }

    /**
     * If the connector supports normalization or not.
     * 
     */
    @JsonProperty("supportsNormalization")
    public void setSupportsNormalization(Boolean supportsNormalization) {
        this.supportsNormalization = supportsNormalization;
    }

    public ConnectorSpecification withSupportsNormalization(Boolean supportsNormalization) {
        this.supportsNormalization = supportsNormalization;
        return this;
    }

    /**
     * If the connector supports DBT or not.
     * 
     */
    @JsonProperty("supportsDBT")
    public Boolean getSupportsDBT() {
        return supportsDBT;
    }

    /**
     * If the connector supports DBT or not.
     * 
     */
    @JsonProperty("supportsDBT")
    public void setSupportsDBT(Boolean supportsDBT) {
        this.supportsDBT = supportsDBT;
    }

    public ConnectorSpecification withSupportsDBT(Boolean supportsDBT) {
        this.supportsDBT = supportsDBT;
        return this;
    }

    /**
     * List of destination sync modes supported by the connector
     * 
     */
    @JsonProperty("supported_destination_sync_modes")
    public List<DestinationSyncMode> getSupportedDestinationSyncModes() {
        return supportedDestinationSyncModes;
    }

    /**
     * List of destination sync modes supported by the connector
     * 
     */
    @JsonProperty("supported_destination_sync_modes")
    public void setSupportedDestinationSyncModes(List<DestinationSyncMode> supportedDestinationSyncModes) {
        this.supportedDestinationSyncModes = supportedDestinationSyncModes;
    }

    public ConnectorSpecification withSupportedDestinationSyncModes(List<DestinationSyncMode> supportedDestinationSyncModes) {
        this.supportedDestinationSyncModes = supportedDestinationSyncModes;
        return this;
    }

    /**
     * deprecated, switching to advanced_auth instead
     * 
     */
    @JsonProperty("authSpecification")
    public AuthSpecification getAuthSpecification() {
        return authSpecification;
    }

    /**
     * deprecated, switching to advanced_auth instead
     * 
     */
    @JsonProperty("authSpecification")
    public void setAuthSpecification(AuthSpecification authSpecification) {
        this.authSpecification = authSpecification;
    }

    public ConnectorSpecification withAuthSpecification(AuthSpecification authSpecification) {
        this.authSpecification = authSpecification;
        return this;
    }

    /**
     * Additional and optional specification object to describe what an 'advanced' Auth flow would need to function.
     *   - A connector should be able to fully function with the configuration as described by the ConnectorSpecification in a 'basic' mode.
     *   - The 'advanced' mode provides easier UX for the user with UI improvements and automations. However, this requires further setup on the
     *   server side by instance or workspace admins beforehand. The trade-off is that the user does not have to provide as many technical
     *   inputs anymore and the auth process is faster and easier to complete.
     * 
     */
    @JsonProperty("advanced_auth")
    public AdvancedAuth getAdvancedAuth() {
        return advancedAuth;
    }

    /**
     * Additional and optional specification object to describe what an 'advanced' Auth flow would need to function.
     *   - A connector should be able to fully function with the configuration as described by the ConnectorSpecification in a 'basic' mode.
     *   - The 'advanced' mode provides easier UX for the user with UI improvements and automations. However, this requires further setup on the
     *   server side by instance or workspace admins beforehand. The trade-off is that the user does not have to provide as many technical
     *   inputs anymore and the auth process is faster and easier to complete.
     * 
     */
    @JsonProperty("advanced_auth")
    public void setAdvancedAuth(AdvancedAuth advancedAuth) {
        this.advancedAuth = advancedAuth;
    }

    public ConnectorSpecification withAdvancedAuth(AdvancedAuth advancedAuth) {
        this.advancedAuth = advancedAuth;
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

    public ConnectorSpecification withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ConnectorSpecification.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("documentationUrl");
        sb.append('=');
        sb.append(((this.documentationUrl == null)?"<null>":this.documentationUrl));
        sb.append(',');
        sb.append("changelogUrl");
        sb.append('=');
        sb.append(((this.changelogUrl == null)?"<null>":this.changelogUrl));
        sb.append(',');
        sb.append("connectionSpecification");
        sb.append('=');
        sb.append(((this.connectionSpecification == null)?"<null>":this.connectionSpecification));
        sb.append(',');
        sb.append("supportsIncremental");
        sb.append('=');
        sb.append(((this.supportsIncremental == null)?"<null>":this.supportsIncremental));
        sb.append(',');
        sb.append("supportsNormalization");
        sb.append('=');
        sb.append(((this.supportsNormalization == null)?"<null>":this.supportsNormalization));
        sb.append(',');
        sb.append("supportsDBT");
        sb.append('=');
        sb.append(((this.supportsDBT == null)?"<null>":this.supportsDBT));
        sb.append(',');
        sb.append("supportedDestinationSyncModes");
        sb.append('=');
        sb.append(((this.supportedDestinationSyncModes == null)?"<null>":this.supportedDestinationSyncModes));
        sb.append(',');
        sb.append("authSpecification");
        sb.append('=');
        sb.append(((this.authSpecification == null)?"<null>":this.authSpecification));
        sb.append(',');
        sb.append("advancedAuth");
        sb.append('=');
        sb.append(((this.advancedAuth == null)?"<null>":this.advancedAuth));
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
        result = ((result* 31)+((this.supportsIncremental == null)? 0 :this.supportsIncremental.hashCode()));
        result = ((result* 31)+((this.documentationUrl == null)? 0 :this.documentationUrl.hashCode()));
        result = ((result* 31)+((this.supportsNormalization == null)? 0 :this.supportsNormalization.hashCode()));
        result = ((result* 31)+((this.connectionSpecification == null)? 0 :this.connectionSpecification.hashCode()));
        result = ((result* 31)+((this.changelogUrl == null)? 0 :this.changelogUrl.hashCode()));
        result = ((result* 31)+((this.supportsDBT == null)? 0 :this.supportsDBT.hashCode()));
        result = ((result* 31)+((this.supportedDestinationSyncModes == null)? 0 :this.supportedDestinationSyncModes.hashCode()));
        result = ((result* 31)+((this.advancedAuth == null)? 0 :this.advancedAuth.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.authSpecification == null)? 0 :this.authSpecification.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConnectorSpecification) == false) {
            return false;
        }
        ConnectorSpecification rhs = ((ConnectorSpecification) other);
        return (((((((((((this.supportsIncremental == rhs.supportsIncremental)||((this.supportsIncremental!= null)&&this.supportsIncremental.equals(rhs.supportsIncremental)))&&((this.documentationUrl == rhs.documentationUrl)||((this.documentationUrl!= null)&&this.documentationUrl.equals(rhs.documentationUrl))))&&((this.supportsNormalization == rhs.supportsNormalization)||((this.supportsNormalization!= null)&&this.supportsNormalization.equals(rhs.supportsNormalization))))&&((this.connectionSpecification == rhs.connectionSpecification)||((this.connectionSpecification!= null)&&this.connectionSpecification.equals(rhs.connectionSpecification))))&&((this.changelogUrl == rhs.changelogUrl)||((this.changelogUrl!= null)&&this.changelogUrl.equals(rhs.changelogUrl))))&&((this.supportsDBT == rhs.supportsDBT)||((this.supportsDBT!= null)&&this.supportsDBT.equals(rhs.supportsDBT))))&&((this.supportedDestinationSyncModes == rhs.supportedDestinationSyncModes)||((this.supportedDestinationSyncModes!= null)&&this.supportedDestinationSyncModes.equals(rhs.supportedDestinationSyncModes))))&&((this.advancedAuth == rhs.advancedAuth)||((this.advancedAuth!= null)&&this.advancedAuth.equals(rhs.advancedAuth))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.authSpecification == rhs.authSpecification)||((this.authSpecification!= null)&&this.authSpecification.equals(rhs.authSpecification))));
    }

}
