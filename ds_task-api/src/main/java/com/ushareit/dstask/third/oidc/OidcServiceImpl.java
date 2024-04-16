package com.ushareit.dstask.third.oidc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/9/19
 */
@Slf4j
@Service
public class OidcServiceImpl implements OidcService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oidc.url}")
    private String oidcUrl;

    @Value("${oidc.username}")
    private String username;

    @Value("${oidc.password}")
    private String password;

    @Override
    public String getToken(String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("scope", "openid groups");
        map.add("client_id", clientId);
        map.add("grant_type", "password");
        map.add("password", password);
        map.add("username", username);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(oidcUrl, request, String.class);

        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            log.error("request for token error: {}", response.getBody());
            throw new RuntimeException("get oidc for scmp error");
        }

        Map<String, Object> result = JSONObject.parseObject(response.getBody(),
                new TypeReference<Map<String, Object>>() {
                });

        log.info("result for clientId {} is {}", clientId, result);
        return result == null ? null : (String) result.get("id_token");
    }

}
