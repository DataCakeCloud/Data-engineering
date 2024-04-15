package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.Task;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/12/1
 */
@Data
public class TaskParam extends Task {
    private static final long serialVersionUID = -1057523868496786978L;

    private String taskKey;
}
