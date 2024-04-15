package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.common.module.WorkflowInfo;
import lombok.Data;
import org.assertj.core.util.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@Data
public class WorkflowDetailVO extends WorkflowVO {

    private List<Task> taskList;

    public WorkflowDetailVO(WorkflowInfo workflowInfo, List<Task> taskList, Map<Integer, AccessGroup> groupMap) {
        super(workflowInfo, Maps.newHashMap(workflowInfo.getId(), Optional.of(workflowInfo.getWorkflowVersion())),
                Collections.emptyMap(), groupMap);
        this.taskList = taskList;
    }
}
