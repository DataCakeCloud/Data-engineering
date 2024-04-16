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
@Table(name = "dynamic_form")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("动态表单类")
public class DynamicForm extends DataEntity {
    @ApiModelProperty("组件编码")
    @Column(name = "component_code")
    private String componentCode;

    @ApiModelProperty("组件编码")
    @Column(name = "element_classify")
    private String elementClassify;

    @ApiModelProperty("参数关键字")
    @Column(name = "element_key")
    private String elementKey;

    @ApiModelProperty("参数类型")
    @Column(name = "element_type")
    private String elementType;

    @ApiModelProperty("参数约束正则表达式")
    private String pattern;

    @ApiModelProperty(value = "是否必须 ", dataType = "int", example = "0")
    @Column(name = "`option`")
    private Integer option;

    @ApiModelProperty("参数值")
    private String value;

    @ApiModelProperty("参数默认值")
    @Column(name = "default_value")
    private String defaultValue;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("父节点ID")
    @Column(name = "parent_id")
    private Integer parentId;

    @ApiModelProperty("序列")
    private Integer rank;
}
