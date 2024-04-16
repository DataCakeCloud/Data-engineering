package com.ushareit.dstask.common.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
public class TurnOnWorkflowParam {

    @NotNull(message = "工作流ID不能为空")
    private Integer workflowId;

    @NotNull(message = "工作流版本不能为空")
    private Integer version;

    private Boolean notify;

}
