package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.Task;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author fengxiao
 * @date 2023/2/7
 */
@Data
public class ValidatorCheckParam {

    @NotBlank(message = "校验类别不能为空")
    private String validType;

    private Task task;
}
