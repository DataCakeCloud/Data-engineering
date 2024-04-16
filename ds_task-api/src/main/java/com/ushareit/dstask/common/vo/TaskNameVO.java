package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Task;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Data
public class TaskNameVO {

    private Integer id;
    private String name;

    public TaskNameVO(Task task) {
        this.id = task.getId();
        this.name = task.getName();
    }

}
