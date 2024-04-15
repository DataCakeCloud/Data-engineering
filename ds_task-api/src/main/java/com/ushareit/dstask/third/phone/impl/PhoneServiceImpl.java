package com.ushareit.dstask.third.phone.impl;

import com.ushareit.dstask.configuration.DingDingConfig;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.third.dingding.vo.DingDingTextRequest;
import com.ushareit.dstask.third.phone.PhoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@Service
public class PhoneServiceImpl implements PhoneService {

    @Resource
    private DingDingConfig dingDingConfig;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void notify(List<String> phoneList, String message) {
        try {

            List<String> phoneMessage = new ArrayList<>();
            phoneMessage.add(message);
            DingDingTextRequest dingDingTextRequest = DingDingTextRequest.builder()
                    .template_params(phoneMessage).template_id("0bf9c563efa34331be75fd7e26b4cd75")
                    .receiver(phoneList).build();

            MultiValueMap<String, String> headers = getHeaders();
            if (Objects.isNull(headers) || phoneList.isEmpty()) {
                return;
            }

            HttpEntity<DingDingTextRequest> entity = new HttpEntity<>(dingDingTextRequest, headers);
            log.info("text:" + phoneList.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())));

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(dingDingConfig.getPhoneUrl(), HttpMethod.POST, entity, Map.class);

            if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
                log.error("request for phone notify error: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private MultiValueMap<String, String> getHeaders() {
        //获取token
        Object idToken = getToken();
        if (Objects.isNull(idToken)) {
            return null;
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", APPLICATION_JSON_VALUE);
        headers.add("Authorization", "Bearer " + idToken);
        return headers;
    }


    private Object getToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("scope", "openid groups");
        map.add("client_id", "sgt-notify-openapi");
        map.add("grant_type", "password");
        map.add("password", dingDingConfig.getPassword());
        map.add("username", dingDingConfig.getUsername());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(dingDingConfig.getDingDingTokenUrl(), request, Map.class);
        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            log.error("request for token error: {}", response.getBody());
            return null;
        }
        return response.getBody().get("id_token");
    }
}
