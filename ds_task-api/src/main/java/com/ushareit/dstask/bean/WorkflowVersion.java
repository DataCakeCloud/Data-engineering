package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @author fengxiao
 * @date 2022/11/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "workflow_version")
public class WorkflowVersion extends DeleteEntity {
    private static final long serialVersionUID = -8222704293561240690L;

    private Integer workflowId;
    private String name;
    private Integer version;
    private Integer status;
    private String owner;
    private String collaborators;
    private String granularity;
    private String cronConfig;
    private String userGroup;
}

