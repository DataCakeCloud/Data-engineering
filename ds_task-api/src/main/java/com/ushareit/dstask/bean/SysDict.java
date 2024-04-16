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
@Table(name = "sys_dict")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("系统字典类")
public class SysDict extends DataEntity {
    @ApiModelProperty("唯一编码")
    @Column(name = "code")
    private String code;

    @ApiModelProperty("对应值")
    @Column(name = "value")
    private String value;

    @ApiModelProperty("父编码")
    @Column(name = "parent_code")
    private String parentCode;

    @ApiModelProperty(value = "状态", dataType = "int", example = "0")
    private Integer status;

    @ApiModelProperty("请求模块")
    @Column(name = "source")
    private String source;
}
