package com.ushareit.dstask.common.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author fengxiao
 * @date 2022/12/14
 */
@Data
public class OutputCheckParam {

    private Integer taskId;

    @NotBlank(message = "生成数据集不能为空")
    private String outputDataset;

}
