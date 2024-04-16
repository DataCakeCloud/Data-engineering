package com.ushareit.dstask.bean;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("离线任务节点实例详情")
public class TaskInstanceNode extends BaseEntity {
    @ApiModelProperty(value = "任务或数据集名称")
    private String dagId;

    @ApiModelProperty(value = "实例日期")
    private String executionDate;

    @ApiModelProperty(value = "节点owner")
    private String owner;

    @ApiModelProperty(value = "检查路径")
    private String checkPath;

    @ApiModelProperty(value = "是否外部数据")
    private boolean isExternal;

    @ApiModelProperty(value = "数据集id")
    private String metadataId;

    @ApiModelProperty(value = "此节点的直接下游名称")
    private String downDagId;

    @ApiModelProperty(value = "实例成功日期")
    private String successDate;

    @ApiModelProperty(value = "实例就绪")
    private boolean ready;

    @ApiModelProperty(value = "是否缩略")
    private boolean recursion;

    @ApiModelProperty(value = "节点id")
    private String nodeId;

    @ApiModelProperty(value = "开始时间")
    private String start_date;

    @ApiModelProperty(value = "结束时间")
    private String end_date;

    @ApiModelProperty(value = "任务Id")
    private Integer taskId;

    @ApiModelProperty(value = "任务状态")
    private String state;

    @ApiModelProperty(value = "任务是否上线")
    private boolean online;
}