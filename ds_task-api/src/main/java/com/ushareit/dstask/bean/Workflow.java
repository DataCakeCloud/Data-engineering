package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Builder
@Table(name = "workflow")
@AllArgsConstructor
@NoArgsConstructor
public class Workflow extends DeleteEntity {
    private static final long serialVersionUID = 4068649361903230138L;

    private String name;
    private String source;
    private Integer currentVersion;
    private Integer status;
    private String owner;
    private String collaborators;
    private String granularity;
    private String cronConfig;
    private String userGroup;
    @Transient
    private String userGroupName;

}
