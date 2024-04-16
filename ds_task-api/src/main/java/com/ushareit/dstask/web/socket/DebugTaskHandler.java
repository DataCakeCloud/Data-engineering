package com.ushareit.dstask.web.socket;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.common.message.CardEnum;
import com.ushareit.dstask.common.message.MessageCard;
import com.ushareit.dstask.common.message.ResponseMessage;
import com.ushareit.dstask.common.message.SystemMessage;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.service.WorkflowService;
import com.ushareit.dstask.web.utils.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengxiao
 * @date 2022/11/23
 */
@Slf4j
@Component
public class DebugTaskHandler extends AbstractWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private WorkflowService workflowService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CurrentUser currentUser = (CurrentUser) session.getAttributes().get(CommonConstant.CURRENT_LOGIN_USER);
        sessionMap.compute(session.getId(), ((key, ss) -> {
            if (ss == null) {
                return session;
            }
            return ss;
        }));

        log.info("用户 {} 连接成功, chatId is {}, 当前在线人数为: {}", currentUser.getUserId(), session.getId(), sessionMap.size());
        session.sendMessage(new TextMessage(ResponseMessage.success(new SystemMessage("连接成功, 会话ID: " + session.getId()).toCard())));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserId(session);
        log.info("用户 {} 的会话 ID {} 发送信息 {}", userId, session.getId(), message.getPayload());

        try {
            MessageCard messageCard = JSONObject.parseObject(message.getPayload(), MessageCard.class);
            ValidateUtils.validate(messageCard);

            switch (Objects.requireNonNull(CardEnum.of(messageCard.getType()))) {
                case DEBUG:
                    List<Task> debugTaskList = messageCard.parseToList(Task.class);
                    workflowService.debugTaskList(userId, session.getId(), debugTaskList);
                    break;
                case SHUTDOWN:
                    List<Integer> taskIdList = messageCard.parseToList(Integer.class);
                    workflowService.stopDebugTask(userId, session.getId(), taskIdList);
                    break;
                default:
                    throw new RuntimeException("不支持的消息类别");
            }

            session.sendMessage(new TextMessage(ResponseMessage.success()));
        } catch (JSONException e) {
            log.error(String.format("chatId %s for user %s errors", session.getId(), getUserId(session)), e);
            session.sendMessage(new TextMessage(ResponseMessage.error("传入数据不是合法的JSON格式")));
            session.close();
        } catch (Exception e) {
            log.error(String.format("chatId %s for user %s errors", session.getId(), getUserId(session)), e);
            session.sendMessage(new TextMessage(ResponseMessage.error(e.getMessage())));
            session.close();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable t) throws Exception {
        log.error(String.format("用户 %s 的会话窗口 %s 发生错误，原因是 %s", getUserId(session), session.getId(),
                t.getMessage()), t);
        try {
            session.close();
        } catch (Exception e) {
            log.error(String.format("chatId %s for user %s errors", session.getId(), getUserId(session)), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessionMap.remove(session.getId());
        log.info("用户退出: {}, 当前在线人数为: {}, 退出原因为: {}", getUserId(session), sessionMap.size(), closeStatus.getReason());

        try {
            session.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 给前端发送消息
     *
     * @param chatId  会话ID
     * @param message 发送的消息
     */
    public void sendMessage(String chatId, String message) {
        WebSocketSession session = sessionMap.get(chatId);
        if (session == null) {
            return;
        }

        int sendCount = 0;
        do {
            try {
                session.sendMessage(new TextMessage(message));
                return;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendCount++;
            }
        } while (sendCount < 3);

        log.error("chatId {} for user {} is broken, then removed", chatId, getUserId(session));
        sessionMap.remove(chatId);
    }

    private String getUserId(WebSocketSession session) {
        CurrentUser currentUser = (CurrentUser) session.getAttributes().get(CommonConstant.CURRENT_LOGIN_USER);
        return currentUser.getUserId();
    }
}
