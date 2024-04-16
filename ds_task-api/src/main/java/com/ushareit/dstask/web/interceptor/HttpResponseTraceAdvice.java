package com.ushareit.dstask.web.interceptor;

import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author wuyan
 * @date 2018/11/14
 **/
@Slf4j
@ControllerAdvice
public class HttpResponseTraceAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public Object beforeBodyWrite(Object object,
                                  @NonNull MethodParameter methodParameter,
                                  @NonNull MediaType mediaType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> clas,
                                  @NonNull ServerHttpRequest serverHttpRequest,
                                  @NonNull ServerHttpResponse serverHttpResponse) {

        InfTraceContextHolder.remove();
        MDC.clear();

        return object;
    }

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Class clas) {
        return true;
    }
}

