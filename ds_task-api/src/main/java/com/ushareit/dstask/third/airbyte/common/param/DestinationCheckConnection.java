package com.ushareit.dstask.third.airbyte.common.param;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/9/7
 */
@Data
public class DestinationCheckConnection {

    @NotNull(message = "数据定义ID不能为空")
    private Integer destinationDefinitionId;

    @NotNull(message = "数据实例配置信息不能为空")
    private JSONObject connectionConfiguration;

}
