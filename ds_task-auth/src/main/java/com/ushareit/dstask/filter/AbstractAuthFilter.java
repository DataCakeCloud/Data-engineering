package com.ushareit.dstask.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 过滤器记录请求日志
 *
 * @author wuyan
 * @date 2018/10/26
 **/
public abstract class AbstractAuthFilter extends OncePerRequestFilter {

    @Value("${system.code}")
    public String systemCode;
}
