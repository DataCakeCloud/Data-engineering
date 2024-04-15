package com.ushareit.dstask.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.AccessRole;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.RequestWrapper;
import com.ushareit.dstask.utils.WebUtils;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.ushareit.dstask.constant.CommonConstant.INSIDE_SUPPER_TENANT_NAME;

/**
 * 过滤器记录请求日志
 *
 * @author wuyan
 * @date 2018/10/26
 **/
@Slf4j
@Component
public class HttpRequestTraceFilter extends OncePerRequestFilter {
    @Value("${spring.profiles.active}")
    private String active;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Resource
    private AccessRoleService accessRoleService;

    @Resource
    private AccessRoleMenuService accessRoleMenuService;

    @Resource
    private AccessMenuService accessMenuService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {

        InfTraceContextHolder.get().setStartTime(new Date());
        String authentication = request.getHeader(CommonConstant.AUTHENTICATION_HEADER);
        String uuid=request.getHeader(CommonConstant.UUID);
        InfTraceContextHolder.get().setAuthentication(authentication);
        InfTraceContextHolder.get().setEnv(this.active);

        log.debug("request url is {}", request.getRequestURI());

        CurrentUser currentUser = (CurrentUser) request.getAttribute(CommonConstant.CURRENT_LOGIN_USER);

        if (currentUser == null && request.getHeader(CommonConstant.CURRENT_LOGIN_USER) != null) {
            currentUser = JSONObject.parseObject(request.getHeader(CommonConstant.CURRENT_LOGIN_USER), CurrentUser.class);
            request.setAttribute(CommonConstant.CURRENT_LOGIN_USER, currentUser);
        }

        if (currentUser != null) {
            // 有权限，继续往下走
            String traceId = request.getHeader(CommonConstant.TRACEID);
            if (StringUtils.isBlank(traceId)) {
                traceId = UuidUtil.getUuid32();
            }
            if(request.getHeader("groupId") != null){
                InfTraceContextHolder.get().setGroupId(request.getHeader("groupId"));
            }
            String currentGroup = request.getHeader(CommonConstant.CURRENTGROUP);
            if(currentGroup!= null && !currentGroup.isEmpty()){
                InfTraceContextHolder.get().setUserInfo(currentUser);
            }

            InfTraceContextHolder.get().setUserInfo(currentUser);
            InfTraceContextHolder.get().setUserName(currentUser.getUserId());
            InfTraceContextHolder.get().setCurrentGroup(currentGroup);
            InfTraceContextHolder.get().setNewCode(currentUser.getGroupName());
            InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
            //先加shareit租户判断 之后做成租户功能控制
            if (!DataCakeConfigUtil.getDataCakeConfig().getDcRole() && StringUtils.isNotEmpty(currentUser.getTenantName()) && (currentUser.getTenantName().equals(INSIDE_SUPPER_TENANT_NAME)
                    || currentUser.getTenantName().equals(DsTaskConstant.SHAREIT_TENANT_NAME))) {
                InfTraceContextHolder.get().setIsPrivate(false);
            }
            InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());
            InfTraceContextHolder.get().setTraceId(traceId);

            // put slf4j mdc
            MDC.put(DsTaskConstant.LOG_TRACE_ID, traceId);
            MDC.put(DsTaskConstant.LOG_TENANT_NAME, currentUser.getTenantName());
        }
        log.debug("current user is {}", currentUser);
        InfTraceContextHolder.get().setAdmin(currentUser.isAdmin());
        InfTraceContextHolder.get().setUserName(currentUser.getUserId());
        InfTraceContextHolder.get().setUserId(currentUser.getId());
        InfTraceContextHolder.get().setNewCode(currentUser.getGroupName());
        InfTraceContextHolder.get().setUuid(uuid);

        boolean match = WebUtils.methodIsMatch(request, CommonConstant.IGNORE_METHOD)
                || WebUtils.pathIsMatch(request, CommonConstant.IGNORE_INTERCEPT_PATHS)
                || WebUtils.contentIsMatch(request, CommonConstant.IGNORE_CONTENT)
                || WebUtils.pathIsMatch(request, CommonConstant.OIDC_INTERCEPT_PATHS)
                || WebUtils.headerIsMatch(request, CommonConstant.OIDC_HEADERS);

        if (match) {
            chain.doFilter(request, response);
            return;
        }

        String requestBody = "";
        ServletRequest servletRequest = request;
        match = WebUtils.contentIsMatch(request, DsTaskConstant.IGNORE_CONTENT);
        if (!match) {
            servletRequest = new RequestWrapper(request);
            requestBody = new String(((RequestWrapper) servletRequest).getBody());
        }

        //最终参数列表，包含请求路径参数和body中的参数
        Object paramInfo = getQueryInfo(request, requestBody);
        InfTraceContextHolder.get().setParamInfo(paramInfo);
        String requestPath = StringUtils.isBlank(request.getServletPath()) ? request.getRequestURI() : request.getServletPath();

        String queryPath = request.getQueryString();
        requestPath = requestPath + (queryPath == null ? "" : "?" + queryPath);


     /*   Boolean root = false;
        String roles = currentUser.getRoles();
        if (StringUtils.isNotEmpty(roles) && roles.contains("admin")) {
            root = true;
        }*/

//        InfTraceContextHolder.get().setAdmin(currentUser.isAdmin());

        InfTraceContextHolder.get().setRequestPath(requestPath)
                .setRequestBody(requestBody)
                .setSessionId(request.getSession().getId())
                .setAuthentication(authentication);

        chain.doFilter(servletRequest, response);
    }


    private Object getQueryInfo(HttpServletRequest request, String requestBody) {
        if (!StringUtil.isEmpty(requestBody)) {
            return JSON.parse(requestBody);
        }
        switch (request.getMethod()) {
            case DsTaskConstant.POST_METHOD:
                return getParameterQueryMap(request);
            default:
                return getUrlQueryMap(request);
        }
    }

    private Map<String, String> getUrlQueryMap(HttpServletRequest request) {
        String urlQueryString = request.getQueryString();
        Map<String, String> queryMap = new HashMap<>(8);
        String[] arrSplit;
        if (urlQueryString == null) {
            return queryMap;
        } else {
            //每个键值为一组
            arrSplit = urlQueryString.split("[&]");
            for (String strSplit : arrSplit) {
                String[] arrSplitEqual = strSplit.split("[=]");
                //解析出键值
                if (arrSplitEqual.length > 1) {
                    queryMap.put(arrSplitEqual[0], arrSplitEqual[1]);
                } else {
                    if (!"".equals(arrSplitEqual[0])) {
                        queryMap.put(arrSplitEqual[0], "");
                    }
                }
            }
        }
        return queryMap;
    }

    private Map<String, String> getParameterQueryMap(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, String> queryMap = new HashMap<>(8);
        while (enumeration.hasMoreElements()) {
            String paramName = enumeration.nextElement();
            String[] values = request.getParameterValues(paramName);
            if (values.length != 0) {
                queryMap.put(paramName, values[0]);
            } else {
                queryMap.put(paramName, "");
            }
        }
        return queryMap;
    }
}
