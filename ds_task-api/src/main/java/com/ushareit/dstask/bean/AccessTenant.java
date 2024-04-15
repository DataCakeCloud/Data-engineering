package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author wuyan
 * @date 2022/4/6
 */
@Data
@Entity
@Builder
@Table(name = "access_tenant")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("租户类")
public class AccessTenant extends DeleteEntity {

    @Length(max = 56, message = "租户名不能超过56个字符")
    @Pattern(regexp = "^[0-9a-zA-Z_]+", message = "租户名只能是数字字母下划线的组合")
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;

    @ApiModelProperty(value = "冻结状态")
    @Column(name = "freeze_status")
    private Integer freezeStatus;

    @Column(name = "manager_email")
    private String managerEmail;

    @Transient
    private String userRoleIds;

    @ApiModelProperty(value = "用户配置信息")
    @Column(name = "config")
    private String config;

    @Data
    public static class Config{

        @JSONField(name = "loginMode", alternateNames = "login_mode")
        public String loginMode;

        @JSONField(name = "loginAuthInterface", alternateNames = "login_auth_interface")
        public String loginAuthInterface;

        @JSONField(name = "interfaceDescribe", alternateNames = "interface_describe")
        public String interfaceDescribe;

        @JSONField(name = "isEnableMfa", alternateNames = "is_enable_mfa")
        public String isEnableMfa;

        @JSONField(name = "alarmChannel", alternateNames = "alarm_channel")
        public String alarmChannel;


    }

}