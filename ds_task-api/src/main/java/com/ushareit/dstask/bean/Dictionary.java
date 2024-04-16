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
@Table(name = "dictionary")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("字典类")
public class Dictionary extends DeleteEntity {
    @ApiModelProperty("组件编码")
    @Column(name = "component_code")
    private String componentCode;

    @ApiModelProperty("中文名称")
    @Column(name = "chinese_name")
    private String chineseName;

    @ApiModelProperty("英文名称")
    @Column(name = "english_name")
    private String englishName;
}
