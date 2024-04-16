package com.ushareit.dstask.third.dingding;

import java.util.List;

/**
 * @author fengxiao
 * @date 2021/2/4
 */

public interface DingDingService {
    /**
     * 发送钉钉通知
     *
     * @param shareIdList 接收通知的钉钉号
     * @param message     通知内容
     */
    void notify(List<String> shareIdList, String message);

    /**
     * 发送钉钉通知
     *
     * @param shareIdList 接收通知的钉钉号
     * @param message     通知内容
     */
    void notify(List<String> shareIdList, String message, boolean isMaster);

    /**
     * @param shareIdList
     * @param message
     * @author wuyan
     */
    void notifyCard(List<String> shareIdList, Integer feedbackId, String message);

    /**
     * @param shareIdList
     * @param message
     * @author wuyan
     */
    void notifyCard(List<String> shareIdList, Integer feedbackId, String message, boolean isMaster);

    /**
     * @author wuyan
     * @param message
     * @param secret
     * @param webhook
     * @param isAtAll
     * @param mobiles
     * @param shareIds
     */
    void notifyRobot(String message,
                     String secret,
                     String webhook,
                     boolean isAtAll,
                     List<String> mobiles,
                     List<String> shareIds
    );

}
