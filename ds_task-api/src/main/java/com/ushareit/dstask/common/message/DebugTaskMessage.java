package com.ushareit.dstask.common.message;

import com.ushareit.dstask.bean.Task;
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
public class DebugTaskMessage implements Serializable {
    private static final long serialVersionUID = 5542289814866920989L;

    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    @NotEmpty(message = "任务信息不能为空")
    private List<Task> taskList;

}
