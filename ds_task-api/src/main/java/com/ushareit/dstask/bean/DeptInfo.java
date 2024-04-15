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
import javax.persistence.Transient;
import java.sql.Timestamp;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("部门信息")
public class DeptInfo extends BaseEntity {

    @ApiModelProperty(value = "部门名称")
    @Column(name = "organization_name")
    private String organizationName;

    @ApiModelProperty(value = "成本统计部门名称")
    @Column(name = "cost_name")
    private String costName;

    @ApiModelProperty(value = "是否是成本统计有效部门")
    @Column(name = "is_effective_cost")
    private Integer isEffectiveCost;

    @ApiModelProperty(value = "父部门ID")
    @Column(name = "parent_id")
    private Integer parentId;

    @Transient
    @ApiModelProperty(value = "部门ID")
    private String organizationId;

    @Transient
    @ApiModelProperty(value = "部门主管ID")
    private String managerShareId;

    @Transient
    @ApiModelProperty(value = "部门全路径")
    private String organizationPath;

    @Transient
    @ApiModelProperty(value = "是否是主部门")
    private String isMain;

    @Transient
    @ApiModelProperty(value = "部门主管名称")
    private String managerName;

    @Transient
    @ApiModelProperty(value = "是否是父部门")
    private String isParent;

    @Transient
    @ApiModelProperty(value = "是否默认部门")
    private Integer isDefault = 0;

    @Column(name = "create_by")
    private String createBy;
    @Column(name = "update_by")
    private String updateBy;
    @Column(name = "create_time")
    private Timestamp createTime;
    @Column(name = "update_time")
    private Timestamp updateTime;

    @Transient
    private Integer hierarchy = 1;
    @Transient
    public boolean isHasChildrenDir = false;


    public static DeptInfo conversion(AccessGroup accessGroup) {
        return DeptInfo.builder().parentId(accessGroup.getParentId())
                .organizationId(accessGroup.getId().toString())
                .organizationName(accessGroup.getName())
                .createBy(accessGroup.getCreateBy())
                .createTime(accessGroup.getCreateTime()).build();
    }
}