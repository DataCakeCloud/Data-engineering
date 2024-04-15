package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Task;
import lombok.Data;
import org.apache.commons.math3.util.Pair;

/**
 * @author fengxiao
 * @date 2022/12/8
 */
@Data
public class DownTaskVO {

    private String curTaskName;
    private String downTaskName;
    private String downTaskOwner;

    public DownTaskVO(Pair<Task, Task> taskPair) {
        this.curTaskName = taskPair.getKey().getName();
        this.downTaskName = taskPair.getValue().getName();
        this.downTaskOwner = taskPair.getValue().getCreateBy();
    }
}
