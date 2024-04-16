package com.ushareit.dstask.common.message;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author fengxiao
 * @date 2022/11/29
 */
public interface MessageCardBuilder {

    @JSONField(serialize = false)
    CardEnum getType();

    /**
     * 转换给前端的卡片类型
     */
    default MessageCard toCard() {
        MessageCard messageCard = new MessageCard();
        messageCard.setType(getType().name());
        messageCard.setData(this);
        return messageCard;
    }

}
