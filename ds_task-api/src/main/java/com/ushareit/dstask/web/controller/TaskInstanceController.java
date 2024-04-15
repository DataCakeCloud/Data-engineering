package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.ClearStopParam;
import com.ushareit.dstask.bean.EventTrigger;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskInstance;
import com.ushareit.dstask.constant.BaseActionCodeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.TaskInstanceService;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.utils.AuditlogUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "任务实例管理")
@RestController
@RequestMapping("/taskinstance")
public class TaskInstanceController extends BaseBusinessController<TaskInstance> {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private TaskServiceImpl taskService;

    @Override
    public BaseService<TaskInstance> getBaseService() {
        return taskInstanceService;
    }

    @ApiOperation(value = "任务重跑（离线）")
    @PostMapping("/clear")
    public BaseResponse clear(@RequestParam("name") String name, String[] executionDate, Boolean isCheckUpstream) throws Exception {
        Task task = taskService.getByName(name);
        if("Hive2Sharestore".equals(task.getTemplateCode())) {
            taskService.start(task.getId(), null, null);
        }
        taskInstanceService.clear(name, executionDate, isCheckUpstream);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


    @ApiOperation(value = "批量任务重跑（离线）")
    @PostMapping("/batch_clear")
    public BaseResponse batchClear(@RequestBody @Valid List<ClearStopParam> clearStopParamList) throws Exception {
        taskInstanceService.clear(clearStopParamList);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "任务停止（离线）")
    @PatchMapping("/stop")
    public BaseResponse stopEtl(@RequestParam("name") String name, @RequestParam("status") String status, @RequestParam("executionDate") String executionDate, @RequestParam(name = "flinkUrl", required = false) String flinkUrl) {
        return BaseResponse.success(taskInstanceService.stopEtlJob(name, status, executionDate, flinkUrl));
    }

    @ApiOperation(value = "批量任务停止（离线）")
    @PostMapping("/batch_stop")
    public BaseResponse batchStop(@RequestBody @Valid List<ClearStopParam> clearStopParamList) {
        return BaseResponse.success(taskInstanceService.stopEtlJob(clearStopParamList));
    }

    @ApiOperation(value = "任务实例状态检测")
    @GetMapping("/diagnose")
    public BaseResponse diagnose(@RequestParam("name") String name, String executionDate, String state) {
        return BaseResponse.success(taskInstanceService.diagnose(name, executionDate, state));
    }

    @ApiOperation(value = "删除K8S deployment")
    @DeleteMapping("/deleteDeploy")
    public BaseResponse deleteDeploy(@RequestParam("name") String name, @RequestParam("context") String context) {
        return BaseResponse.success(taskInstanceService.deleteDeploy(name, context));
    }

    @Override
    @GetMapping("/page")
    public BaseResponse page(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "50") Integer pageSize,
                             @RequestParam Map<String, String> paramMap) {
        return BaseResponse.success(taskInstanceService.page(pageNum, pageSize, paramMap));
    }

    @GetMapping("/exeSql/get")
    public BaseResponse getExeSql(@RequestParam("taskId") Integer taskId,
                                  @RequestParam("executionDate") String executionDate,
                                  @RequestParam(defaultValue = "0") Integer version) {
        return taskInstanceService.getExeSql(taskId, executionDate, version);
    }

    @ApiOperation(value = "根据instanceId获取任务状态")
    @GetMapping("/uuidState")
    public BaseResponse getStateByUid(@RequestParam("uuid") String uuid) {
        return  BaseResponse.success(taskInstanceService.getStateByUid(uuid));
    }

    @GetMapping("/offline_pages")
    public BaseResponse offlinePages(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "50") Integer pageSize,
                                     @RequestParam Map<String, String> paramMap) {
        return BaseResponse.success(taskInstanceService.offlineTaskInstancePages(pageNum, pageSize, paramMap));
    }


}
