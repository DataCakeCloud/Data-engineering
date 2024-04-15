package com.ushareit.dstask.trace.holder;

import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.CommonConstant;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuyan
 * @date 2020/05/11
 */
@Data
@Accessors(chain = true)
public class InfWebContext {
    private String traceId;

    private Integer applicationId;
    private String userName = "system";
    private String oldCode;
    private String newCode;
    private Date startTime;
    private String clientIp;
    private String requestPath;
    private String requestBody;
    private String env;
    private Boolean admin;
    private String sessionId;
    private String authentication;

    private Integer tenantId = 1;
    private String tenantName = "ninebot";
    private Boolean isPrivate = false;
    private String groupIds;
    private Integer userId;
    private String uuid;

    private Map<String, Object> map = new HashMap<>();
    private Object paramInfo;
    private CurrentUser userInfo;
    private String currentGroup;


    private String groupId;

    public String getEnv() {
        if (StringUtils.isEmpty(env)) {
            return "test";
        }
        return env.split(",")[1].trim();
    }

    public boolean getIsPrivate() {
//        if (StringUtils.isNotEmpty(tenantName) && (tenantName.equals(CommonConstant.INSIDE_SUPPER_TENANT_NAME)
//                || tenantName.equals(CommonConstant.SHAREIT_TENANT_NAME))) {
//            return false;
//        }
        return true;
    }

}
