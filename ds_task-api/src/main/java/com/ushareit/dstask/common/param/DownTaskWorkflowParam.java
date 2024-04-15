package com.ushareit.dstask.common.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
public class DownTaskWorkflowParam {

    @NotNull(message = "工作流ID不能为空")
    private Integer workflowId;

    @NotNull(message = "工作流版本ID不能为空")
    private Integer workflowVersionId;

}
