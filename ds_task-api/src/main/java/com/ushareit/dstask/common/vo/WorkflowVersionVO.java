package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.WorkflowTask;
import com.ushareit.dstask.bean.WorkflowVersion;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@Data
public class WorkflowVersionVO {

    private Integer workflowVersionId;
    private Integer workflowId;
    private Integer version;
    private String description;
    private String createBy;
    private Timestamp createTime;
    private Integer status;
    private Boolean isCurrentVersion;
    private List<Integer> taskIds;

    public WorkflowVersionVO(WorkflowVersion workflowVersion, Integer currentVersion,
                             Map<Integer, List<WorkflowTask>> workflowVersionTaskMap) {
        this.workflowVersionId = workflowVersion.getId();
        this.workflowId = workflowVersion.getWorkflowId();
        this.version = workflowVersion.getVersion();
        this.description = workflowVersion.getDescription();
        this.createBy = workflowVersion.getCreateBy();
        this.createTime = workflowVersion.getCreateTime();
        this.status = workflowVersion.getStatus();
        this.isCurrentVersion = workflowVersion.getVersion().intValue() == currentVersion;

        List<WorkflowTask> workflowTaskList = workflowVersionTaskMap.get(workflowVersionId);
        this.taskIds = CollectionUtils.emptyIfNull(workflowTaskList).stream().map(WorkflowTask::getTaskId)
                .collect(Collectors.toList());
    }
}
