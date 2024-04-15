package com.ushareit.dstask.constant;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.net.InetAddress;

/**
 * @author wuyan
 * @date 2018/11/14
 */
@Slf4j
public class CommonConstant {

    /**
     * 通过OPENAPI认证的请求path
     */
    public static final String[] OIDC_INTERCEPT_PATHS = {"/oidc/**"};

    /**
     * 通过 OpenApi 认证的请求，携带的 header
     */
    public static final Header[] OIDC_HEADERS = {new BasicHeader("OidcKey", "OpenApi")};

    /**
     * 忽略拦截或过滤的请求path
     */
    public static final String[] IGNORE_INTERCEPT_PATHS = {"/", "/csrf", "/webjars/**", "/static/**", "/error/**", "/logout", "/code", "/login",
            "/inf-druid/**", "/druid/**", "/index", "/index.html", "/version", "/favicon.ico", "/cost/excel", "/doc.html","/swagger-resources","/swagger-resources/*", "/v2/api-docs", "/systemuserinfo/expand/remote", "/systemlogout/remote", "/systemmenu/remote", "/artifactversion/download*", "/editor.worker.js",
            "/task/statushook", "/task/flinkstatushook", "/attachment/download*", "/task/backfill/process", "/ds/switch/newaccess", "/task/batchCreateTask", "/ds/check/newaccess", "/ds/expand/remote", "/ds/menu/remote", "/ds/userinfo/hook", "/task/realtimeExecute",
            "/feedback/acceptByDingding", "/feedback/assignByDingding", "/pipeline/**", "/sla-service/**",
            "/accessuser/login", "/websocket/**", "/group/user/insertUser", "/accessuser/checkMFACode", "/accessuser/unbundlingMFA",
            "/accessuser/updatePassword", "/accessuser/checkCode", "/accessuser/sendCode","/tenant/list","/task/list", "/tenant/aksk/userInfo","/cloud-market/**","/task/instanceLog"};
    public static final String[] IGNORE_DB_SHARD = {"access_tenant", "announcement", "account",
            "duty_info", "feedback", "feedback_process_item", "attachment","advice", "sys_dict", "aksk", "aksk_token","template_region_imp","lock_info", "operate_log"};

    /**
     * 忽略的CONTENT_TYPE
     **/
    public static final String[] IGNORE_CONTENT = {};

    /**
     * 忽略拦截或过滤的请求方法
     **/
    public static final String[] IGNORE_METHOD = {"HEAD", "OPTIONS", "TRACE", "CONNECT"};

    /**
     * 忽略拦截或过滤的请求path
     */
    public static final String[] IGNORE_RECORD_PATHS = {"/operate/**", "/oidc/log/**"};

    public static final String CURRENT_LOGIN_USER = "current_login_user";
    public static final String WEBSOCKET_CHAT_ID = "chatId";
    public static final String USER_FOR_GRPC = CURRENT_LOGIN_USER + "-bin";

    public static final String AUTHENTICATION_HEADER = "Authentication";
    public static final String UUID = "Uuid";
    public static final String DATACAKE_TOKEN = "datacake_token";
    public static final String TRACEID = "traceId";
    public static final String CURRENTGROUP = "currentGroup";
    public static final String DINGTALK_TOKEN_HEADER = "Token";
    public static final String DINGTALK_ACCESS_PATH = "/access";
    public static final String DINGTALK_ACCESS_METHOD = "POST";

    public static final String SELECT_TABLE = "SELECT_TABLE";
    public static final String INSERT_TABLE = "INSERT_TABLE";
    public static final String CREATE_TABLE = "CREATE_TABLE";
    public static final String CATALOG = "shareit_";
    public static final String OP_TYPE = "WRITE;READ";
    public static final Integer TABLE_LIMIT = 100000;
    public static final Integer CATALOG_ROW = 50;

    public static final String URI_PREFIX = "/api-gateway";

    public static final String AUTH_EXCEPTION = "auth_exception";

    public static final String AUTH_WEBSOCKET_HEADER = "Sec-WebSocket-Protocol";
    /**
     * 管理员
     */
    public static final Integer ADMIN = 2;

    public static final String TENANT_NAME_KEY = "tenantName";


    public static String HOSTNAME;

    /**
     * shareit 超级管理租户
     */
    public static String INSIDE_SUPPER_TENANT_NAME = "ninebot";

    public static String SHAREIT_TENANT_NAME = "shareit";

    static {
        if (StringUtils.isNotBlank(System.getenv("HOSTNAME"))) {
            HOSTNAME = System.getenv("HOSTNAME");
        } else {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                HOSTNAME = localHost.getHostName();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                HOSTNAME = SymbolEnum.DASH.getSymbol();
            }
        }

    }
}
