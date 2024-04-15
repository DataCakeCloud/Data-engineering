package com.ushareit.dstask.third.dingding.impl;

import com.google.common.collect.Maps;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.configuration.DingDingConfig;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.third.dingding.vo.DingDingCardRequest;
import com.ushareit.dstask.third.dingding.vo.DingDingTextRequest;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.DingUtil;
import com.ushareit.dstask.web.utils.EmailUtils;
import com.ushareit.dstask.web.utils.ItUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author fengxiao
 * @date 2021/2/4
 */
@Slf4j
@Service
public class DingDingServiceImpl implements DingDingService {

    public static final String MSG_TEXT_TYPE = "text";
    public static final String MSG_CARD_TYPE = "action_card";
    public static final String CONTENT = "content";
    private static final List<String> MODULES = Arrays.asList("DE", "QE", "BI", "LAKECAT");
    @Resource
    private DingDingConfig dingDingConfig;

    @Value("${de-server-url.host}")
    private String noticeUrl = "";


    @Resource
    private EmailUtils emailUtils;
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void notify(List<String> shareIdList, String message) {
        notify(shareIdList, message, false);
    }

    @Override
    public void notify(List<String> shareIdList, String message, boolean isMaster) {
        try {
            if (isMaster && !DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
                InfTraceContextHolder.get().setTenantName(DsTaskConstant.SHAREIT_TENANT_NAME);
                InfTraceContextHolder.get().setTenantId(1);
            }

            Map<String, String> content = Maps.newHashMap();
            content.put(CONTENT, message);
            DingDingTextRequest dingDingTextRequest = DingDingTextRequest.builder()
                    .msgtype(MSG_TEXT_TYPE).receiver(shareIdList).text(content).build();
            //如果是cloud版发邮箱
            Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
            if (isPrivate) {
                emailUtils.sendMessage(shareIdList, "工作通知", message);
                return;
            }

            MultiValueMap<String, String> headers = getHeaders();
            if (Objects.isNull(headers) || shareIdList.isEmpty()) {
                return;
            }

            HttpEntity<DingDingTextRequest> entity = new HttpEntity<>(dingDingTextRequest, headers);
            log.info("text:" + shareIdList.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())));
            ResponseEntity<Map> response = restTemplate.exchange(dingDingConfig.getDingDingUrl(), HttpMethod.POST, entity, Map.class);
            // 如果钉钉发送消息失败，则输出到日志中
            if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
                log.error("request for dingDing notify error: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void notifyCard(List<String> shareIdList, Integer feedbackId, String message) {
        notifyCard(shareIdList, feedbackId, message, false);
    }

    @Override
    public void notifyCard(List<String> shareIdList, Integer feedbackId, String message, boolean isMaster) {
        if (isMaster && !DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
            InfTraceContextHolder.get().setTenantName(DsTaskConstant.SHAREIT_TENANT_NAME);
            InfTraceContextHolder.get().setTenantId(1);
        }

        Map<String, Object> card = generateCard(message, feedbackId, shareIdList);
        DingDingCardRequest dingDingRequest = DingDingCardRequest.builder()
                .msgtype(MSG_CARD_TYPE).receiver(shareIdList).action_card(card).build();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            emailUtils.sendMessage(shareIdList, "工作通知", message);
            return;
        }

        MultiValueMap<String, String> headers = getHeaders();
        if (Objects.isNull(headers)) {
            return;
        }

        HttpEntity<DingDingCardRequest> entity = new HttpEntity<>(dingDingRequest, headers);
        log.info("card:" + shareIdList.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())));
//        ResponseEntity<Map> response = restTemplate.exchange(dingDingConfig.getDingDingUrl(), HttpMethod.POST, entity, Map.class);
        // 如果钉钉发送消息失败，则输出到日志中
//        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
//            System.out.println("request for dingDing notify error: " + response.getBody());
//        }
    }


    @Override
    public void notifyRobot(String message, String secret, String webhook, boolean isAtAll, List<String> mobiles, List<String> shareIds) {
        List<String> dingdingUserIds = getDingdingUserIds(shareIds);
        log.info("robot:" + shareIds.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())));
        // 通知具体的人
        DingUtil.buildRequest(message,
                secret,
                webhook,
                false,
                new ArrayList<>(),
                dingdingUserIds);
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

    private Map<String, Object> generateCard(String message, Integer feedbackId, List<String> shareIdList) {
        Map<String, Object> card = Maps.newHashMap();
        card.put("title", "请处理以下工单");
        card.put("markdown", message);
        card.put("btn_orientation", "1");
        List<Map<String, String>> btnJsonList = generateBtnJsonList(feedbackId, shareIdList);
        card.put("btn_json_list", btnJsonList);
        return card;
    }

    private List<Map<String, String>> generateBtnJsonList(Integer feedbackId, List<String> shareIdList) {
        List<Map<String, String>> btnJsonList = new ArrayList<>(MODULES.size() + 1);
        Map<String, String> acceptBtn = new HashMap<>(2);
        acceptBtn.put("title", "受理");
        acceptBtn.put("action_url", getAcceptUrl(feedbackId, shareIdList.get(0)));
        btnJsonList.add(acceptBtn);

        MODULES.forEach(module -> {
            Map<String, String> btn = generateAssignButton("转让" + module, module, feedbackId);
            btnJsonList.add(btn);
        });
        return btnJsonList;
    }

    private Map<String, String> generateAssignButton(String title, String module, Integer feedbackId) {
        Map<String, String> btn = new HashMap<>(2);
        if (module.equalsIgnoreCase("LAKECAT")) {
            title = "转让治理";
        }
        btn.put("title", title);
        btn.put("action_url", getAssignUrl(feedbackId, module));
        return btn;
    }

    private List<String> getDingdingUserIds(List<String> shareIds) {
        ItUtil itUtil = new ItUtil();
        List<UserBase> userBases = itUtil.batchGetUsersInfo(shareIds);
        List<String> collect = userBases.stream().map(UserBase::getDingTalkUid).collect(Collectors.toList());
        return collect;
    }

    private String getAssignUrl(Integer feedbackId, String module) {
        return getBaseUrl() + "feedback/assignByDingding?feedbackId=" + feedbackId + "&module=" + module;
    }

    private String getAcceptUrl(Integer feedbackId, String acceptMan) {
        log.info("accept url is " + (getBaseUrl() + "feedback/acceptByDingding?feedbackId=" + feedbackId + "&accept=" + acceptMan));
        return getBaseUrl() + "feedback/acceptByDingding?feedbackId=" + feedbackId + "&accept=" + acceptMan;
    }

    private String getBaseUrl() {
//        String env = InfTraceContextHolder.get().getEnv();
//        if (DsTaskConstant.PROD.equalsIgnoreCase(env)) {
//            return "http://ds-task.ushareit.org/";
//        }
//
//        if (DsTaskConstant.TEST.equalsIgnoreCase(env)) {
//            return "http://ds-task-test.ushareit.org/";
//        }
        // TODO 需添加
//        return "http://ds-task-dev.ushareit.org/";
//        return "http://localhost:8088/";
        if (StringUtils.isEmpty(noticeUrl)) {
            return "http://localhost:8088/";
        }
        return noticeUrl;
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
