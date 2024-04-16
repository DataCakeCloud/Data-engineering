
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An object containing any metadata needed to describe this connector's Oauth flow. Deprecated, switching to advanced_auth instead
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "rootObject",
    "oauthFlowInitParameters",
    "oauthFlowOutputParameters"
})
public class OAuth2Specification {

    /**
     * A list of strings representing a pointer to the root object which contains any oauth parameters in the ConnectorSpecification.
     * Examples:
     * if oauth parameters were contained inside the top level, rootObject=[] If they were nested inside another object {'credentials': {'app_id' etc...}, rootObject=['credentials'] If they were inside a oneOf {'switch': {oneOf: [{client_id...}, {non_oauth_param]}},  rootObject=['switch', 0] 
     * 
     */
    @JsonProperty("rootObject")
    @JsonPropertyDescription("A list of strings representing a pointer to the root object which contains any oauth parameters in the ConnectorSpecification.\nExamples:\nif oauth parameters were contained inside the top level, rootObject=[] If they were nested inside another object {'credentials': {'app_id' etc...}, rootObject=['credentials'] If they were inside a oneOf {'switch': {oneOf: [{client_id...}, {non_oauth_param]}},  rootObject=['switch', 0] ")
    private List<Object> rootObject = new ArrayList<Object>();
    /**
     * Pointers to the fields in the rootObject needed to obtain the initial refresh/access tokens for the OAuth flow. Each inner array represents the path in the rootObject of the referenced field. For example. Assume the rootObject contains params 'app_secret', 'app_id' which are needed to get the initial refresh token. If they are not nested in the rootObject, then the array would look like this [['app_secret'], ['app_id']] If they are nested inside an object called 'auth_params' then this array would be [['auth_params', 'app_secret'], ['auth_params', 'app_id']]
     * 
     */
    @JsonProperty("oauthFlowInitParameters")
    @JsonPropertyDescription("Pointers to the fields in the rootObject needed to obtain the initial refresh/access tokens for the OAuth flow. Each inner array represents the path in the rootObject of the referenced field. For example. Assume the rootObject contains params 'app_secret', 'app_id' which are needed to get the initial refresh token. If they are not nested in the rootObject, then the array would look like this [['app_secret'], ['app_id']] If they are nested inside an object called 'auth_params' then this array would be [['auth_params', 'app_secret'], ['auth_params', 'app_id']]")
    private List<List<String>> oauthFlowInitParameters = new ArrayList<List<String>>();
    /**
     * Pointers to the fields in the rootObject which can be populated from successfully completing the oauth flow using the init parameters. This is typically a refresh/access token. Each inner array represents the path in the rootObject of the referenced field.
     * 
     */
    @JsonProperty("oauthFlowOutputParameters")
    @JsonPropertyDescription("Pointers to the fields in the rootObject which can be populated from successfully completing the oauth flow using the init parameters. This is typically a refresh/access token. Each inner array represents the path in the rootObject of the referenced field.")
    private List<List<String>> oauthFlowOutputParameters = new ArrayList<List<String>>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * A list of strings representing a pointer to the root object which contains any oauth parameters in the ConnectorSpecification.
     * Examples:
     * if oauth parameters were contained inside the top level, rootObject=[] If they were nested inside another object {'credentials': {'app_id' etc...}, rootObject=['credentials'] If they were inside a oneOf {'switch': {oneOf: [{client_id...}, {non_oauth_param]}},  rootObject=['switch', 0] 
     * 
     */
    @JsonProperty("rootObject")
    public List<Object> getRootObject() {
        return rootObject;
    }

    /**
     * A list of strings representing a pointer to the root object which contains any oauth parameters in the ConnectorSpecification.
     * Examples:
     * if oauth parameters were contained inside the top level, rootObject=[] If they were nested inside another object {'credentials': {'app_id' etc...}, rootObject=['credentials'] If they were inside a oneOf {'switch': {oneOf: [{client_id...}, {non_oauth_param]}},  rootObject=['switch', 0] 
     * 
     */
    @JsonProperty("rootObject")
    public void setRootObject(List<Object> rootObject) {
        this.rootObject = rootObject;
    }

    public OAuth2Specification withRootObject(List<Object> rootObject) {
        this.rootObject = rootObject;
        return this;
    }

    /**
     * Pointers to the fields in the rootObject needed to obtain the initial refresh/access tokens for the OAuth flow. Each inner array represents the path in the rootObject of the referenced field. For example. Assume the rootObject contains params 'app_secret', 'app_id' which are needed to get the initial refresh token. If they are not nested in the rootObject, then the array would look like this [['app_secret'], ['app_id']] If they are nested inside an object called 'auth_params' then this array would be [['auth_params', 'app_secret'], ['auth_params', 'app_id']]
     * 
     */
    @JsonProperty("oauthFlowInitParameters")
    public List<List<String>> getOauthFlowInitParameters() {
        return oauthFlowInitParameters;
    }

    /**
     * Pointers to the fields in the rootObject needed to obtain the initial refresh/access tokens for the OAuth flow. Each inner array represents the path in the rootObject of the referenced field. For example. Assume the rootObject contains params 'app_secret', 'app_id' which are needed to get the initial refresh token. If they are not nested in the rootObject, then the array would look like this [['app_secret'], ['app_id']] If they are nested inside an object called 'auth_params' then this array would be [['auth_params', 'app_secret'], ['auth_params', 'app_id']]
     * 
     */
    @JsonProperty("oauthFlowInitParameters")
    public void setOauthFlowInitParameters(List<List<String>> oauthFlowInitParameters) {
        this.oauthFlowInitParameters = oauthFlowInitParameters;
    }

    public OAuth2Specification withOauthFlowInitParameters(List<List<String>> oauthFlowInitParameters) {
        this.oauthFlowInitParameters = oauthFlowInitParameters;
        return this;
    }

    /**
     * Pointers to the fields in the rootObject which can be populated from successfully completing the oauth flow using the init parameters. This is typically a refresh/access token. Each inner array represents the path in the rootObject of the referenced field.
     * 
     */
    @JsonProperty("oauthFlowOutputParameters")
    public List<List<String>> getOauthFlowOutputParameters() {
        return oauthFlowOutputParameters;
    }

    /**
     * Pointers to the fields in the rootObject which can be populated from successfully completing the oauth flow using the init parameters. This is typically a refresh/access token. Each inner array represents the path in the rootObject of the referenced field.
     * 
     */
    @JsonProperty("oauthFlowOutputParameters")
    public void setOauthFlowOutputParameters(List<List<String>> oauthFlowOutputParameters) {
        this.oauthFlowOutputParameters = oauthFlowOutputParameters;
    }

    public OAuth2Specification withOauthFlowOutputParameters(List<List<String>> oauthFlowOutputParameters) {
        this.oauthFlowOutputParameters = oauthFlowOutputParameters;
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

    public OAuth2Specification withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(OAuth2Specification.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("rootObject");
        sb.append('=');
        sb.append(((this.rootObject == null)?"<null>":this.rootObject));
        sb.append(',');
        sb.append("oauthFlowInitParameters");
        sb.append('=');
        sb.append(((this.oauthFlowInitParameters == null)?"<null>":this.oauthFlowInitParameters));
        sb.append(',');
        sb.append("oauthFlowOutputParameters");
        sb.append('=');
        sb.append(((this.oauthFlowOutputParameters == null)?"<null>":this.oauthFlowOutputParameters));
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
        result = ((result* 31)+((this.oauthFlowOutputParameters == null)? 0 :this.oauthFlowOutputParameters.hashCode()));
        result = ((result* 31)+((this.rootObject == null)? 0 :this.rootObject.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.oauthFlowInitParameters == null)? 0 :this.oauthFlowInitParameters.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OAuth2Specification) == false) {
            return false;
        }
        OAuth2Specification rhs = ((OAuth2Specification) other);
        return (((((this.oauthFlowOutputParameters == rhs.oauthFlowOutputParameters)||((this.oauthFlowOutputParameters!= null)&&this.oauthFlowOutputParameters.equals(rhs.oauthFlowOutputParameters)))&&((this.rootObject == rhs.rootObject)||((this.rootObject!= null)&&this.rootObject.equals(rhs.rootObject))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.oauthFlowInitParameters == rhs.oauthFlowInitParameters)||((this.oauthFlowInitParameters!= null)&&this.oauthFlowInitParameters.equals(rhs.oauthFlowInitParameters))));
    }

}
