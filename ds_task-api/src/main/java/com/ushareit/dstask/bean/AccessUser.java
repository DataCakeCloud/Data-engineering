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
@Table(name = "access_user")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("用户类")
public class AccessUser extends DeleteEntity {
    //    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;

    @ApiModelProperty(value = "用户邮箱")
    @Column(name = "email")
    private String email;

    @ApiModelProperty(value = "用户密码")
    @Column(name = "password")
    private String password;

    @ApiModelProperty(value = "冻结状态")
    @Column(name = "freeze_status")
    private Integer freezeStatus;

    @ApiModelProperty(value = "企业id")
    @Column(name = "company_id")
    private String companyId;

    @ApiModelProperty(value = "租户id")
    @Column(name = "tenant_id")
    private Integer tenantId;

    @ApiModelProperty(value = "企业部门")
    @Column(name = "tenancy_code")
    private String tenancyCode;

    @ApiModelProperty(value = "org")
    private String org;

    @ApiModelProperty(value = "来源")
    private String source;

    @ApiModelProperty(value = "最新的验证码")
    private String latestCode;

    @ApiModelProperty(value = "用户所属角色")
    @Transient
    private List<String> roles;
    /**
     * 所属用户组
     */
    @Transient
    private List<String> groups;

    @ApiModelProperty(value = "用户手机号")
    @Column(name = "phone")
    private String phone;

    @ApiModelProperty(value = "企业微信id")
    @Column(name = "we_chat_id")
    private String weChatId;

    private String eName;//用户名

    @Transient
    private List<Integer> roleIds;

    @Transient
    private String token;

    @Transient
    private String tenantName;

    private String mfaSecret;

    @Transient
    private String codeInput;

    private String isBindmfa;

    @Transient
    private String qrCodeLink;

    @Transient
    private String isHasCloudResource = "1";

    @Transient
    private String userRoleIds;

    @Transient
    private String role = "common";

    @Transient
    private Integer userId;

    @Transient
    private Boolean isMFA = true;

    @Transient
    private String loginMode;
}