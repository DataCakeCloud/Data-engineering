package com.ushareit.dstask.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.param.BatchGetTaskParam;
import com.ushareit.dstask.common.param.OutputCheckParam;
import com.ushareit.dstask.common.vo.LabelVO;
import com.ushareit.dstask.common.vo.TaskNameVO;
import com.ushareit.dstask.common.vo.TaskVO;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.AuditlogUtil;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Api(tags = "任务管理")
@RestController
@RequestMapping("/task")
public class TaskController extends BaseBusinessController<Task> {

    @Autowired
    private TaskService taskService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private UserGroupService userGroupService;
    @Resource
    public TemplateRegionImpService templateRegionImpService;

    @Override
    public BaseService<Task> getBaseService() {
        return taskService;
    }

    @Override
    @ApiOperation(value = "创建task")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @PostMapping("/add")
    public BaseResponse add(@RequestBody @Valid Task task) {
        task.setUserGroup(InfTraceContextHolder.get().getUuid());
        return super.add(task);
    }

    @DeleteMapping("/new/delete")
    public BaseResponse delete(@RequestParam("id") Integer id,
                               Boolean ifNotify) {
        taskService.deleteAndNotify(id, ifNotify);
        return BaseResponse.success();
    }

    @GetMapping("/downstreamTask")
    public BaseResponse downstreamTask(@RequestParam("id") Integer id) {
        return BaseResponse.success(taskService.downstreamTask(id));
    }


    @ApiOperation(value = "创建并且发布")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @PostMapping("/addAndStart")
    public BaseResponse addAndStart(@RequestBody @Valid Task task) {
        task.setUserGroup(InfTraceContextHolder.get().getUuid());
        task.setOnline(0);
        task.setAuditStatus(BaseActionCodeEnum.CREATEANDSTART.name());
        if (!task.getInvokingStatus()) {
            task.setAuditStatus(BaseActionCodeEnum.CREATE.name());
        }
        BaseResponse addResponse = super.add(task);
        if ((task.getOnline() == null || task.getOnline() != 1) || (StringUtils.isNotEmpty(task.getAuditStatus()) &&
                task.getAuditStatus().equals(BaseActionCodeEnum.CREATE.name()))) {
            return addResponse;
        }
        if (addResponse.getCodeStr().equals("SUCCESS")) {
            addResponse = start(task.getId(), null, null, BaseActionCodeEnum.CREATEANDSTART.name());
        }
        return addResponse;
    }

