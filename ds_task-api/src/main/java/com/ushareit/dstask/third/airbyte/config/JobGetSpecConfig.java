
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;


/**
 * JobGetSpecConfig
 * <p>
 * job check get spec
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dockerImage"
})
public class JobGetSpecConfig implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dockerImage")
    private String dockerImage;
    private final static long serialVersionUID = -6862751337992632819L;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dockerImage")
    public String getDockerImage() {
        return dockerImage;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dockerImage")
    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public JobGetSpecConfig withDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JobGetSpecConfig.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dockerImage");
        sb.append('=');
        sb.append(((this.dockerImage == null)?"<null>":this.dockerImage));
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
        result = ((result* 31)+((this.dockerImage == null)? 0 :this.dockerImage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobGetSpecConfig) == false) {
            return false;
        }
        JobGetSpecConfig rhs = ((JobGetSpecConfig) other);
        return ((this.dockerImage == rhs.dockerImage)||((this.dockerImage!= null)&&this.dockerImage.equals(rhs.dockerImage)));
    }

}
