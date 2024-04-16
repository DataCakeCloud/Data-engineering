
package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * JobRunConfig
 * <p>
 * job run config
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobId",
    "attemptId"
})
public class JobRunConfig {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    private String jobId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attemptId")
    private Long attemptId;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public String getJobId() {
        return jobId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobRunConfig withJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attemptId")
    public Long getAttemptId() {
        return attemptId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attemptId")
    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public JobRunConfig withAttemptId(Long attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JobRunConfig.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jobId");
        sb.append('=');
        sb.append(((this.jobId == null)?"<null>":this.jobId));
        sb.append(',');
        sb.append("attemptId");
        sb.append('=');
        sb.append(((this.attemptId == null)?"<null>":this.attemptId));
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
        result = ((result* 31)+((this.jobId == null)? 0 :this.jobId.hashCode()));
        result = ((result* 31)+((this.attemptId == null)? 0 :this.attemptId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobRunConfig) == false) {
            return false;
        }
        JobRunConfig rhs = ((JobRunConfig) other);
        return (((this.jobId == rhs.jobId)||((this.jobId!= null)&&this.jobId.equals(rhs.jobId)))&&((this.attemptId == rhs.attemptId)||((this.attemptId!= null)&&this.attemptId.equals(rhs.attemptId))));
    }

}
