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
@Table(name = "access_tenant_role")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("租户与角色映射类")
public class AccessTenantRole extends OperatorEntity{
    @NotBlank(message = "租户id")
    @ApiModelProperty(value = "租户id")
    @Column(name = "tenant_id")
    private Integer tenantId;


    @NotBlank(message = "角色id")
    @ApiModelProperty(value = "角色id")
    @Column(name = "role_id")
    private Integer roleId;
}
