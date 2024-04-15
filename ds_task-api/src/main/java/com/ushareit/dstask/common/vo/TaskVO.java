package com.ushareit.dstask.common.vo;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
@Data
public class TaskVO {

    private Integer id;
    private String name;
    private String granularity;

    public TaskVO(Task task) {
        this.id = task.getId();
        this.name = task.getName();

        TriggerParam triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
        if (triggerParam != null) {
            this.granularity = triggerParam.getOutputGranularity();
        }
    }
}