    @ApiOperation(value = "打标签application")
    @PostMapping("/tag")
    public BaseResponse tag(@RequestBody @Valid Task task) {
        taskService.tag(task);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "启动作业")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "部署ID", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "tagId", value = "版本ID", dataType = "int", example = "0"),
            @ApiImplicitParam(name = "savepointId", value = "保存点ID", dataType = "int", example = "0"),
            @ApiImplicitParam(name = "auditStatus", value = "审计操作状态", dataType = "String", example = "UPDATEANDSTART")
    })
    @PatchMapping("/start")
    public BaseResponse start(@RequestParam("id") Integer id,
                              @RequestParam(value = "tagId", required = false) Integer tagId,
                              @RequestParam(name = "savepointId", required = false) Integer savepointId,
                              @RequestParam(name = "auditStatus", required = false) String auditStatus) {
        try {
            taskService.start(id, tagId, savepointId);
            if (StringUtils.isEmpty(auditStatus)) {
                AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.START, "启动任务");
            }
            return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
        } catch (ServiceException e) {
            e.printStackTrace();
            return BaseResponse.error(e.getCodeStr(), e.getMessage(), e.getData() == null ? null: e.getData().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, e.getMessage());
        }

    }

    @ApiOperation(value = "保存并发布")
    @PostMapping("/updateAndStart")
    public BaseResponse updateAndStart(@RequestBody @Valid Task task) {
        task.setAuditStatus(BaseActionCodeEnum.UPDATEANDSTART.name());
        if (!task.getInvokingStatus()) {
            task.setAuditStatus(BaseActionCodeEnum.UPDATE.name());
        }
        BaseResponse addResponse = super.update(task);
        if ((task.getOnline() == null || task.getOnline() != 1) || (StringUtils.isNotEmpty(task.getAuditStatus()) &&
                task.getAuditStatus().equals(BaseActionCodeEnum.UPDATE.name()))) {
            return addResponse;
        }
        if (addResponse.getCodeStr().equals("SUCCESS")) {
            addResponse = start(task.getId(), null, null, BaseActionCodeEnum.UPDATEANDSTART.name());
        }
        return addResponse;
    }

    @ApiOperation(value = "获取ServiceUi地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务ID", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "genieJobId", value = "genie_job_id", dataType = "String", example = "0")
    })
    @GetMapping("/serviceui")
    public BaseResponse getServiceUi(@RequestParam("id") Integer id,
                                     String genieJobId,
                                     String state,
                                     String logFileUrl) throws Exception {

        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getServiceUi(id, genieJobId, state, logFileUrl));
    }


    @ApiOperation(value = "获取metrics地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务ID", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "genieJobId", value = "genie_job_id", dataType = "String", example = "0")
    })
    @GetMapping("/metricsui")
    public BaseResponse getMetricsUi(@RequestParam("id") Integer id, String genieJobId) throws Exception {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getMetricsUi(id, genieJobId));
    }

    @ApiOperation(value = "获取log地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务ID", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "executionDate", value = "执行入参时间", dataType = "String", example = "0")
    })
    @GetMapping("/logsui")
    public BaseResponse getLogsUi(@RequestParam("id") Integer id, String executionDate) throws Exception {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getlogsUi(id, executionDate));
    }

    @GetMapping("/instanceLog")
    public ResponseEntity<Object> getInstanceLog(@RequestParam("url") String url) throws Exception {
        return taskService.getInstanceLog(url);
    }

    @ApiOperation(value = "补数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务ID", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", required = true, dataType = "Date", example = "2020-01-01"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = true, dataType = "Date", example = "2020-01-01"),
            @ApiImplicitParam(name = "chidIds", value = "下游任务ID", required = true, dataType = "Date", example = "2020-01-01"),
    })
    @GetMapping("/backfill")
    public BaseResponse backFill(Integer id, String startDate, String endDate, Integer[] childIds, String isSendNotify, String isCheckDependency) {
        String message = String.format("对%s-%s的实例进行%s", startDate, endDate, "普通回溯");
        if (childIds != null && childIds.length > 0) {
            message = String.format("对%s-%s的实例进行%s", startDate, endDate, "深度补数");
        }
        AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.BACKFILL, message);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.backFill(id, startDate, endDate, childIds, isSendNotify, isCheckDependency));
    }

    @ApiOperation(value = "获取目录树")
    @GetMapping("/catalog")
    public BaseResponse getCatalog() {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getCatalog());
    }

    @ApiOperation(value = "获取目录树")
    @GetMapping("/backfill/process")
    public BaseResponse process(@RequestParam("userActionId") Integer userActionId) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.process(userActionId));
    }

    @Override
    @GetMapping("/page")
    public BaseResponse page(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "50") Integer pageSize,
                             @RequestParam Map<String, String> paramMap) {
        if(InfTraceContextHolder.get().getCurrentGroup() == null || InfTraceContextHolder.get().getCurrentGroup().isEmpty()){
            throw new ServiceException(BaseResponseCodeEnum.USER_GROUP_DEATIL_IS_NULL);
        }
        paramMap.put("userGroupDetail",InfTraceContextHolder.get().getUuid());

        return BaseResponse.success(taskService.page(pageNum, pageSize, paramMap));
    }

    @GetMapping("/resultSet")
    public BaseResponse getResultSet() {
        return BaseResponse.success(taskService.getResultSet());
    }

    @Override
    @GetMapping("/get")
    public BaseResponse getById(@RequestParam(required = false) Object id,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) Integer version) {
        if (id != null) {
            if (version != null) {
                return BaseResponse.success(taskService.getTaskVersionInfo(Integer.parseInt((String) id), version));
            }
            return BaseResponse.success(getBaseService().getById(id));
        }

        if (StringUtils.isNoneEmpty(name)) {
            return BaseResponse.success(getBaseService().getByName(name));
        }

        if (getBaseService().getById(id) == null) {
            return BaseResponse.error(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }

        return BaseResponse.success(getBaseService().getById(id));
    }

    @PostMapping("batch/get")
    public BaseResponse<List<Task>> batchGetIds(@RequestBody @NotEmpty(message = "至少要有一个任务ID")
                                                List<BatchGetTaskParam> paramList) {
        return BaseResponse.success(paramList.stream().map(item -> item.getTaskInfo(taskService))
                .collect(Collectors.toList()));
    }

    @GetMapping("/listforlabel")
    public BaseResponse listTasks(@RequestParam("name") String name) {
        return BaseResponse.success(taskService.listTasks(name));
    }

    @PatchMapping("/stop")
    public BaseResponse stop(@RequestParam(defaultValue = "1") Integer id) {
        try {
            AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.STOP, "任务停止");
            taskService.stop(id);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.JOB_CANCEL_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @PatchMapping("/cancel")
    public BaseResponse cancel(@RequestParam(defaultValue = "1") Integer id) {
        try {
            AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.CANCEL, "任务取消");
            taskService.cancel(id);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.JOB_CANCEL_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "获取DDL")
    @GetMapping("/getddl")
    public BaseResponse getDdl(@RequestParam("guid") String guId) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getDdl(guId));
    }

    @ApiOperation(value = "模版渲染")
    @GetMapping("/templateRendering")
    public BaseResponse templateRendering(@RequestParam("modelName") String modelName,@RequestParam("modelType") String modelType) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.templateRendering(modelName,modelType));
    }


    @ApiOperation(value = "获取DDL")
    @GetMapping("/getdispalyddl")
    public BaseResponse getDispalyDdl(@RequestParam("guid") String guId, @RequestParam("isSql") Boolean isSql) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getDisplayDdl(guId, isSql));
    }

    @ApiOperation(value = "获取下游依赖")
    @GetMapping("/getChildDependencies")
    public BaseResponse getChildDependencies(@RequestParam(defaultValue = "1") Integer id) {
        return BaseResponse.success(taskService.getChildDependencies(id));
    }

    @ApiOperation(value = "获取依赖概述")
    @GetMapping("/getDependenciesOverview")
    public BaseResponse getDependenciesOverview(@RequestParam("tableName") String tableName) {
        return BaseResponse.success(taskService.getDependenciesOverview(tableName));
    }

    @ApiOperation(value = "获取血缘图")
    @GetMapping("/getDependencies")
    public BaseResponse getDependencies(@RequestParam(defaultValue = "1") Integer id,
                                        @RequestParam(defaultValue = "2") Integer level,
                                        @RequestParam("executionDate") String executionDate,
                                        @RequestParam("upDown") Integer upDown) {
        return BaseResponse.success(taskService.getDependencies(id, level, executionDate, upDown));
    }

    @ApiOperation(value = "sql校验")
    @PostMapping("/check")
    public BaseResponse check(@RequestBody @Valid Task task) {
        try {
            return taskService.check(task);
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.JOB_CHECK_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "状态同步（离线）")
    @PutMapping("/statushook")
    public BaseResponse statusHook(@RequestParam(defaultValue = "1") String taskName, String status) {
        try {
            if (StringUtils.isBlank(status)) {
                return BaseResponse.error(BaseResponseCodeEnum.JOB_CANCEL_FAIL.name(), "status code is null");
            }

            taskService.statusHook(taskName, status);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.JOB_CANCEL_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "状态同步（实时）")
    @GetMapping("/flinkstatushook")
    public BaseResponse flinkstatushook(@RequestParam("name") String taskName, @RequestParam(defaultValue = "FINISHED") String status) {
        try {
            taskService.statusHook(taskName, status);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.JOB_CANCEL_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "离线任务上下线")
    @PutMapping("/onlineAndOffline")
    public BaseResponse onlineAndOffline(@RequestParam("id") Integer id,
                                         @RequestParam("status") Integer status,
                                         @RequestParam("ifnotify") Boolean ifNotify) {
        try {
            taskService.onlineAndOffline(id, status, ifNotify);
            return BaseResponse.success();
        } catch (Exception e) {
            log.error(String.format("taskId[%d] failed to onlineAndOffline: %s", id, CommonUtil.printStackTraceToString(e)));
            return BaseResponse.error(BaseResponseCodeEnum.ONLINE_AND_OFFLINE_FAIL, e.getMessage());
        }
    }

    @ApiOperation(value = "任务名模糊查询")
    @GetMapping("name/search")
    public BaseResponse<List<TaskNameVO>> search(String name) {
        return BaseResponse.success(taskService.searchByName(name));
    }

    @ApiOperation(value = "批量更新角色")
    @PostMapping("/role/batchupdate")
    public BaseResponse batchUpdateRole(@RequestBody @Valid BatchUpdateRole batchUpdate) {
        try {
            taskService.batchUpdateRole(batchUpdate.getOwner(), batchUpdate.getCollaborators(), batchUpdate.getTaskNames());
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.BATCH_UPDATE_ROLE_FAILED, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "批量修改owner或协作者")
    @PutMapping("/batch/update/ownerOrCollaborator")
    public BaseResponse batchUpdateOwnerOrCollaborator(@RequestParam("taskIds") String taskIds,
                                                       @RequestParam(name = "owner", required = false) String owner,
                                                       @RequestParam(name = "collaborator", required = false) String collaborator) {
        try {
            taskService.batchUpdateOwnerOrCollaborator(taskIds, owner, collaborator);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.BATCH_UPDATE_ROLE_FAILED, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "查询iam账号")
    @GetMapping("/iam")
    public BaseResponse getIams() {
        String groups = getCurrentUser().getGroupName();
        return taskService.getIams(groups);
    }

    @ApiOperation(value = "任务一键复制")
    @GetMapping("/copy")
    public BaseResponse copy(@RequestParam("id") Integer id, @RequestParam("name") String name) {
        try {
            taskService.copy(id, name);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.APP_SAVE_FAIL, CommonUtil.printStackTraceToString(e));
        }
    }

    @ApiOperation(value = "后端统计信息")
    @GetMapping("/statistic")
    public BaseResponse getStatistic() {
        return taskService.getStatistic();
    }

    @ApiOperation(value = "任务名模糊查询")
    @GetMapping("/upanddown")
    public BaseResponse upAndDown(@RequestParam("id") Integer id, @RequestParam("upDown") Integer upDown) {
        return BaseResponse.success(taskService.upAndDown(id, upDown));
    }

    @ApiOperation(value = "任务名模糊查询")
    @GetMapping("selectbyoutputguid")
    public BaseResponse<Task> selectByOutput(String output) {
        return BaseResponse.success(taskService.selectByOutputGuid(output));
    }

    @ApiOperation(value = "查询dataset是否为外部数据")
    @GetMapping("/datasetInfo")
    public BaseResponse getDatasetInfo(@RequestParam("type") String type, @RequestParam("region") String region, String source, @RequestParam("db") String db, @RequestParam("tbl") String tbl, String qualifyname) {
        return BaseResponse.success(taskService.getDatasetInfo(type, region, source, db, tbl, qualifyname));
    }

    @ApiOperation(value = "查询dataset是否为外部数据")
    @GetMapping("/renderContent")
    public BaseResponse renderContent(@RequestParam("content") String content,String taskName) {
        return BaseResponse.success(taskService.renderContent(content,taskName));
    }

    @ApiOperation(value = "解析sql的jinjia表达式")
    @PostMapping("/renderSql")
    public BaseResponse renderSql(@RequestBody Map<String, String> requestBody) {
        return BaseResponse.success(taskService.renderContent(requestBody.get("sql"), ""));
    }

    @GetMapping("/backfillDateDetail")
    public BaseResponse getBackFillDateDetail(@RequestParam("taskId") Integer taskId){
        return BaseResponse.success(taskService.getBackFillDateDetail(taskId));
    }

    /**
     * 预留批量修改runtimeconfig中isAutoScaleMode为false
     *
     * @return
     */
    @ApiOperation(value = "任务名模糊查询")
    @GetMapping("/backupdate")
    public BaseResponse backUpdate() {
        return BaseResponse.success(taskService.backUpdate());
    }

    /**
     * @return
     */
    @ApiOperation(value = "扩缩容tm")
    @PutMapping("/autoscaletm")
    public BaseResponse autoScaleTm(Integer id, Integer count) {
        taskService.autoScaleTm(id, count);
        return BaseResponse.success();
    }

    /**
     * @return
     */
    @ApiOperation(value = "获取自动扩缩容任务的tm数量")
    @GetMapping("/gettmnum")
    public BaseResponse autoScaleTm(Integer id) {
        Integer num = taskService.getAutoScaleTaskParal(id);
        return BaseResponse.success(num);
    }

    @ApiOperation(value = "获取最近7个状态实例接口")
    @GetMapping("/last7")
    public BaseResponse getLast7State(@RequestParam("ids") String ids) {
        return BaseResponse.success(taskService.getLast7State(ids));
    }

    @ApiOperation(value = "获取GIT SQL文件")
    @GetMapping("/getGitSqlFile")
    public BaseResponse getGitSqlFile(@RequestParam("projectName") String projectName,
                                      @RequestParam("filePath") String filePath) throws IOException {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getGitFileSql(projectName, filePath));
    }

    @ApiOperation(value = "获取sparksql所有用到的表")
    @PostMapping("/sqlTbls")
    public BaseResponse getSqlTbl(@RequestParam("sql") String sql,
                                  @RequestParam("region") String region) {
        return BaseResponse.success(taskService.getEtlSqlTbl(sql, region));
    }

    @ApiOperation(value = "获取模糊匹配所有的表")
    @PostMapping("/getTasks")
    public BaseResponse getTasks(@RequestParam("name") String name) {
        return BaseResponse.success(taskService.getTaskInfo(name));
    }

    @ApiOperation(value = "获取数据集对应的offset")
    @PostMapping("/getOffset")
    public BaseResponse getOffset(@RequestParam("table") String table, @RequestParam("granularity") String granularity, @RequestParam("sql") String sql) {
        return BaseResponse.success(taskService.getOffset(table, granularity, sql));
    }

    @ApiOperation(value = "校验生成数据集路径")
    @PostMapping("/checkPath")
    public BaseResponse checkPath(@RequestParam("path") String path) {
        return BaseResponse.success(taskService.checkPath(path));
    }

    @ApiOperation(value = "获取Crontab结果文件")
    @PostMapping("/getCrontab")
    public BaseResponse getCrontab(@RequestBody @Valid CrontabParam crontabParam) {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, taskService.getCrontab(crontabParam));
    }

    @ApiOperation(value = "将airflow时间参数的任务转换为DS时间参数的任务")
    @PutMapping("/dateTransform")
    public BaseResponse dateTransform(Integer id, String airflowCrontab, String newTaskName) {
        try {
            taskService.dateTransform(id, airflowCrontab, newTaskName);
        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, CommonUtil.printStackTraceToString(e));
        }
        return BaseResponse.success();
    }

    @ApiOperation(value = "更新spark高级参数")
    @PostMapping("/updateSeniorParam")
    public BaseResponse updateSeniorParam(@RequestBody @Valid Task task) {
        Boolean aBoolean = taskService.updateSeniorParam(task.getId(), task.getSparkConfParam());
        start(task.getId(), null, null, BaseActionCodeEnum.UPDATEANDSTART.name());
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, aBoolean);
    }

    @ApiOperation(value = "获取日期预览")
    @PostMapping("/date/preview")
    public BaseResponse getDatePreview(@RequestBody @Valid TaskDatePreviewReq TaskDatePreviewReq) {
        return BaseResponse.success(taskService.getDatePreview(TaskDatePreviewReq.getTaskGra(), TaskDatePreviewReq.getTaskCrontab(), TaskDatePreviewReq.getDataDepend(), TaskDatePreviewReq.getTaskDepend()));
    }

    @ApiOperation(value = "小文件合并创建并且发布")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @PostMapping("/mergeSmallFiles")
    public BaseResponse mergeSmallFilesAddAndStart(@RequestBody @Valid Task task) {
        String groupName = getCurrentUser().getGroupName();
//        if (StringUtils.isBlank(groupName)) {
//            throw new ServiceException(BaseResponseCodeEnum.NO_GROUP);
//        }
//        task.setTenancyCode(groupName.split(",")[0]);
        task.setOnline(0);
        task.setAuditStatus(BaseActionCodeEnum.CREATEANDSTART.name());
        if (!task.getInvokingStatus()) {
            task.setAuditStatus(BaseActionCodeEnum.CREATE.name());
        }

        task.setMainClass("com.shareit.storage.CombineSmallFiles");

        String inputDataset = task.getInputDataset();
        JSONArray jsonArray = JSONArray.parseArray(inputDataset);
        JSONObject json = jsonArray.getJSONObject(0);
        JSONObject metadata = json.getJSONObject("metadata");
        String table = metadata.getString("table");
        String db = metadata.getString("db");
        task.setMainClassArgs(db + "." + table + " " + task.getPathType());
        BaseResponse addResponse = super.add(task);
        if ((task.getOnline() == null || task.getOnline() != 1) || (StringUtils.isNotEmpty(task.getAuditStatus()) &&
                task.getAuditStatus().equals(BaseActionCodeEnum.CREATE.name()))) {
            return addResponse;
        }
        if (addResponse.getCodeStr().equals("SUCCESS")) {
            addResponse = start(task.getId(), null, null, BaseActionCodeEnum.CREATEANDSTART.name());
        }
        return addResponse;
    }

    @ApiOperation(value = "获取jar包下拉菜单")
    @PostMapping("/mergeSmallFiles/jars")
    public BaseResponse jars(@RequestBody JSONObject task) {
        String region = task.getString("region");
        TemplateRegionImp templateRegionImp = templateRegionImpService
                .selectOne(new TemplateRegionImp().setTemplateCode(TemplateEnum.MergeSmallFiles.name()).setRegionCode(region));
        if (templateRegionImp == null) {
            throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
        }
        return BaseResponse.success(templateRegionImp.getUrl());
    }

    @ApiOperation(value = "合并小文件保存并发布")
    @PostMapping("/updateMergeSmallFiles")
    public BaseResponse updateMergeSmallFiles(@RequestBody @Valid Task task) {
        task.setAuditStatus(BaseActionCodeEnum.UPDATEANDSTART.name());
        if (!task.getInvokingStatus()) {
            task.setAuditStatus(BaseActionCodeEnum.UPDATE.name());
        }
        String inputDataset = task.getInputDataset();
        JSONArray jsonArray = JSONArray.parseArray(inputDataset);
        JSONObject json = jsonArray.getJSONObject(0);
        JSONObject metadata = json.getJSONObject("metadata");
        String table = metadata.getString("table");
        String db = metadata.getString("db");
        task.setMainClassArgs(db + "." + table + " " + task.getPathType());
        BaseResponse addResponse = super.update(task);
        if ((task.getOnline() == null || task.getOnline() != 1) || (StringUtils.isNotEmpty(task.getAuditStatus()) &&
                task.getAuditStatus().equals(BaseActionCodeEnum.UPDATE.name()))) {
            return addResponse;
        }
        if (addResponse.getCodeStr().equals("SUCCESS")) {
            addResponse = start(task.getId(), null, null, BaseActionCodeEnum.UPDATEANDSTART.name());
        }
        return addResponse;
    }

    @ApiOperation(value = "立即执行接口，暂只供Push使用")
    @PostMapping("/realtimeExecute")
    public BaseResponse realtimeExecute(@RequestParam("taskId") Integer taskId, String newArgs, String callbackUrl) {
        try {
            taskService.realtimeExecute(taskId, newArgs, callbackUrl);
        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, CommonUtil.printStackTraceToString(e));
        }
        return BaseResponse.success();
    }

    @GetMapping("search")
    public BaseResponse<List<LabelVO>> his(@RequestParam("keyword") String keyword) {
        Map<Integer, Task> taskMap = taskService.getScheduleTaskListByUser(InfTraceContextHolder.get().getUserName());
        List<Label> labelList = labelService.getList(InfTraceContextHolder.get().getUserName());

        List<LabelVO> existLabelList = labelList.stream()
                .map(label -> new LabelVO(label, taskMap))
                .filter(label -> CollectionUtils.isNotEmpty(label.getTaskList()))
                .collect(Collectors.toList());

        Set<Integer> existLabelIds = existLabelList.stream()
                .flatMap(item -> item.getTaskList().stream().map(TaskVO::getId).collect(Collectors.toSet()).stream())
                .collect(Collectors.toSet());

        List<Task> noLabelTaskList = taskMap.entrySet().stream()
                .filter(item -> !existLabelIds.contains(item.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(noLabelTaskList)) {
            existLabelList.add(new LabelVO("default", noLabelTaskList));
        }

        if (StringUtils.isBlank(keyword)) {
            return BaseResponse.success(existLabelList);
        }

        List<LabelVO> filteredLabelList = existLabelList.stream().map(item -> {
            if (item.getLabelName().contains(keyword)) {
                return item;
            }

            List<TaskVO> filteredTaskVOList = item.getTaskList().stream()
                    .filter(one -> one.getName().contains(keyword) ||
                            StringUtils.equalsIgnoreCase(one.getId().toString(), keyword))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredTaskVOList)) {
                return null;
            }

            item.setTaskList(filteredTaskVOList);
            return item;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return BaseResponse.success(filteredLabelList);
    }

    @PostMapping("output/check/exist")
    public BaseResponse<?> checkExist(@Valid @RequestBody OutputCheckParam param) {
        taskService.checkOutputDataset(param.getTaskId(), param.getOutputDataset());
        return BaseResponse.success();
    }

    @ApiOperation(value = "快速补数接口")
    @PostMapping("/fastBackfill")
    public BaseResponse fastBackfill(@RequestParam("taskId") Integer taskId, String args, String callbackUrl,String startDate,String endDate) {
        try {
            if (args == null) {
                args = "";
            }
            if (callbackUrl == null) {
                callbackUrl = "";
            }
            if (startDate == null) {
                startDate = "";
            }
            if (endDate == null) {
                endDate = "";
            }
            taskService.fastBackfill(taskId,args, callbackUrl,startDate,endDate);

        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, CommonUtil.printStackTraceToString(e));
        }
        return BaseResponse.success();
    }

    @ApiOperation(value = "事件触发调度")
    @PostMapping("/eventTrigger")
    public BaseResponse eventTrigger(@Valid @RequestBody EventTrigger eventTrigger) {
        return BaseResponse.success(taskService.eventTrigger(eventTrigger));
    }

    @ApiOperation(value = "获取任务执行的Command")
    @GetMapping("/getCommand")
    public BaseResponse getCommand(@RequestParam("taskId") Integer taskId) {
        return BaseResponse.success(taskService.getCommand(taskId));
    }

}

