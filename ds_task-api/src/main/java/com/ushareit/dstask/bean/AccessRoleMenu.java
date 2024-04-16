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
@Table(name = "access_role_menu")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("角色与菜单映射类")
public class AccessRoleMenu extends OperatorEntity{
    @NotBlank(message = "角色id")
    @ApiModelProperty(value = "角色id")
    @Column(name = "role_id")
    private Integer roleId;


    @NotBlank(message = "菜单id")
    @ApiModelProperty(value = "菜单id")
    @Column(name = "menu_id")
    private Integer menuId;
}