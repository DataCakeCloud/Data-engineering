package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.bean.WorkflowTask;
import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.common.module.WorkflowInfo;
import com.ushareit.dstask.common.param.GetWorkflowInfoParam;
import com.ushareit.dstask.common.param.WorkflowVersionSearchParam;
import com.ushareit.dstask.common.vo.WorkflowDetailVO;
import com.ushareit.dstask.common.vo.WorkflowVersionVO;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@RestController
@RequestMapping("workflow/version")
public class WorkflowVersionController {

    @Resource
    private TaskMapper taskMapper;
    @Resource
    private TaskService taskService;
    @Resource
    private WorkflowService workflowService;
    @Resource
    private AccessGroupService accessGroupService;
    @Resource
    private WorkflowTaskService workflowTaskService;
    @Resource
    private WorkflowVersionService workflowVersionService;

    @PostMapping("page")
    public BaseResponse<PageInfo<WorkflowVersionVO>> page(@Valid @RequestBody WorkflowVersionSearchParam param) {
        PageInfo<WorkflowVersion> workflowPage = workflowVersionService.listByPage(param.getPageNo(),
                param.getPageSize(), param.toExample());

        Workflow workflow = workflowService.getById(param.getWorkflowId());
        Map<Integer, List<WorkflowTask>> workflowVersionTaskMap = workflowTaskService.getWorkflowVersionTaskMap(
                PageUtils.mapToList(workflowPage, WorkflowVersion::getId));

        return BaseResponse.success(PageUtils.map(workflowPage, item -> new WorkflowVersionVO(item,
                workflow.getCurrentVersion(), workflowVersionTaskMap)));
    }

    @PostMapping("info")
    public BaseResponse<WorkflowDetailVO> getInfo(@Valid @RequestBody GetWorkflowInfoParam param) {
        WorkflowInfo workflowInfo;
        if (param.getVersion() != null) {
            workflowInfo = workflowService.getInfoByVersion(param.getWorkflowId(), param.getVersion());
        } else {
            workflowInfo = workflowService.getCurrentVersionInfo(param.getWorkflowId());
        }

        Map<Integer, AccessGroup> groupMap = accessGroupService.mapByIds(workflowInfo.toGroupList().stream());
        return BaseResponse.success(new WorkflowDetailVO(workflowInfo, workflowInfo.getTaskList().stream()
                .map(item -> taskService.getTaskByVersion(item.getTaskId(), item.getTaskVersion()))
                .collect(Collectors.toList()), groupMap));
    }
}
