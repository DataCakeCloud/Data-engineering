package com.ushareit.dstask.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.module.CronConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.UserGroupMapper;
import com.ushareit.dstask.service.impl.UserGroupServiceImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@Data
@NoArgsConstructor
public class WorkflowVO {

    private Integer id;
    private Integer version;
    private String name;
    private String description;
    private String createBy;
    private String updateBy;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String granularity;
    private Integer status;
    private String source;
    private Integer workflowVersionId;
    private Timestamp nextExecuteTime;
    private String owner;
    private List<String> collaborators;
    private List<AccessGroupVO> groupList;
    private CronConfig cronConfig;
    private List<Integer> taskIds;
    private String userGroup;

    public WorkflowVO(Workflow workflow, Map<Integer, Optional<WorkflowVersion>> workflowVersionMap,
                      Map<Integer, List<WorkflowTask>> workflowVersionTaskMap, Map<Integer, AccessGroup> groupMap) {
        this.id = workflow.getId();
        this.source = workflow.getSource();
        this.createBy = workflow.getCreateBy();
        this.updateBy = workflow.getUpdateBy();
        this.createTime = workflow.getCreateTime();
        this.updateTime = workflow.getUpdateTime();
        this.name = workflow.getName();
        this.status = workflow.getStatus();
        this.description = workflow.getDescription();
        this.granularity = workflow.getGranularity();
        this.userGroup = workflow.getUserGroupName();

        Optional<WorkflowVersion> workflowVersionOptional = workflowVersionMap.getOrDefault(this.id, Optional.empty());
        if (!workflowVersionOptional.isPresent()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(),
                    String.format("工作流 %s 没有当前版本信息", workflow.getId()));
        }

        WorkflowVersion workflowVersion = workflowVersionOptional.get();
        this.workflowVersionId = workflowVersion.getId();
        this.owner = workflowVersion.getOwner();
        this.collaborators = StringUtils.isBlank(workflowVersion.getCollaborators()) ? Collections.emptyList() :
                Arrays.stream(workflowVersion.getCollaborators().split(SymbolEnum.COMMA.getSymbol()))
                        .collect(Collectors.toList());
        
        this.groupList = StringUtils.isBlank(workflowVersion.getUserGroup()) ? Collections.emptyList() :
                Arrays.stream(workflowVersion.getUserGroup().split(SymbolEnum.COMMA.getSymbol()))
                        .map(item -> groupMap.get(Integer.parseInt(item)))
                        .filter(Objects::nonNull).map(AccessGroupVO::new)
                        .collect(Collectors.toList());

        this.cronConfig = StringUtils.isBlank(workflowVersion.getCronConfig()) ? null :
                JSONObject.parseObject(workflowVersion.getCronConfig(), CronConfig.class);
        this.version = workflowVersion.getVersion();

        List<WorkflowTask> workflowTaskList = workflowVersionTaskMap.get(this.workflowVersionId);
        this.taskIds = CollectionUtils.emptyIfNull(workflowTaskList).stream().map(WorkflowTask::getTaskId)
                .collect(Collectors.toList());
    }
}
