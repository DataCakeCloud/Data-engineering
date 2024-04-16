package com.ushareit.dstask.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.third.phone.PhoneService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.scheduled.param.AlertModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * author :xuebotao
 * date :2023-04-10
 * <p>
 * support : email dingding phone
 */

@Slf4j
@Component
public class AlarmNoticeUtil {

    @Resource
    private EmailUtils emailUtils;

    @Resource
    private DingDingService dingDingService;

    @Resource
    private PhoneService phoneService;

    //电话通知
    public void phoneNotice(List<String> shareIdList, String level) {
        switch (level) {
            case "failure":
                phoneService.notify(shareIdList, "DataCake任务失败报警");
            case "retry":
                phoneService.notify(shareIdList, "DataCake任务正在重试");
            case "success":
                phoneService.notify(shareIdList, "DataCake任务成功");
            case "start":
                phoneService.notify(shareIdList, "DataCake任务正在启动");
        }
    }

    //邮件通知
    public void emailNotice(List<String> shareIdList, String message) {
        emailUtils.sendMessage(shareIdList, "[DataCake]:告警通知", message);
    }

    //dingding通知
    public void dingdingNotice(List<String> shareIdList, String message) {
        dingDingService.notify(shareIdList, message, false);
    }

    public void enterpriseWeChatNotice(List<String> shareIdList, String message, Map<String, String> config) {
        WxWorkAlarmUtil.sendMessage(shareIdList, "[DataCake]:任务告警通知:", message, config);
    }


    public void notice(List<String> shareIdList, String message, String type, String level, Map<String, String> config) {
        if (StringUtils.isEmpty(type)) {
            return;
        }
        switch (type) {
            case "phone":
                phoneNotice(shareIdList, level);
                break;
            case "dingTalk":
                dingdingNotice(shareIdList, message);
                break;
            case "email":
                emailNotice(shareIdList, message);
                break;
            case "enterprise_wechat":
                enterpriseWeChatNotice(shareIdList, message, config);
                break;

        }
    }


    public void notice(Task task, String message) {
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        String alertModel = runtimeConfigObject.getString("alertModel");
        log.warn(" alertModel is :" + alertModel);
        if (StringUtils.isEmpty(alertModel)) {
            return;
        }

        Map<String, JSONObject> alertMap = JSON.parseObject(alertModel, Map.class);
        if (alertMap != null) {
            for (Map.Entry<String, JSONObject> alertNode : alertMap.entrySet()) {
                String level = alertNode.getKey();
                String value = alertNode.getValue().toJSONString();
                AlertModel detail = JSON.parseObject(value, AlertModel.class);
                boolean isNotify = detail.getAlertType().size() > 0;
                if (isNotify) {
                    for (String type : detail.getAlertType()) {
                        Map<String, String> config = new HashMap<>();
                        config.put("URL", detail.getWechatRobotKey());
                        List<String> noticeUser = new ArrayList<>();
                        List<UserBase> emailReceivers = detail.getEmailReceivers();
                        List<UserBase> wechatReceivers = detail.getWechatReceivers();
                        if (emailReceivers != null && !emailReceivers.isEmpty() && type.equals("enterprise_wechat")) {
                            List<String> collect = emailReceivers.stream().map(UserBase::getName).collect(Collectors.toList());
                            noticeUser.addAll(collect);
                        }

                        if (wechatReceivers != null && !wechatReceivers.isEmpty() && type.equals("email")) {
                            List<String> collect = wechatReceivers.stream().map(UserBase::getName).collect(Collectors.toList());
                            noticeUser.addAll(collect);
                        }


                        if (detail.isNotifyCollaborator()) {
                            addCollaborators(noticeUser, task.getCollaborators(), runtimeConfigObject.getString("owner"));
                        } else {
                            noticeUser.add(runtimeConfigObject.getString("owner"));
                        }
                        notice(noticeUser, message, type, level, config);
                    }
                }
            }
        }
    }


    public void addCollaborators(List<String> noticeUser, String collaborators, String owner) {
        if (StringUtils.isEmpty(collaborators)) {
            noticeUser.add(owner);
            return;
        }
        Set<String> set = new HashSet<>();
        set.add(owner);
        String[] collaboratorsArray = collaborators.split(",");
        List<String> collaboratorsList = Arrays.asList(collaboratorsArray);
        set.addAll(collaboratorsList);
        noticeUser.addAll(set);

    }

}
