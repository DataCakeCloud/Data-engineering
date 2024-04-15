package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @author wuyan
 * @date 2022/4/7
 */
@Data
@Entity
@Builder
@Table(name = "access_user_group")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("用户与组映射类")
public class AccessUserGroup extends OperatorEntity{
    @NotBlank(message = "用户id")
    @ApiModelProperty(value = "用户id")
    @Column(name = "user_id")
    private Integer userId;


    @NotBlank(message = "组id")
    @ApiModelProperty(value = "组id")
    @Column(name = "group_id")
    private Integer groupId;
}