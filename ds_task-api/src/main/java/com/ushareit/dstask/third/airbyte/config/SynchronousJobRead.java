package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ushareit.dstask.third.airbyte.common.enums.JobConfigType;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;


@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2022-06-23T15:55:19.222177+08:00[Asia/Shanghai]")
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class SynchronousJobRead {

    private @Valid UUID id;
    private @Valid JobConfigType configType;
    private @Valid String configId;
    private @Valid Long createdAt;
    private @Valid Long endedAt;
    private @Valid Boolean succeeded;
    private @Valid LogRead logs;

    /**
     *
     **/
    public SynchronousJobRead id(UUID id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    @NotNull
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     *
     **/
    public SynchronousJobRead configType(JobConfigType configType) {
        this.configType = configType;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("configType")
    @NotNull
    public JobConfigType getConfigType() {
        return configType;
    }

    public void setConfigType(JobConfigType configType) {
        this.configType = configType;
    }

    /**
     * only present if a config id was provided.
     **/
    public SynchronousJobRead configId(String configId) {
        this.configId = configId;
        return this;
    }


    @ApiModelProperty(value = "only present if a config id was provided.")
    @JsonProperty("configId")
    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    /**
     *
     **/
    public SynchronousJobRead createdAt(Long createdAt) {
        this.createdAt = createdAt;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("createdAt")
    @NotNull
    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     **/
    public SynchronousJobRead endedAt(Long endedAt) {
        this.endedAt = endedAt;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("endedAt")
    @NotNull
    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Long endedAt) {
        this.endedAt = endedAt;
    }

    /**
     *
     **/
    public SynchronousJobRead succeeded(Boolean succeeded) {
        this.succeeded = succeeded;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("succeeded")
    @NotNull
    public Boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(Boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     *
     **/
    public SynchronousJobRead logs(LogRead logs) {
        this.logs = logs;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("logs")
    public LogRead getLogs() {
        return logs;
    }

    public void setLogs(LogRead logs) {
        this.logs = logs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SynchronousJobRead synchronousJobRead = (SynchronousJobRead) o;
        return Objects.equals(this.id, synchronousJobRead.id) &&
                Objects.equals(this.configType, synchronousJobRead.configType) &&
                Objects.equals(this.configId, synchronousJobRead.configId) &&
                Objects.equals(this.createdAt, synchronousJobRead.createdAt) &&
                Objects.equals(this.endedAt, synchronousJobRead.endedAt) &&
                Objects.equals(this.succeeded, synchronousJobRead.succeeded) &&
                Objects.equals(this.logs, synchronousJobRead.logs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, configType, configId, createdAt, endedAt, succeeded, logs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SynchronousJobRead {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    configType: ").append(toIndentedString(configType)).append("\n");
        sb.append("    configId: ").append(toIndentedString(configId)).append("\n");
        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    endedAt: ").append(toIndentedString(endedAt)).append("\n");
        sb.append("    succeeded: ").append(toIndentedString(succeeded)).append("\n");
        sb.append("    logs: ").append(toIndentedString(logs)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }


}

