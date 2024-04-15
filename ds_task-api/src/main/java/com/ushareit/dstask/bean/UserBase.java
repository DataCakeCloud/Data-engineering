package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("用户类")
public class UserBase extends BaseEntity {
    @ApiModelProperty(value = "员工工号")
    private String staffId;

    @ApiModelProperty(value = "shareId")
    private String shareId;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "钉钉id")
    private String dingTalkUid;

    @ApiModelProperty(value = "员工姓名")
    private String name;

    @ApiModelProperty(value = "部门名称")
    private String department;

    @ApiModelProperty(value = "部门全路径")
    private String deptFullPath;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "企业微信id")
    private String wechatId;

    public static UserBase conversion(AccessUser accessUser) {
        return UserBase.builder().name(accessUser.getName())
                .staffId(accessUser.getId().toString())
                .shareId(accessUser.getName())
                .email(accessUser.getEmail())
                .wechatId(accessUser.getWeChatId())
                .mobile(accessUser.getPhone()).build();
    }
}