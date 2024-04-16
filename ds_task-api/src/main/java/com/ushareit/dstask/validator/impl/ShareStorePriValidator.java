package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.sharestore.ShareStoreService;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <a href="https://shimo.im/docs/L9kBMdRa6DuM4bqK">ShareStore常见问题文档</a>
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.SHARE_STORE_PRI)
public class ShareStorePriValidator implements Validator {

    @Autowired
    private ShareStoreService shareStoreService;

    @Override
    public void validateImpl(Task task, TaskContext context) {
        RuntimeConfig runtimeConfig = JSONObject.parseObject(task.getRuntimeConfig(), RuntimeConfig.class);
        if (runtimeConfig == null) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_RUN_PARAM_NOT_NULL);
        }

        if (StringUtils.isBlank(runtimeConfig.getRestEndpoint())) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_ADDRESS_NOT_NULL);
        }

        if (StringUtils.isBlank(runtimeConfig.getClusterLoad())) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CLUSTER_NOT_NULL);
        }

        if (StringUtils.isBlank(runtimeConfig.getSegmentLoad())) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_SEGMENT_NOT_NULL);
        }

        if (!shareStoreService.segmentExist(runtimeConfig.getRestEndpoint(), runtimeConfig.getClusterLoad(),
                runtimeConfig.getSegmentLoad())) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_SEGMENT_NOT_FOUND.name(),
                    String.format(BaseResponseCodeEnum.SHARESTORE_SEGMENT_NOT_FOUND.getMessage(), runtimeConfig.getSegmentLoad(),
                            runtimeConfig.getClusterLoad()));
        }
    }
}
