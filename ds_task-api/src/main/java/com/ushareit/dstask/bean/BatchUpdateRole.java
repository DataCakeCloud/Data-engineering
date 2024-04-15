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
@ApiModel("批量修改任务角色请求参数类")
public class BatchUpdateRole {
    @ApiModelProperty(value = "负责人")
    private String owner;

    @ApiModelProperty(value = "修改的任务名列表")
    private String[] taskNames;

    @ApiModelProperty(value = "修改的任务id列表")
    private String[] taskIds;

    @ApiModelProperty(value = "协作人")
    private String[] collaborators;
}
