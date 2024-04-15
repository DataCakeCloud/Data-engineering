package com.ushareit.dstask.configuration;

import javax.annotation.Resource;

import com.ushareit.dstask.filter.AuthIdentityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ushareit.dstask.filter.AuthAccessFilter;
import com.ushareit.dstask.filter.AuthFilterExceptionFilter;
import com.ushareit.dstask.filter.AbstractAuthFilter;

/**
 * 配置类
 *
 * @author wuyan
 * @date 2018/10/25
 **/
@Configuration
public class AuthWebMvcConfig implements WebMvcConfigurer {


    @Resource
    AuthAccessFilter authAccessFilter;
    @Resource
    AuthIdentityFilter authIdentityFilter;
    @Resource
    AuthFilterExceptionFilter authFilterExceptionFilter;

    @Bean
    public FilterRegistrationBean<AuthFilterExceptionFilter> authFilter() {
        final FilterRegistrationBean<AuthFilterExceptionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authFilterExceptionFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthAccessFilter> accessFilter() {
        final FilterRegistrationBean<AuthAccessFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authAccessFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 200);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AbstractAuthFilter> identityFilter() {
        final FilterRegistrationBean<AbstractAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authIdentityFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return registrationBean;
    }

}
