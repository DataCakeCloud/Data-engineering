package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.OperateLog;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author fengxiao
 * @date 2021/8/12
 */
@Data
public class OperateLogSaveParam {

    @NotBlank(message = "用户ID不能为空")
    private String userName;

    private String traceId;

    @NotBlank(message = "请求方法不能为空")
    private String type;

    @NotBlank(message = "请求路径不能为空")
    private String uri;

    private String params;

    @NotBlank(message = "返回码不能为空")
    private String resultCode;

    private String resultMessage;

    private String resultData;

    @NotNull(message = "请求时间不能为空")
    private Long requestTime;

    @NotNull(message = "返回时间不能为空")
    private Long responseTime;

    public OperateLog toEntity() {
        OperateLog operateLog = new OperateLog()
                .setUserName(userName)
                .setType(type)
                .setUri(uri)
                .setResultCode(resultCode)
                .setRequestTime(new Date(requestTime))
                .setResponseTime(new Date(responseTime))
                .setCostTime(responseTime - requestTime);

        if (StringUtils.isNotBlank(traceId)) {
            operateLog.setTraceId(traceId);
        }

        if (StringUtils.isNotBlank(params)) {
            operateLog.setParams(params);
        }

        if (StringUtils.isNotBlank(resultMessage)) {
            operateLog.setResultMessage(resultMessage);
        }

        if (StringUtils.isNotBlank(resultData)) {
            operateLog.setResultData(resultData);
        }

        return operateLog;
    }

}
