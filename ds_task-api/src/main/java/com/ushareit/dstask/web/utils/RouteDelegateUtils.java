package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/8/23
 */
@Slf4j
public class RouteDelegateUtils {

    public static ResponseEntity<String> redirect(HttpServletRequest request, HttpServletResponse response, String routeUrl, String prefix, String user) {
        try {
            // build up the redirect URL
            String redirectUrl = createRedirectUrl(request, routeUrl, prefix, user);
            RequestEntity<?> requestEntity = createRequestEntity(request, redirectUrl);
            return route(requestEntity);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR, BaseResponseCodeEnum.SYS_ERR.getMessage(), e.getMessage());
        }
    }

    private static String createRedirectUrl(HttpServletRequest request, String routeUrl, String prefix, String user) {
        String queryString = request.getQueryString();
        queryString = StringUtils.defaultIfBlank(queryString, StringUtils.EMPTY);
        return routeUrl + request.getRequestURI() + "?" + queryString + "&ds-user=" + user;
    }

    private static RequestEntity<?> createRequestEntity(HttpServletRequest request, String url) throws URISyntaxException, IOException {
        String method = request.getMethod();
        HttpMethod httpMethod = HttpMethod.resolve(method);
        MultiValueMap<String, String> headers = parseRequestHeader(request);
        byte[] body = parseRequestBody(request);
        return new RequestEntity<>(body, headers, httpMethod, new URI(url));
    }

    private static ResponseEntity<String> route(RequestEntity requestEntity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(requestEntity, String.class);
    }

    private static byte[] parseRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }

    private static MultiValueMap<String, String> parseRequestHeader(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }

}
