package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.util.JsonFormat;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.entity.UserInfoOuterClass;
import com.ushareit.dstask.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;

/**
 * @author wuyan
 * @date 2019/8/7
 **/
@Slf4j
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CurrentUser {
    private Integer id;
    private String userId;
    private String userName;

    /**
     * 部门
     */
    @JSONField(name = "org", alternateNames = "groupName")
    private String groupName;

    @JSONField(name = "group")
    private String group;


    private String name;

    private String email;

    private String password;

    private Integer tenantId = 1;

    private String tenantName = CommonConstant.SHAREIT_TENANT_NAME;

    private String token;

    private String groupIds;

    private String roles;

    private boolean admin;

    private boolean supperAdmin;

    private String phone;
    private String[] alarmChannel;
    private String[] userGroup;


    public CurrentUser(Integer id, String userId, String userName, String groupName, String group) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.groupName = groupName;
        this.group = group;
    }

    public static CurrentUser parse(String userBase64Str) {
        if (StringUtils.isBlank(userBase64Str)) {
            return null;
        }

        try {
            byte[] userBytes = BaseEncoding.base64().decode(userBase64Str);
            UserInfoOuterClass.UserInfo.Builder builder = UserInfoOuterClass.UserInfo.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(new String(userBytes), builder);
            UserInfoOuterClass.UserInfo userInfo = builder.build();
            CurrentUser user = new CurrentUser();
            BeanUtils.copyProperties(userInfo, user);
            return user;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public UserInfoOuterClass.UserInfo toUserInfo() {
        return UserInfoOuterClass.UserInfo.newBuilder()
                .setId(ObjectUtils.defaultIfNull(this.id, NumberUtils.INTEGER_ZERO))
                .setUserId(StringUtils.defaultIfEmpty(this.userId, StringUtils.EMPTY))
                .setUserName(StringUtils.defaultIfEmpty(this.userName, StringUtils.EMPTY))
                .setGroupName(StringUtils.defaultIfEmpty(this.groupName, StringUtils.EMPTY))
                .setGroup(StringUtils.defaultIfEmpty(this.group, StringUtils.EMPTY))
                .setName(StringUtils.defaultIfEmpty(this.name, StringUtils.EMPTY))
                .setEmail(StringUtils.defaultIfEmpty(this.email, StringUtils.EMPTY))
                .setPassword(StringUtils.defaultIfEmpty(this.password, StringUtils.EMPTY))
                .setTenantId(ObjectUtils.defaultIfNull(tenantId, NumberUtils.INTEGER_ONE))
                .setToken(StringUtils.defaultIfEmpty(this.token, StringUtils.EMPTY))
                .setGroupIds(StringUtils.defaultIfEmpty(this.groupIds, StringUtils.EMPTY))
                .build();
    }

    public static void main(String[] args) {
        String a="{\"isSupperAdmin\":true,\"tenantName\":\"shareit\",\"org\":\"BDP\",\"roles\":\"admin,common,menu_test,test_lys\",\"groupIds\":\"402539,313273,313251,900605025,900605560,900605557\",\"tenantId\":1,\"id\":267,\"isAdmin\":true,\"userName\":\"hanzenggui\",\"userId\":\"hanzenggui\",\"email\":\"hanzenggui@ushareit.com\",\"group\":\"#N/A\"}";
        CurrentUser currentUser= GsonUtil.parse(a,CurrentUser.class);
        System.out.println(currentUser);
    }
}

