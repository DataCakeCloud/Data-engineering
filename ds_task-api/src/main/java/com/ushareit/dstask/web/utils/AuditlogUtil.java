package com.ushareit.dstask.web.utils;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.Auditlog;
import com.ushareit.dstask.constant.BaseActionCodeEnum;
import com.ushareit.dstask.mapper.AuditlogMapper;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * @author: licg
 * @create: 2022-06-01 10:08
 **/
@Component
@Slf4j
public class AuditlogUtil {
    private static AuditlogUtil auditlogUtil;

    @Autowired
    private AuditlogMapper auditlogMapper;

    @PostConstruct
    public void init() {
        auditlogUtil = this;
        auditlogUtil.auditlogMapper = this.auditlogMapper;
    }

    public static void auditlog(String module, BaseActionCodeEnum resCode) {
        saveAuditlog(module, null, null, resCode.name(), resCode.getMessage());
    }

    public static void auditlog(String module, Integer eventId, BaseActionCodeEnum resCode, String message) {
        saveAuditlog(module, eventId, null, resCode.getMessage(), message);
    }

    public static void auditlog(String module, Integer eventId, Integer eventVersion, BaseActionCodeEnum resCode, String message) {
        saveAuditlog(module, eventId, eventVersion, resCode.getMessage(), message);
    }

    private static void saveAuditlog(String module, Integer eventId, Integer eventVersion, String eventCode, String eventMessage) {
        String currentUserName = InfTraceContextHolder.get().getUserName();
        if (currentUserName.equals("system") && InfTraceContextHolder.get().getTraceId() == null) {
            eventMessage = "系统触发:" + eventMessage;
        }
        String snapshot = JSON.toJSONString(InfTraceContextHolder.get().getParamInfo());

        Auditlog auditlog = new Auditlog()
                .setModule(module)
                .setEventId(eventId)
                .setEventVersion(eventVersion)
                .setTraceId(InfTraceContextHolder.get().getTraceId())
                .setEventCode(eventCode)
                .setEventMessage(eventMessage)
                .setEventSnapshot(snapshot)
                .setCreateTime(new Timestamp(System.currentTimeMillis()))
                .setCreateBy(currentUserName);

        auditlogUtil.auditlogMapper.insertSelective(auditlog);
    }
}
