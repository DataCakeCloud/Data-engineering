package com.ushareit.dstask.common.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
public class DeleteWorkflowParam {

    @NotNull(message = "工作流ID不能为空")
    private Integer workflowId;

    @NotNull(message = "是否通知下游任务不能为空")
    private Boolean notify;

}
