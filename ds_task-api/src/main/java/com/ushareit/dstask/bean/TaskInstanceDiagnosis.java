package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("任务实例检测上游信息")
public class TaskInstanceDiagnosis extends BaseEntity {
    @ApiModelProperty(value = "节点列表")
    private List<TaskInstanceNode> instance;

    @ApiModelProperty(value = "节点之间的依赖关系列表")
    private List<TaskInstanceRelation> relation;

    @ApiModelProperty(value = "终节点")
    private String coreTaskId;
}