
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Additional and optional specification object to describe what an 'advanced' Auth flow would need to function.
 *   - A connector should be able to fully function with the configuration as described by the ConnectorSpecification in a 'basic' mode.
 *   - The 'advanced' mode provides easier UX for the user with UI improvements and automations. However, this requires further setup on the
 *   server side by instance or workspace admins beforehand. The trade-off is that the user does not have to provide as many technical
 *   inputs anymore and the auth process is faster and easier to complete.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "auth_flow_type",
    "predicate_key",
    "predicate_value",
    "oauth_config_specification"
})
public class AdvancedAuth {

    @JsonProperty("auth_flow_type")
    private AuthFlowType authFlowType;
    /**
     * Json Path to a field in the connectorSpecification that should exist for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_key")
    @JsonPropertyDescription("Json Path to a field in the connectorSpecification that should exist for the advanced auth to be applicable.")
    private List<String> predicateKey = new ArrayList<String>();
    /**
     * Value of the predicate_key fields for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_value")
    @JsonPropertyDescription("Value of the predicate_key fields for the advanced auth to be applicable.")
    private String predicateValue;
    @JsonProperty("oauth_config_specification")
    private OAuthConfigSpecification oauthConfigSpecification;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("auth_flow_type")
    public AuthFlowType getAuthFlowType() {
        return authFlowType;
    }

    @JsonProperty("auth_flow_type")
    public void setAuthFlowType(AuthFlowType authFlowType) {
        this.authFlowType = authFlowType;
    }

    public AdvancedAuth withAuthFlowType(AuthFlowType authFlowType) {
        this.authFlowType = authFlowType;
        return this;
    }

    /**
     * Json Path to a field in the connectorSpecification that should exist for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_key")
    public List<String> getPredicateKey() {
        return predicateKey;
    }

    /**
     * Json Path to a field in the connectorSpecification that should exist for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_key")
    public void setPredicateKey(List<String> predicateKey) {
        this.predicateKey = predicateKey;
    }

    public AdvancedAuth withPredicateKey(List<String> predicateKey) {
        this.predicateKey = predicateKey;
        return this;
    }

    /**
     * Value of the predicate_key fields for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_value")
    public String getPredicateValue() {
        return predicateValue;
    }

    /**
     * Value of the predicate_key fields for the advanced auth to be applicable.
     * 
     */
    @JsonProperty("predicate_value")
    public void setPredicateValue(String predicateValue) {
        this.predicateValue = predicateValue;
    }

    public AdvancedAuth withPredicateValue(String predicateValue) {
        this.predicateValue = predicateValue;
        return this;
    }

    @JsonProperty("oauth_config_specification")
    public OAuthConfigSpecification getOauthConfigSpecification() {
        return oauthConfigSpecification;
    }

    @JsonProperty("oauth_config_specification")
    public void setOauthConfigSpecification(OAuthConfigSpecification oauthConfigSpecification) {
        this.oauthConfigSpecification = oauthConfigSpecification;
    }

    public AdvancedAuth withOauthConfigSpecification(OAuthConfigSpecification oauthConfigSpecification) {
        this.oauthConfigSpecification = oauthConfigSpecification;
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

    public AdvancedAuth withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AdvancedAuth.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("authFlowType");
        sb.append('=');
        sb.append(((this.authFlowType == null)?"<null>":this.authFlowType));
        sb.append(',');
        sb.append("predicateKey");
        sb.append('=');
        sb.append(((this.predicateKey == null)?"<null>":this.predicateKey));
        sb.append(',');
        sb.append("predicateValue");
        sb.append('=');
        sb.append(((this.predicateValue == null)?"<null>":this.predicateValue));
        sb.append(',');
        sb.append("oauthConfigSpecification");
        sb.append('=');
        sb.append(((this.oauthConfigSpecification == null)?"<null>":this.oauthConfigSpecification));
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
        result = ((result* 31)+((this.predicateKey == null)? 0 :this.predicateKey.hashCode()));
        result = ((result* 31)+((this.predicateValue == null)? 0 :this.predicateValue.hashCode()));
        result = ((result* 31)+((this.authFlowType == null)? 0 :this.authFlowType.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.oauthConfigSpecification == null)? 0 :this.oauthConfigSpecification.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdvancedAuth) == false) {
            return false;
        }
        AdvancedAuth rhs = ((AdvancedAuth) other);
        return ((((((this.predicateKey == rhs.predicateKey)||((this.predicateKey!= null)&&this.predicateKey.equals(rhs.predicateKey)))&&((this.predicateValue == rhs.predicateValue)||((this.predicateValue!= null)&&this.predicateValue.equals(rhs.predicateValue))))&&((this.authFlowType == rhs.authFlowType)||((this.authFlowType!= null)&&this.authFlowType.equals(rhs.authFlowType))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.oauthConfigSpecification == rhs.oauthConfigSpecification)||((this.oauthConfigSpecification!= null)&&this.oauthConfigSpecification.equals(rhs.oauthConfigSpecification))));
    }

    public enum AuthFlowType {

        OAUTH_2_0("oauth2.0"),
        OAUTH_1_0("oauth1.0");
        private final String value;
        private final static Map<String, AuthFlowType> CONSTANTS = new HashMap<String, AuthFlowType>();

        static {
            for (AuthFlowType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private AuthFlowType(String value) {
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
        public static AuthFlowType fromValue(String value) {
            AuthFlowType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
