package com.ushareit.dstask.common.message;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
@Data
public class ShutdownTaskMessage implements Serializable {
    private static final long serialVersionUID = 5542289814866920989L;

    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    @NotEmpty(message = "至少要包含一个任务ID")
    private List<Integer> taskIds;

}
