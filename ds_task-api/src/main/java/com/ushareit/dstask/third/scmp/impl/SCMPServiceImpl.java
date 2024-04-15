package com.ushareit.dstask.third.scmp.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ushareit.dstask.third.scmp.SCMPService;
import com.ushareit.dstask.third.scmp.vo.SCMPMemberItem;
import com.ushareit.dstask.third.scmp.vo.SCMPOnDutyItem;
import com.ushareit.dstask.third.scmp.vo.SCMPResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author fengxiao
 * @date 2022/3/9
 */
@Slf4j
@Service
public class SCMPServiceImpl implements SCMPService {

    private static final String TOKEN_URL = "xxx";
    private static final String USERNAME = "xxx";
    private static final String PASSWORD = "xxx";

    /**
     * 值班信息，配置在 SCMP 平台
     */
    private static final Integer PLAN_ID = 767;
    private static final String PLAN_URL = "xxx" + PLAN_ID;
    private static final String MEMBER_URL = "xxx";


    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getDutyMan() {
        Object idToken = getToken();
        if (Objects.isNull(idToken)) {
            throw new RuntimeException("获取 token 失败");
        }

        return getDutyMap(idToken).get(getDutyId(idToken));
    }

    private Map<Integer, String> getDutyMap(Object token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("Authorization", "Bearer " + token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(MEMBER_URL, HttpMethod.GET, request, String.class);
        //log.info("request for scmp member list : {}", response);

        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            throw new RuntimeException("获取人员列表信息失败");
        }

        SCMPResult<SCMPMemberItem> result = JSONObject.parseObject(response.getBody(),
                new TypeReference<SCMPResult<SCMPMemberItem>>() {
                });

        if (result == null || result.getCode() != NumberUtils.INTEGER_ZERO.intValue()) {
            throw new RuntimeException("获取 BDP 人员列表失败");
        }

        if (CollectionUtils.isEmpty(result.getResult())) {
            throw new RuntimeException("BDP 人员列表为空");
        }

        return result.getResult().stream()
                .collect(HashMap::new, (m, item) -> m.put(item.getId(), item.getShareId()), HashMap::putAll);
    }

    private Integer getDutyId(Object token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(PLAN_URL, HttpMethod.GET, request, String.class);

        // 如果失败则记录到日志中
        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            log.error("request for scmp plan error: {}", response.getBody());
            throw new RuntimeException("获取值班人信息失败");
        }

        SCMPResult<SCMPOnDutyItem> result = JSONObject.parseObject(response.getBody(),
                new TypeReference<SCMPResult<SCMPOnDutyItem>>() {
                });
        if (result == null || result.getCode() != NumberUtils.INTEGER_ZERO.intValue()) {
            throw new RuntimeException("获取值班人列表失败");
        }

        if (CollectionUtils.isEmpty(result.getResult())) {
            throw new RuntimeException("值班列表为空");
        }

        long timeMillis = System.currentTimeMillis();
        List<Integer> dutyList = result.getResult().stream()
                .filter(item -> item.onDuty(timeMillis))
                .map(SCMPOnDutyItem::getMembers)
                .findFirst().orElseThrow(() -> new RuntimeException("当前无人值班"));

        if (CollectionUtils.isEmpty(dutyList)) {
            throw new RuntimeException("当前值班列表为空");
        }

        return dutyList.get(NumberUtils.INTEGER_ZERO);
    }

    private Object getToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("scope", "openid groups");
        map.add("client_id", "scmp-oncall");
        map.add("grant_type", "password");
        map.add("password", PASSWORD);
        map.add("username", USERNAME);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            log.error("request for token error: {}", response.getBody());
            throw new RuntimeException("get oidc for scmp error");
        }

        Map<String, Object> result = JSONObject.parseObject(response.getBody(),
                new TypeReference<Map<String, Object>>() {
                });

        //log.info("result is {}", result);
        return result == null ? null : result.get("id_token");
    }

}
