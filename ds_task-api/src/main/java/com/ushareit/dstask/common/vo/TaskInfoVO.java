package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Task;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @author fengxiao
 * @date 2022/9/13
 */
@Data
public class TaskInfoVO extends Task {
    private static final long serialVersionUID = -143226591620437658L;

    private Boolean isSparkTask;

    public TaskInfoVO(Task task) {
        BeanUtils.copyProperties(task, this);
        this.isSparkTask = !"PythonShell".equals(task.getTemplateCode()) && !"Hive2Redshift".equals(task.getTemplateCode());
    }
}
