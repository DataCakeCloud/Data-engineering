
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * sets resource requirements for a specific job type for an actor definition. these values override the default, if both are set.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobType",
    "resourceRequirements"
})
public class JobTypeResourceLimit implements Serializable
{

    /**
     * JobType
     * <p>
     * enum that describes the different types of jobs that the platform runs.
     * (Required)
     * 
     */
    @JsonProperty("jobType")
    @JsonPropertyDescription("enum that describes the different types of jobs that the platform runs.")
    private JobType jobType;
    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     * (Required)
     * 
     */
    @JsonProperty("resourceRequirements")
    @JsonPropertyDescription("generic configuration for pod source requirements")
    private ResourceRequirements resourceRequirements;
    private final static long serialVersionUID = -1769322469833859749L;

    /**
     * JobType
     * <p>
     * enum that describes the different types of jobs that the platform runs.
     * (Required)
     * 
     */
    @JsonProperty("jobType")
    public JobType getJobType() {
        return jobType;
    }

    /**
     * JobType
     * <p>
     * enum that describes the different types of jobs that the platform runs.
     * (Required)
     * 
     */
    @JsonProperty("jobType")
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public JobTypeResourceLimit withJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     * (Required)
     * 
     */
    @JsonProperty("resourceRequirements")
    public ResourceRequirements getResourceRequirements() {
        return resourceRequirements;
    }

    /**
     * ResourceRequirements
     * <p>
     * generic configuration for pod source requirements
     * (Required)
     * 
     */
    @JsonProperty("resourceRequirements")
    public void setResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public JobTypeResourceLimit withResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JobTypeResourceLimit.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jobType");
        sb.append('=');
        sb.append(((this.jobType == null)?"<null>":this.jobType));
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
        result = ((result* 31)+((this.jobType == null)? 0 :this.jobType.hashCode()));
        result = ((result* 31)+((this.resourceRequirements == null)? 0 :this.resourceRequirements.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTypeResourceLimit) == false) {
            return false;
        }
        JobTypeResourceLimit rhs = ((JobTypeResourceLimit) other);
        return (((this.jobType == rhs.jobType)||((this.jobType!= null)&&this.jobType.equals(rhs.jobType)))&&((this.resourceRequirements == rhs.resourceRequirements)||((this.resourceRequirements!= null)&&this.resourceRequirements.equals(rhs.resourceRequirements))));
    }


    /**
     * JobType
     * <p>
     * enum that describes the different types of jobs that the platform runs.
     * 
     */
    public enum JobType {

        GET_SPEC("get_spec"),
        CHECK_CONNECTION("check_connection"),
        DISCOVER_SCHEMA("discover_schema"),
        SYNC("entrypoints/sync"),
        RESET_CONNECTION("reset_connection"),
        CONNECTION_UPDATER("connection_updater"),
        REPLICATE("replicate");
        private final String value;
        private final static Map<String, JobType> CONSTANTS = new HashMap<String, JobType>();

        static {
            for (JobType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobType(String value) {
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
        public static JobType fromValue(String value) {
            JobType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
