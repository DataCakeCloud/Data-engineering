package com.ushareit.dstask.bean;

import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TaskDatePreviewReq {
    @ApiModelProperty(value = "任务周期")
    String taskGra;
    @ApiModelProperty(value = "任务cron表达式")
    String taskCrontab;
    @ApiModelProperty(value = "数据依赖")
    Dataset dataDepend;
    @ApiModelProperty(value = "任务依赖")
    EventDepend taskDepend;
}
