package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CrontabParam {
    @ApiModelProperty(value = "周期")
    String cycle;
    @ApiModelProperty(value = "指定范围")
    List<String> range = new ArrayList<>();
    @ApiModelProperty(value = "间隔")
    Integer interval;
    @ApiModelProperty(value = "开日时间")
    String startTime;
    @ApiModelProperty(value = "结束时间")
    String endTime;
    @ApiModelProperty(value = "指定时间")
    String fixedTime;
    @ApiModelProperty(value = "是否转为utc0")
    Boolean convertUtc0;
    @ApiModelProperty(value = "高级设置")
    Boolean advancedSetting;
}
