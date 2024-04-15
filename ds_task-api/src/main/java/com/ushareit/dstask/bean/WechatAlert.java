package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Table(name = "wechat_alert")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("微信群报警信息")
public class WechatAlert extends OperatorEntity {

    @ApiModelProperty(value = "群名称")
    @Column(name = "name")
    private String name;

    @ApiModelProperty(value = "token")
    @Column(name = "token")
    private String token;

    @Column(name = "user_group_id")
    private Integer userGroupId;

    @Column(name = "user_group_name")
    private String userGroupName;

    @ApiModelProperty(value = "删除状态：0未删除，1删除")
    @Column(name = "delete_status")
    private Integer deleteStatus;

    @Transient
    private String startTime;

    @Transient
    private String endTime;
}
