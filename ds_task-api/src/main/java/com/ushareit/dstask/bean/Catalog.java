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

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "catalog")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("目录树类")
public class Catalog extends DeleteEntity {
    @ApiModelProperty("组件编码")
    @Column(name = "component_code")
    private String componentCode;

    @ApiModelProperty("父节点ID")
    @Column(name = "parent_id")
    private Integer parentId;

    @ApiModelProperty("对应值")
    @Column(name = "value")
    private String value;
}
