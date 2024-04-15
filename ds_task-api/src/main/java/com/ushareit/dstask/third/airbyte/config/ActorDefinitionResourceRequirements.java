package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * actor definition specific resource requirements. if default is set, these are the requirements that should be set for ALL jobs run for this actor definition. it is overriden by the job type specific configurations. if not set, the platform will use defaults. these values will be overriden by configuration at the connection level.
 **/
@ApiModel(description = "actor definition specific resource requirements. if default is set, these are the requirements that should be set for ALL jobs run for this actor definition. it is overriden by the job type specific configurations. if not set, the platform will use defaults. these values will be overriden by configuration at the connection level.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2022-06-23T15:55:19.222177+08:00[Asia/Shanghai]")
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class ActorDefinitionResourceRequirements {
  
  private @Valid ResourceRequirements _default;
  private @Valid List<JobTypeResourceLimit> jobSpecific = new ArrayList<>();

  /**
   **/
  public ActorDefinitionResourceRequirements _default(ResourceRequirements _default) {
    this._default = _default;
    return this;
  }

  

  
  @ApiModelProperty(value = "")
  @JsonProperty("default")
  public ResourceRequirements getDefault() {
    return _default;
  }

  public void setDefault(ResourceRequirements _default) {
    this._default = _default;
  }

/**
   **/
  public ActorDefinitionResourceRequirements jobSpecific(List<JobTypeResourceLimit> jobSpecific) {
    this.jobSpecific = jobSpecific;
    return this;
  }

  

  
  @ApiModelProperty(value = "")
  @JsonProperty("jobSpecific")
  public List<JobTypeResourceLimit> getJobSpecific() {
    return jobSpecific;
  }

  public void setJobSpecific(List<JobTypeResourceLimit> jobSpecific) {
    this.jobSpecific = jobSpecific;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorDefinitionResourceRequirements actorDefinitionResourceRequirements = (ActorDefinitionResourceRequirements) o;
    return Objects.equals(this._default, actorDefinitionResourceRequirements._default) &&
        Objects.equals(this.jobSpecific, actorDefinitionResourceRequirements.jobSpecific);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_default, jobSpecific);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorDefinitionResourceRequirements {\n");
    
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
    sb.append("    jobSpecific: ").append(toIndentedString(jobSpecific)).append("\n");
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

