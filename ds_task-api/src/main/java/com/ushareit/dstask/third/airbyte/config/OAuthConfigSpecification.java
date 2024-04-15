
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "oauth_user_input_from_connector_config_specification",
    "complete_oauth_output_specification",
    "complete_oauth_server_input_specification",
    "complete_oauth_server_output_specification"
})
public class OAuthConfigSpecification {

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations used as input to OAuth.
     * Must be a valid non-nested JSON that refers to properties from ConnectorSpecification.connectionSpecification
     * using special annotation 'path_in_connector_config'.
     * These are input values the user is entering through the UI to authenticate to the connector, that might also shared
     * as inputs for syncing data via the connector.
     * 
     * Examples:
     * 
     * if no connector values is shared during oauth flow, oauth_user_input_from_connector_config_specification=[]
     * if connector values such as 'app_id' inside the top level are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['app_id']
     *     }
     *   }
     * if connector values such as 'info.app_id' nested inside another object are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['info', 'app_id']
     *     }
     *   }
     * 
     */
    @JsonProperty("oauth_user_input_from_connector_config_specification")
    @JsonPropertyDescription("OAuth specific blob. This is a Json Schema used to validate Json configurations used as input to OAuth.\nMust be a valid non-nested JSON that refers to properties from ConnectorSpecification.connectionSpecification\nusing special annotation 'path_in_connector_config'.\nThese are input values the user is entering through the UI to authenticate to the connector, that might also shared\nas inputs for syncing data via the connector.\n\nExamples:\n\nif no connector values is shared during oauth flow, oauth_user_input_from_connector_config_specification=[]\nif connector values such as 'app_id' inside the top level are used to generate the API url for the oauth flow,\n  oauth_user_input_from_connector_config_specification={\n    app_id: {\n      type: string\n      path_in_connector_config: ['app_id']\n    }\n  }\nif connector values such as 'info.app_id' nested inside another object are used to generate the API url for the oauth flow,\n  oauth_user_input_from_connector_config_specification={\n    app_id: {\n      type: string\n      path_in_connector_config: ['info', 'app_id']\n    }\n  }")
    private JsonNode oauthUserInputFromConnectorConfigSpecification;
    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations produced by the OAuth flows as they are
     * returned by the distant OAuth APIs.
     * Must be a valid JSON describing the fields to merge back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *     complete_oauth_output_specification={
     *       refresh_token: {
     *         type: string,
     *         path_in_connector_config: ['credentials', 'refresh_token']
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_output_specification")
    @JsonPropertyDescription("OAuth specific blob. This is a Json Schema used to validate Json configurations produced by the OAuth flows as they are\nreturned by the distant OAuth APIs.\nMust be a valid JSON describing the fields to merge back to `ConnectorSpecification.connectionSpecification`.\nFor each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,\n\nExamples:\n\n    complete_oauth_output_specification={\n      refresh_token: {\n        type: string,\n        path_in_connector_config: ['credentials', 'refresh_token']\n      }\n    }")
    private JsonNode completeOauthOutputSpecification;
    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations.
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * server when completing an OAuth flow (typically exchanging an auth code for refresh token).
     * 
     * Examples:
     * 
     *     complete_oauth_server_input_specification={
     *       client_id: {
     *         type: string
     *       },
     *       client_secret: {
     *         type: string
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_server_input_specification")
    @JsonPropertyDescription("OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations.\nMust be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the\nserver when completing an OAuth flow (typically exchanging an auth code for refresh token).\n\nExamples:\n\n    complete_oauth_server_input_specification={\n      client_id: {\n        type: string\n      },\n      client_secret: {\n        type: string\n      }\n    }")
    private JsonNode completeOauthServerInputSpecification;
    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations that
     * also need to be merged back into the connector configuration at runtime.
     * This is a subset configuration of `complete_oauth_server_input_specification` that filters fields out to retain only the ones that
     * are necessary for the connector to function with OAuth. (some fields could be used during oauth flows but not needed afterwards, therefore
     * they would be listed in the `complete_oauth_server_input_specification` but not `complete_oauth_server_output_specification`)
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * connector when using OAuth flow APIs.
     * These fields are to be merged back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *       complete_oauth_server_output_specification={
     *         client_id: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_id']
     *         },
     *         client_secret: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_secret']
     *         }
     *       }
     * 
     */
    @JsonProperty("complete_oauth_server_output_specification")
    @JsonPropertyDescription("OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations that\nalso need to be merged back into the connector configuration at runtime.\nThis is a subset configuration of `complete_oauth_server_input_specification` that filters fields out to retain only the ones that\nare necessary for the connector to function with OAuth. (some fields could be used during oauth flows but not needed afterwards, therefore\nthey would be listed in the `complete_oauth_server_input_specification` but not `complete_oauth_server_output_specification`)\nMust be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the\nconnector when using OAuth flow APIs.\nThese fields are to be merged back to `ConnectorSpecification.connectionSpecification`.\nFor each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,\n\nExamples:\n\n      complete_oauth_server_output_specification={\n        client_id: {\n          type: string,\n          path_in_connector_config: ['credentials', 'client_id']\n        },\n        client_secret: {\n          type: string,\n          path_in_connector_config: ['credentials', 'client_secret']\n        }\n      }")
    private JsonNode completeOauthServerOutputSpecification;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations used as input to OAuth.
     * Must be a valid non-nested JSON that refers to properties from ConnectorSpecification.connectionSpecification
     * using special annotation 'path_in_connector_config'.
     * These are input values the user is entering through the UI to authenticate to the connector, that might also shared
     * as inputs for syncing data via the connector.
     * 
     * Examples:
     * 
     * if no connector values is shared during oauth flow, oauth_user_input_from_connector_config_specification=[]
     * if connector values such as 'app_id' inside the top level are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['app_id']
     *     }
     *   }
     * if connector values such as 'info.app_id' nested inside another object are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['info', 'app_id']
     *     }
     *   }
     * 
     */
    @JsonProperty("oauth_user_input_from_connector_config_specification")
    public JsonNode getOauthUserInputFromConnectorConfigSpecification() {
        return oauthUserInputFromConnectorConfigSpecification;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations used as input to OAuth.
     * Must be a valid non-nested JSON that refers to properties from ConnectorSpecification.connectionSpecification
     * using special annotation 'path_in_connector_config'.
     * These are input values the user is entering through the UI to authenticate to the connector, that might also shared
     * as inputs for syncing data via the connector.
     * 
     * Examples:
     * 
     * if no connector values is shared during oauth flow, oauth_user_input_from_connector_config_specification=[]
     * if connector values such as 'app_id' inside the top level are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['app_id']
     *     }
     *   }
     * if connector values such as 'info.app_id' nested inside another object are used to generate the API url for the oauth flow,
     *   oauth_user_input_from_connector_config_specification={
     *     app_id: {
     *       type: string
     *       path_in_connector_config: ['info', 'app_id']
     *     }
     *   }
     * 
     */
    @JsonProperty("oauth_user_input_from_connector_config_specification")
    public void setOauthUserInputFromConnectorConfigSpecification(JsonNode oauthUserInputFromConnectorConfigSpecification) {
        this.oauthUserInputFromConnectorConfigSpecification = oauthUserInputFromConnectorConfigSpecification;
    }

    public OAuthConfigSpecification withOauthUserInputFromConnectorConfigSpecification(JsonNode oauthUserInputFromConnectorConfigSpecification) {
        this.oauthUserInputFromConnectorConfigSpecification = oauthUserInputFromConnectorConfigSpecification;
        return this;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations produced by the OAuth flows as they are
     * returned by the distant OAuth APIs.
     * Must be a valid JSON describing the fields to merge back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *     complete_oauth_output_specification={
     *       refresh_token: {
     *         type: string,
     *         path_in_connector_config: ['credentials', 'refresh_token']
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_output_specification")
    public JsonNode getCompleteOauthOutputSpecification() {
        return completeOauthOutputSpecification;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations produced by the OAuth flows as they are
     * returned by the distant OAuth APIs.
     * Must be a valid JSON describing the fields to merge back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *     complete_oauth_output_specification={
     *       refresh_token: {
     *         type: string,
     *         path_in_connector_config: ['credentials', 'refresh_token']
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_output_specification")
    public void setCompleteOauthOutputSpecification(JsonNode completeOauthOutputSpecification) {
        this.completeOauthOutputSpecification = completeOauthOutputSpecification;
    }

    public OAuthConfigSpecification withCompleteOauthOutputSpecification(JsonNode completeOauthOutputSpecification) {
        this.completeOauthOutputSpecification = completeOauthOutputSpecification;
        return this;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations.
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * server when completing an OAuth flow (typically exchanging an auth code for refresh token).
     * 
     * Examples:
     * 
     *     complete_oauth_server_input_specification={
     *       client_id: {
     *         type: string
     *       },
     *       client_secret: {
     *         type: string
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_server_input_specification")
    public JsonNode getCompleteOauthServerInputSpecification() {
        return completeOauthServerInputSpecification;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations.
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * server when completing an OAuth flow (typically exchanging an auth code for refresh token).
     * 
     * Examples:
     * 
     *     complete_oauth_server_input_specification={
     *       client_id: {
     *         type: string
     *       },
     *       client_secret: {
     *         type: string
     *       }
     *     }
     * 
     */
    @JsonProperty("complete_oauth_server_input_specification")
    public void setCompleteOauthServerInputSpecification(JsonNode completeOauthServerInputSpecification) {
        this.completeOauthServerInputSpecification = completeOauthServerInputSpecification;
    }

    public OAuthConfigSpecification withCompleteOauthServerInputSpecification(JsonNode completeOauthServerInputSpecification) {
        this.completeOauthServerInputSpecification = completeOauthServerInputSpecification;
        return this;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations that
     * also need to be merged back into the connector configuration at runtime.
     * This is a subset configuration of `complete_oauth_server_input_specification` that filters fields out to retain only the ones that
     * are necessary for the connector to function with OAuth. (some fields could be used during oauth flows but not needed afterwards, therefore
     * they would be listed in the `complete_oauth_server_input_specification` but not `complete_oauth_server_output_specification`)
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * connector when using OAuth flow APIs.
     * These fields are to be merged back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *       complete_oauth_server_output_specification={
     *         client_id: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_id']
     *         },
     *         client_secret: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_secret']
     *         }
     *       }
     * 
     */
    @JsonProperty("complete_oauth_server_output_specification")
    public JsonNode getCompleteOauthServerOutputSpecification() {
        return completeOauthServerOutputSpecification;
    }

    /**
     * OAuth specific blob. This is a Json Schema used to validate Json configurations persisted as Airbyte Server configurations that
     * also need to be merged back into the connector configuration at runtime.
     * This is a subset configuration of `complete_oauth_server_input_specification` that filters fields out to retain only the ones that
     * are necessary for the connector to function with OAuth. (some fields could be used during oauth flows but not needed afterwards, therefore
     * they would be listed in the `complete_oauth_server_input_specification` but not `complete_oauth_server_output_specification`)
     * Must be a valid non-nested JSON describing additional fields configured by the Airbyte Instance or Workspace Admins to be used by the
     * connector when using OAuth flow APIs.
     * These fields are to be merged back to `ConnectorSpecification.connectionSpecification`.
     * For each field, a special annotation `path_in_connector_config` can be specified to determine where to merge it,
     * 
     * Examples:
     * 
     *       complete_oauth_server_output_specification={
     *         client_id: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_id']
     *         },
     *         client_secret: {
     *           type: string,
     *           path_in_connector_config: ['credentials', 'client_secret']
     *         }
     *       }
     * 
     */
    @JsonProperty("complete_oauth_server_output_specification")
    public void setCompleteOauthServerOutputSpecification(JsonNode completeOauthServerOutputSpecification) {
        this.completeOauthServerOutputSpecification = completeOauthServerOutputSpecification;
    }

    public OAuthConfigSpecification withCompleteOauthServerOutputSpecification(JsonNode completeOauthServerOutputSpecification) {
        this.completeOauthServerOutputSpecification = completeOauthServerOutputSpecification;
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

    public OAuthConfigSpecification withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(OAuthConfigSpecification.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("oauthUserInputFromConnectorConfigSpecification");
        sb.append('=');
        sb.append(((this.oauthUserInputFromConnectorConfigSpecification == null)?"<null>":this.oauthUserInputFromConnectorConfigSpecification));
        sb.append(',');
        sb.append("completeOauthOutputSpecification");
        sb.append('=');
        sb.append(((this.completeOauthOutputSpecification == null)?"<null>":this.completeOauthOutputSpecification));
        sb.append(',');
        sb.append("completeOauthServerInputSpecification");
        sb.append('=');
        sb.append(((this.completeOauthServerInputSpecification == null)?"<null>":this.completeOauthServerInputSpecification));
        sb.append(',');
        sb.append("completeOauthServerOutputSpecification");
        sb.append('=');
        sb.append(((this.completeOauthServerOutputSpecification == null)?"<null>":this.completeOauthServerOutputSpecification));
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
        result = ((result* 31)+((this.completeOauthServerInputSpecification == null)? 0 :this.completeOauthServerInputSpecification.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.completeOauthOutputSpecification == null)? 0 :this.completeOauthOutputSpecification.hashCode()));
        result = ((result* 31)+((this.oauthUserInputFromConnectorConfigSpecification == null)? 0 :this.oauthUserInputFromConnectorConfigSpecification.hashCode()));
        result = ((result* 31)+((this.completeOauthServerOutputSpecification == null)? 0 :this.completeOauthServerOutputSpecification.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OAuthConfigSpecification) == false) {
            return false;
        }
        OAuthConfigSpecification rhs = ((OAuthConfigSpecification) other);
        return ((((((this.completeOauthServerInputSpecification == rhs.completeOauthServerInputSpecification)||((this.completeOauthServerInputSpecification!= null)&&this.completeOauthServerInputSpecification.equals(rhs.completeOauthServerInputSpecification)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.completeOauthOutputSpecification == rhs.completeOauthOutputSpecification)||((this.completeOauthOutputSpecification!= null)&&this.completeOauthOutputSpecification.equals(rhs.completeOauthOutputSpecification))))&&((this.oauthUserInputFromConnectorConfigSpecification == rhs.oauthUserInputFromConnectorConfigSpecification)||((this.oauthUserInputFromConnectorConfigSpecification!= null)&&this.oauthUserInputFromConnectorConfigSpecification.equals(rhs.oauthUserInputFromConnectorConfigSpecification))))&&((this.completeOauthServerOutputSpecification == rhs.completeOauthServerOutputSpecification)||((this.completeOauthServerOutputSpecification!= null)&&this.completeOauthServerOutputSpecification.equals(rhs.completeOauthServerOutputSpecification))));
    }

}
