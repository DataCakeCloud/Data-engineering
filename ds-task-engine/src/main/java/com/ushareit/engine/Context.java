package com.ushareit.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.engine.param.RuntimeConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Context {
    private String runtimeConfigStr;
    private String sourceType;
    private String sourceConfigStr;
    private JSONObject sourceConfigJson;
    private String sinkType;
    private String sinkConfigStr;
    private JSONObject sinkConfigJson;
    // local、cluster
    private String executeMode;
    private String jarPath;

    private RuntimeConfig runtimeConfig;

    public void prepare() {
        if(StringUtils.isEmpty(runtimeConfigStr)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR);
        }
        runtimeConfig = JSON.parseObject(runtimeConfigStr, RuntimeConfig.class);
        executeMode = runtimeConfig.getExecuteMode();

        if(StringUtils.isNotEmpty(sourceConfigStr)) {
            sourceConfigJson = JSON.parseObject(sourceConfigStr);
        }

        if(StringUtils.isNotEmpty(sinkConfigStr)) {
            sinkConfigJson = JSON.parseObject(sinkConfigStr);
        }
    }
}