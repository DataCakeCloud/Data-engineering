
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;


/**
 * StandardDiscoverCatalogOutput
 * <p>
 * describes the standard output for any discovery run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "catalog"
})
public class StandardDiscoverCatalogOutput implements Serializable
{

    /**
     * describes the available schemas.
     * (Required)
     * 
     */
    @JsonProperty("catalog")
    @JsonPropertyDescription("describes the available schemas.")
    private AirbyteCatalog catalog;
    private final static long serialVersionUID = -5598287480312054363L;

    /**
     * describes the available schemas.
     * (Required)
     * 
     */
    @JsonProperty("catalog")
    public AirbyteCatalog getCatalog() {
        return catalog;
    }

    /**
     * describes the available schemas.
     * (Required)
     * 
     */
    @JsonProperty("catalog")
    public void setCatalog(AirbyteCatalog catalog) {
        this.catalog = catalog;
    }

    public StandardDiscoverCatalogOutput withCatalog(AirbyteCatalog catalog) {
        this.catalog = catalog;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StandardDiscoverCatalogOutput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("catalog");
        sb.append('=');
        sb.append(((this.catalog == null)?"<null>":this.catalog));
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
        result = ((result* 31)+((this.catalog == null)? 0 :this.catalog.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StandardDiscoverCatalogOutput) == false) {
            return false;
        }
        StandardDiscoverCatalogOutput rhs = ((StandardDiscoverCatalogOutput) other);
        return ((this.catalog == rhs.catalog)||((this.catalog!= null)&&this.catalog.equals(rhs.catalog)));
    }

}
