package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.action.Create;
import com.ushareit.dstask.common.action.Update;
import com.ushareit.dstask.common.module.WorkflowInfo;
import com.ushareit.dstask.common.param.*;
import com.ushareit.dstask.common.vo.DownTaskVO;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.common.vo.WorkflowVO;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.constant.WorkflowStatus;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.service.impl.UserGroupServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("workflow")
public class WorkflowController extends BaseBusinessController<Task>{

    @Resource
    private TaskService taskService;
    @Resource
    private WorkflowService workflowService;
    @Resource
    private WorkflowTaskService workflowTaskService;
    @Resource
    private WorkflowVersionService workflowVersionService;
    @Resource
    private AccessGroupService accessGroupService;
    @Autowired
    private UserGroupService userGroupService;
    @Resource
    private UserGroupServiceImpl userGroupServiceImpl;

    @PostMapping("addOne")
    public BaseResponse<?> addOne(@Validated(Create.class) @RequestBody WorkFlowParam param) {
        param.validate(taskService);
        if (CollectionUtils.isNotEmpty(workflowService.searchByName(param.getName()))) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(),
                    String.format("已经存在名为【%s】的工作流，不能重复", param.getName()));
        }

        workflowService.addOne(param.toAddWorkflow(), param.toAddWorkflowVersion(), param.decoratedTaskList());
        return BaseResponse.success();
    }

    @PostMapping("updateOne")
    public BaseResponse<?> updateOne(@Validated(Update.class) @RequestBody WorkFlowParam param) {
        param.validate(taskService);
        if (StringUtils.isNotBlank(param.getName()) && workflowService.searchByName(param.getName()).stream()
                .anyMatch(item -> item.getId() != param.getId().intValue())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(),
                    String.format("已经存在名为【%s】的工作流，请更换", param.getName()));
        }

        WorkflowInfo workflowInfo = workflowService.getInfo(param.getId(), param.getOriginWorkflowVersionId());
        workflowService.updateOne(param.toUpdateWorkflow(), param.toUpdateWorkflowVersion(workflowInfo.getWorkflowVersion()),
                param.decoratedTaskList(), workflowInfo, BooleanUtils.toBooleanDefaultIfNull(param.getNotify(), false));
        return BaseResponse.success();
    }

    @PostMapping("deleteOne")
    public BaseResponse<?> delete(@Valid @RequestBody DeleteWorkflowParam param) {
        WorkflowInfo workflowInfo = workflowService.getCurrentVersionInfo(param.getWorkflowId());
        if (workflowInfo.getStatusEnum() == WorkflowStatus.ONLINE) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "未下线工作流不可删除");
        }

        workflowService.deleteAndNotify(workflowInfo, param.getNotify());
        return BaseResponse.success();
    }

    @PostMapping("turnOn")
    public BaseResponse<?> online(@Valid @RequestBody TurnOnWorkflowParam param) {
        Workflow workflow = workflowService.getById(param.getWorkflowId());
        if (workflow == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "工作流不存在");
        }

        Optional<WorkflowVersion> workflowVersionOptional = workflowVersionService.getByVersion(param.getWorkflowId(),
                param.getVersion());
        if (!workflowVersionOptional.isPresent()) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "工作流版本不存在");
        }

        if (workflow.getStatus() == WorkflowStatus.ONLINE.getType() && workflow.getCurrentVersion().intValue() ==
                workflowVersionOptional.get().getVersion()) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "已经上线的工作流版本，无需再次上线");
        }

        workflowService.turnOn(param.getWorkflowId(), workflowVersionOptional.get().getId(),
                BooleanUtils.toBooleanDefaultIfNull(param.getNotify(), false));
        return BaseResponse.success();
    }

    @PostMapping("turnOff")
    public BaseResponse<?> turnOff(@Valid @RequestBody TurnOffWorkflowParam param) {
        WorkflowInfo workflowInfo = workflowService.getCurrentVersionInfo(param.getWorkflowId());
        if (workflowInfo == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "不存在已上线的工作流版本，请核实");
        }

        workflowService.turnOff(workflowInfo, param.getNotify());
        return BaseResponse.success();
    }

    @PostMapping("page")
    public BaseResponse<PageInfo<WorkflowVO>> page(@Valid @RequestBody WorkflowSearchParam param) {
        List<AccessGroup> groupList = new ArrayList<>();
        if (param.getComefromLabel()!=null&&param.getComefromLabel()==true&&StringUtils.isBlank(param.getWorkflows())&&StringUtils.isBlank(param.getKeyword())){
            return BaseResponse.success(new PageInfo<>());
        }
        if (param.getOnlyMine()) {
            groupList = accessGroupService.getParentGroupList(InfTraceContextHolder.get().getTenantId(), InfTraceContextHolder.get().getUserName());
        }
        Example example = param.toExample(groupList);
        if(!InfTraceContextHolder.get().getAdmin()){
            Example.Criteria criteria = new Example(Workflow.class).createCriteria();
            criteria.andEqualTo("userGroup",InfTraceContextHolder.get().getGroupId());
            example.and(criteria);
        }

        PageInfo<Workflow> workflowPage = workflowService.listByPage(param.getPageNo(), param.getPageSize(),
                example);

        Map<Integer, Optional<WorkflowVersion>> workflowVersionMap = workflowVersionService.getWorkflowVersionList(
                PageUtils.mapToList(workflowPage, item -> Pair.create(item.getId(), item.getCurrentVersion())));

        Map<Integer, List<WorkflowTask>> workflowVersionTaskMap = workflowTaskService.getWorkflowVersionTaskMap(
                workflowVersionMap.values().stream().filter(Optional::isPresent).map(item -> item.get().getId())
                        .collect(Collectors.toList()));

        Stream<Integer> accessGroupIds = PageUtils.mapToList(workflowPage, Workflow::getUserGroup).stream()
                .filter(StringUtils::isNotBlank)
                .flatMap(item -> Arrays.stream(item.split(SymbolEnum.COMMA.getSymbol())))
                .map(Integer::parseInt).distinct();
        Map<Integer, AccessGroup> groupMap = accessGroupService.mapByIds(accessGroupIds);

        return BaseResponse.success(PageUtils.map(workflowPage, item ->{
            item.setUserGroupName(userGroupServiceImpl.selectUserGroupById(Integer.parseInt(item.getUserGroup())).getName());
            return new WorkflowVO(item, workflowVersionMap,
                    workflowVersionTaskMap, groupMap);
        }));
    }

    @PostMapping("cron")
    public BaseResponse<String> cron(@Valid @RequestBody CronParam param) {
        param.validate();
        return BaseResponse.success(param.toExpression());
    }

    @GetMapping("down/task/listByTaskIds")
    public BaseResponse<List<DownTaskVO>> downList(@RequestParam("taskIds") List<Integer> taskIds) {
        List<Pair<Task, Task>> downTaskList = workflowService.getDownTaskList(taskIds);
        return BaseResponse.success(downTaskList.stream().map(DownTaskVO::new).collect(Collectors.toList()));
    }

    @GetMapping("down/task/list")
    public BaseResponse<List<DownTaskVO>> downList(@RequestParam("workflowId") Integer workflowId) {
        WorkflowInfo workflowInfo = workflowService.getCurrentVersionInfo(workflowId);
        List<Pair<Task, Task>> downTaskList = workflowService.getDownTaskList(workflowInfo.getTaskIds());
        return BaseResponse.success(downTaskList.stream().map(DownTaskVO::new).collect(Collectors.toList()));
    }

    @Override
    public BaseService<Task> getBaseService() {
        return taskService;
    }
}
