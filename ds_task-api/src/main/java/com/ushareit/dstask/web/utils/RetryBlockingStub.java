package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class RetryBlockingStub {
    private static final int maxRetry = 2;
    public static TaskSchedulerApi.TaskCommonResponse executeWithRetry(Callable<TaskSchedulerApi.TaskCommonResponse> method) {
        int retryCount = 0;
        while (retryCount <= maxRetry) {
            try {
                TaskSchedulerApi.TaskCommonResponse taskCommonResponse = method.call();
                if (taskCommonResponse == null) {
                    throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
                }
                if ("UNAVAILABLE: io exception".equals(taskCommonResponse.getMessage())) {
                    throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "UNAVAILABLE: io exception");
                }
                return taskCommonResponse;
            } catch (Exception e) {
                retryCount++;
                log.warn("Request failed. Retrying... Retry count: " + retryCount + " method:" + method.toString() );
                if (retryCount > maxRetry) {
                    log.warn("Max retry limit reached. Giving up.");
                    throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "UNAVAILABLE: io exception");
                }
            }
        }
         throw new ServiceException(BaseResponseCodeEnum.SYS_ERR);
    }
}

