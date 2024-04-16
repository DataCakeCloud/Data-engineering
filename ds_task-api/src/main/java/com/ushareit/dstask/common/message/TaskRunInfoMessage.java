package com.ushareit.dstask.common.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
@Data
public class TaskRunInfoMessage implements MessageCardBuilder, Serializable {
    private static final long serialVersionUID = 712055660470131538L;

    @NotNull(message = "会话ID不能为空")
    private String chatId;

    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

    @NotNull(message = "消息不能为空")
    private String message;

    @Override
    public CardEnum getType() {
        return CardEnum.TASK_MESSAGE;
    }
}
