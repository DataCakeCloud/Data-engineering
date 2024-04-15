package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class EventTrigger {

    @ApiModelProperty(value = "任务id")
    private Integer taskId;

    @ApiModelProperty(value = "回调url")
    private String callbackUrl;

    @ApiModelProperty(value = "模板参数")
    private Map<String,String> templateParams;
}
