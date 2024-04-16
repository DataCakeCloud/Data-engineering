package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.TaskSnapshot;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.TaskSnapshotService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "任务快照管理")
@RestController
@RequestMapping("/tasksnapshot")
public class TaskSnapshotController extends BaseBusinessController<TaskSnapshot> {

    @Autowired
    private TaskSnapshotService taskSnapshotService;

    @Override
    public BaseService<TaskSnapshot> getBaseService() {
        return taskSnapshotService;
    }

    @PostMapping("/trigger")
    public BaseResponse triggerSavepoint(@RequestBody TaskSnapshot taskSnapshot) {
        taskSnapshot.setCreateBy(InfTraceContextHolder.get().getUserName())
                .setCreateTime(new Timestamp(System.currentTimeMillis()));
        taskSnapshotService.trigger(taskSnapshot);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


    @GetMapping("/list")
    @Override
    public BaseResponse list(TaskSnapshot taskSnapshot) {
        return BaseResponse.success(taskSnapshotService.list(taskSnapshot));
    }
}
