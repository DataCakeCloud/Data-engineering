package com.ushareit.dstask.third.airbyte.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2022-06-23T15:55:19.222177+08:00[Asia/Shanghai]")
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class LogRead {
  
  private @Valid List<String> logLines = new ArrayList<>();

  /**
   **/
  public LogRead logLines(List<String> logLines) {
    this.logLines = logLines;
    return this;
  }

  

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("logLines")
  @NotNull
  public List<String> getLogLines() {
    return logLines;
  }

  public void setLogLines(List<String> logLines) {
    this.logLines = logLines;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LogRead logRead = (LogRead) o;
    return Objects.equals(this.logLines, logRead.logLines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logLines);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LogRead {\n");
    
    sb.append("    logLines: ").append(toIndentedString(logLines)).append("\n");
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

