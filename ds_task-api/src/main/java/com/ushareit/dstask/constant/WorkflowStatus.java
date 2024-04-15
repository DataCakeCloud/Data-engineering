package com.ushareit.dstask.constant;

import com.ushareit.dstask.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fengxiao
 * @date 2022/11/14
 */
@Getter
@AllArgsConstructor
public enum WorkflowStatus {

    CREATED(0),
    ONLINE(1),
    OFFLINE(2);

    private final int type;

    public static WorkflowStatus of(Integer code) {
        if (code == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND.name(), "工作流状态不能为空");
        }

        for (WorkflowStatus status : WorkflowStatus.values()) {
            if (status.type == code) {
                return status;
            }
        }

        throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND.name(), "工作流状态值不存在");
    }

}
