package com.ushareit.dstask.filter;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.*;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.AuthResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.AuthException;
import com.ushareit.dstask.service.third.OIDCIdentityService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.TokenUtil;
import com.ushareit.dstask.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 过滤器记录请求日志
 *
 * @author wuyan
 * @date 2018/10/26
 **/
@Slf4j
@Component
public class AuthIdentityFilter extends AbstractAuthFilter {
    private static final Cache<String, CurrentUser> USE_MAP = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .initialCapacity(110)
            .maximumSize(200)
            .recordStats()
            .removalListener(new RemovalListener<String, CurrentUser>() {
                @Override
                public void onRemoval(RemovalNotification<String, CurrentUser> notification) {
                    if (notification.getCause() != RemovalCause.REPLACED) {
                        log.info(notification.getKey() + " was removed, cause is " + notification.getCause());
                    }
                }
            })
            .build();

    @Value("${remote.identity.verify.url}")
    private String identityUrl;

    @Value("${ds.login.intercept.dsactive}")
    private boolean dsActive;

    @Value("${ds.cloud.intercept.dsactive}")
    private boolean cloudDsActive;

    @Value("${datacake.external-role:true}")
    public Boolean dcRole;

    @Resource
    private OIDCIdentityService oidcIdentityService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {

        if (cloudDsActive || dcRole) {
            InfTraceContextHolder.get().setIsPrivate(true);
        }

//        log.info("request path  is :" + request.getServletPath());

        // oidc 请求
        boolean oidcMatch = WebUtils.requestAnyIsMatch(request, null, null, CommonConstant.OIDC_INTERCEPT_PATHS,
                CommonConstant.OIDC_HEADERS);

        if (oidcMatch) {
            String token = getToken(request);
            boolean isValid = oidcIdentityService.validate(token);
            if (!isValid) {
                request.setAttribute(CommonConstant.AUTH_EXCEPTION, AuthResponseCodeEnum.AUTH_ERROR);
                throw new AuthException(AuthResponseCodeEnum.AUTH_ERROR);
            }
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER,
                    CurrentUser.builder().id(1).userId("oidc").userName("oidc").groupName("bdp").group("bdp")
                            .tenantName("shareit").tenantId(1).build());
            InfTraceContextHolder.get().setAdmin(true);
            chain.doFilter(request, response);
            return;
        }

        if ( !this.dsActive) {
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER,
                    CurrentUser.builder().id(1).userId("admin").userName("admin").groupName("cbs").group("DW").build());
            USE_MAP.put(CommonConstant.CURRENT_LOGIN_USER,
                    CurrentUser.builder().id(1).userId("admin").userName("admin").groupName("cbs").group("DW").build());
            chain.doFilter(request, response);
            return;
        }

        //TODO 服务网关转发情况需优化
        if (request.getHeader(CommonConstant.CURRENT_LOGIN_USER) != null){
            CurrentUser currentUser =
                    JSONObject.parseObject(request.getHeader(CommonConstant.CURRENT_LOGIN_USER), CurrentUser.class);
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER, currentUser);
            String authentication = request.getHeader(CommonConstant.AUTHENTICATION_HEADER);
            if (currentUser == null) {
                log.info(" current user is :" + request.getHeader(CommonConstant.CURRENT_LOGIN_USER));
                chain.doFilter(request, response);
                return;
            }
            if (StringUtils.isNotEmpty(authentication)) {
                InfTraceContextHolder.get().setAuthentication(authentication);
                if (USE_MAP.getIfPresent(authentication) == null) {
                    USE_MAP.put(authentication, currentUser);
                }
            }
            chain.doFilter(request, response);
            return;
        }

        if (request.getHeader(CommonConstant.DATACAKE_TOKEN) != null) {
            chain.doFilter(request, response);
            return;
        }

        boolean match = WebUtils.requestAnyIsMatch(request, CommonConstant.IGNORE_METHOD,
                CommonConstant.IGNORE_CONTENT, CommonConstant.IGNORE_INTERCEPT_PATHS);
        if (match) {
            String tenantName = StringUtils.defaultIfBlank(request.getHeader("tenantName"), CommonConstant.SHAREIT_TENANT_NAME);
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER, CurrentUser.builder().id(1).userId("white")
                    .userName("white").groupName("bdp").group("bdp").tenantId(1).tenantName(tenantName).build());

            chain.doFilter(request, response);
            return;
        }

        String authentication = request.getHeader(CommonConstant.AUTHENTICATION_HEADER);

        // TODO add code
        CurrentUser oldUser = USE_MAP.getIfPresent(authentication);
        if (oldUser != null) {
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER, oldUser);
            chain.doFilter(request, response);
            return;
        }

        //如果是ds的认证全部放行 由getway
        if (dsActive) {
            authentication = request.getHeader(CommonConstant.AUTHENTICATION_HEADER);
            try {
                CurrentUser currentUser = TokenUtil.getInfoFromToken(authentication);
                request.setAttribute(CommonConstant.CURRENT_LOGIN_USER, currentUser);
                USE_MAP.put(authentication, currentUser);
            } catch (Exception e) {
                log.error("ds token certification fail");
                throw new AuthException(AuthResponseCodeEnum.AUTH_ERROR);
            }
        }

        chain.doFilter(request, response);
    }

    String getToken(HttpServletRequest request) {
        log.info("oidc check, uri: {}, host:{}", request.getRequestURI(), WebUtils.getClientIpAddress(request));
        // do openapi identity
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization) || !authorization.contains(StringUtils.SPACE)) {
            log.error("request header: Authorization is empty or invalid");
            return null;
        }

        return authorization.split(StringUtils.SPACE)[1];
    }

    public static CurrentUser getCurrentUser(String authentication) {
        if (USE_MAP.getIfPresent(CommonConstant.CURRENT_LOGIN_USER) != null) {
            return USE_MAP.getIfPresent(CommonConstant.CURRENT_LOGIN_USER);
        }
        if (USE_MAP.getIfPresent(authentication) != null) {
            return USE_MAP.getIfPresent(authentication);
        }
        return TokenUtil.getInfoFromToken(authentication);
    }

}
