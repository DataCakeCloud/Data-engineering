package com.ushareit.dstask.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
@AllArgsConstructor
public class TaskMessage implements MessageCardBuilder, Serializable {
    private static final long serialVersionUID = 1675428948577266810L;

    private Integer taskId;
    private String message;

    @Override
    public CardEnum getType() {
        return CardEnum.TASK_MESSAGE;
    }
}
