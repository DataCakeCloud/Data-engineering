package com.ushareit.dstask.common.module;

import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.bean.WorkflowTask;
import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.constant.WorkflowStatus;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聚合 Workflow 和 WorkflowVersion
 *
 * @author fengxiao
 * @date 2022/11/14
 */
@Data
public class WorkflowInfo extends Workflow {
    private static final long serialVersionUID = 3130544048117181193L;

    private WorkflowVersion workflowVersion;
    private List<WorkflowTask> taskList;

    public WorkflowInfo(Workflow workflow, WorkflowVersion workflowVersion, List<WorkflowTask> workflowTaskList) {
        BeanUtils.copyProperties(workflow, this);
        this.workflowVersion = workflowVersion;
        this.taskList = workflowTaskList;
    }

    public WorkflowStatus getStatusEnum() {
        return WorkflowStatus.of(this.getStatus());
    }

    public List<Integer> getTaskIds() {
        return taskList.stream().map(WorkflowTask::getTaskId).collect(Collectors.toList());
    }

    public List<Pair<Integer, Integer>> getTaskVersionList() {
        return taskList.stream()
                .map(item -> Pair.create(item.getTaskId(), item.getTaskVersion()))
                .collect(Collectors.toList());
    }

    public List<Integer> toGroupList() {
        return StringUtils.isBlank(this.getUserGroup()) ? Collections.emptyList() :
                Arrays.stream(this.getUserGroup().split(SymbolEnum.COMMA.getSymbol()))
                        .map(Integer::parseInt).collect(Collectors.toList());
    }
}
