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
@ApiModel("离线任务节点依赖关系")
public class TaskInstanceRelation {
    @ApiModelProperty(value = "上游节点")
    private String source;

    @ApiModelProperty(value = "下游节点")
    private String target;

    @ApiModelProperty(value = "是否为省略")
    private Boolean type;
}
