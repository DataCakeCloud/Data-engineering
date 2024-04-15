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
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/6
 */
@Data
@Entity
@Builder
@Table(name = "access_menu")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("菜单类")
public class AccessMenu extends DeleteEntity{
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;

    @NotBlank(message = "编码不能为空")
    @ApiModelProperty(value = "编码")
    private String code;

    @ApiModelProperty(value = "层级")
    private Integer level;

    @ApiModelProperty(value = "有效或无效")
    private Integer valid;

    @ApiModelProperty(value = "父id")
    @Column(name = "parent_menu_id")
    private Integer parentMenuId;

    @ApiModelProperty(value = "所属产品id")
    @Column(name = "product_id")
    private Integer productId;

    @NotBlank(message = "链接不能为空")
    @ApiModelProperty(value = "链接")
    private String url;

    @Transient
    private List<AccessMenu> children;
}