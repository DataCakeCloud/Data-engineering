package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

@Data
@Builder
@Table(name = "workflow_task")
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowTask extends DeleteEntity {
    private static final long serialVersionUID = -1279609125513023263L;

    private Integer workflowId;
    private Integer taskId;
    private Integer workflowVersionId;
    private Integer taskVersion;
}
