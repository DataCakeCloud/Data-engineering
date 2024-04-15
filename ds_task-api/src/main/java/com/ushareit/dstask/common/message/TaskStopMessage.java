package com.ushareit.dstask.common.message;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
public class TaskStopMessage implements MessageCardBuilder, Serializable {
    private static final long serialVersionUID = 709948403439258036L;

    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

    private String message;

    @Override
    public CardEnum getType() {
        return CardEnum.STOP_INFO;
    }
}
