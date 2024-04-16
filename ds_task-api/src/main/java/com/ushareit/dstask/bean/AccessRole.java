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
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/5/5
 */
@Data
@Entity
@Builder
@Table(name = "access_role")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("角色类")
public class AccessRole extends DeleteEntity{
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;

    @Transient
    private Integer userNum;

    @Transient
    private List<AccessUser> users;

    @Transient
    private List<AccessMenu> menus;

    @Transient
    private List<Integer> menuChecked;

    @Transient
    private List<Integer> actionChecked;
}