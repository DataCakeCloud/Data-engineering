package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @author wuyan
 * @date 2022/4/6
 */
@Data
@Entity
@Builder
@Table(name = "access_product")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("产品类")
public class AccessProduct extends DeleteEntity{
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;
}