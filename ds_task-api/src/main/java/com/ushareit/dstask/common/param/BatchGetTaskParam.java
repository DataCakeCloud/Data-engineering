package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.TaskService;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/12/4
 */
@Data
public class BatchGetTaskParam {

    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

    private Integer version;

    public Task getTaskInfo(TaskService taskService) {
        if (this.version == null) {
            return taskService.getById(taskId);
        }

        return taskService.getTaskByVersion(this.taskId, this.version);
    }

}
