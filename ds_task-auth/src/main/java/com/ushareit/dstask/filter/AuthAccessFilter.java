package com.ushareit.dstask.filter;

import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.AuthResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.AuthException;
import com.ushareit.dstask.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * 过滤器记录请求日志
 *
 * @author wuyan
 * @date 2018/10/26
 **/
@Slf4j
@Component
public class AuthAccessFilter extends AbstractAuthFilter {

    @Value("${remote.access.verify.url}")
    private String accessUrl;

    @Value("${ds.access.intercept.dsactive}")
    private boolean dsactive;

    @Value("${ds.access.check.url}")
    private String dsUrl;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();


        boolean match = WebUtils.methodIsMatch(request, CommonConstant.IGNORE_METHOD)
                || WebUtils.pathIsMatch(request, CommonConstant.IGNORE_INTERCEPT_PATHS)
                || WebUtils.pathIsMatch(request, CommonConstant.OIDC_INTERCEPT_PATHS)
                || WebUtils.headerIsMatch(request, CommonConstant.OIDC_HEADERS);

        if (match) {
            chain.doFilter(request, response);
            return;
        }

        if (this.dsactive) {
            // dsactive=true表示按新权限体系走，false表示按宙斯权限体系走， 放过这个接口，进入if。在Http拦截器做access验证
            // 用户接口
            String requestPath =
                    StringUtils.isBlank(request.getServletPath()) ? request.getRequestURI() : request.getServletPath();


            CurrentUser currentUser = (CurrentUser) request.getAttribute(CommonConstant.CURRENT_LOGIN_USER);
            String url = MessageFormat.format(dsUrl, requestPath);
//            String url = "http://ds-task-dev.ushareit.org/ds/check";


            String authentication = request.getHeader(CommonConstant.AUTHENTICATION_HEADER);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstant.AUTHENTICATION_HEADER, authentication);
            HttpEntity<String> requestEntity = new HttpEntity((Object) null, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Boolean> re = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Boolean.class);
            Boolean haveAccess = false;
            if (re.getStatusCodeValue() == 200) {
                haveAccess = re.getBody();
            }
            if (!haveAccess) {
                // 如果没有权限，就报错
                log.error("Current user " + currentUser.getUserId() + "do not have new access!");
                request.setAttribute(CommonConstant.AUTH_EXCEPTION, AuthResponseCodeEnum.NO_AUTH);
                throw new AuthException(AuthResponseCodeEnum.NO_AUTH);
            }
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

}
