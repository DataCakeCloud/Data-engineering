package com.ushareit.dstask.third.schedule;

import com.ushareit.dstask.api.TaskRpcServiceGrpc;
import com.ushareit.dstask.api.TaskServiceApi;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.mapper.TaskVersionMapper;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/9/8
 */
@Slf4j
@GrpcService
public class TaskRpcServiceImpl extends TaskRpcServiceGrpc.TaskRpcServiceImplBase {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskVersionMapper taskVersionMapper;
    @Autowired
    private TaskService taskService;

    @Override
    public void getTaskInfoByName(TaskServiceApi.NameRequest request, StreamObserver<TaskServiceApi.Response> responseObserver) {
        TaskServiceApi.Response.Builder responseBuilder = TaskServiceApi.Response.newBuilder();
        try {
            if (StringUtils.isBlank(request.getTaskName())) {
                throw new RuntimeException("任务名不能为空");
            }

            Task task = taskService.getByName(request.getTaskName());
            if (task == null) {
                throw new RuntimeException(String.format("任务不存在：%s", request.getTaskName()));
            }

            responseBuilder.setInfo(parseTaskToPb(task));
            responseBuilder.setCode(NumberUtils.INTEGER_ZERO);
        } catch (Exception e) {
            responseBuilder.setCode(NumberUtils.INTEGER_MINUS_ONE);
            responseBuilder.setMessage(ObjectUtils.defaultIfNull(e.getMessage(), "null"));
            log.error(e.getMessage(), e);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTaskInfoByID(TaskServiceApi.IDRequest request, StreamObserver<TaskServiceApi.TaskResponse> responseObserver) {
        TaskServiceApi.TaskResponse.Builder responseBuilder = TaskServiceApi.TaskResponse.newBuilder();
        InfTraceContextHolder.get().setUuid("INNER_SCHEDULE");
        try {
            if (CollectionUtils.isEmpty(request.getIdVersionPairList())) {
                throw new RuntimeException("需至少包含一个任务ID");
            }

            Example example = new Example(TaskVersion.class);
            example.or().andIn("taskId", request.getIdVersionPairList().stream()
                    .map(TaskServiceApi.IdVersionPair::getTaskID)
                    .collect(Collectors.toList()));

            List<TaskVersion> taskVersionList = taskVersionMapper.selectByExample(example);
            List<TaskVersion> destTaskList =request.getIdVersionPairList().stream()
                    .map(item -> taskVersionList.stream()
                            .filter(one -> one.getTaskId() == item.getTaskID())
                            .filter(one -> {
                                if (item.hasVersion()) {
                                    return one.getVersion() == item.getVersion();
                                }
                                return true;
                            })
                            .reduce((x, y) -> x.getVersion() > y.getVersion() ? x : y)
                            .orElseThrow(() -> new RuntimeException(String.format("任务不存在：taskId = %s, version = %s ",
                                    item.getTaskID(), item.getVersion())))).collect(Collectors.toList());

//            List<TaskVersion> destTaskList = new ArrayList<>();
//            for (TaskServiceApi.IdVersionPair item : request.getIdVersionPairList()) {
//                TaskVersion selectedTaskVersion = null;
//                for (TaskVersion one : taskVersionList) {
//                    if (one.getTaskId() == item.getTaskID()) {
//                        if (item.hasVersion()) {
//                            if (one.getVersion() == item.getVersion()) {
//                                selectedTaskVersion = one;
//                                break;
//                            }
//                        } else {
//                            selectedTaskVersion = one;
//                            break;
//                        }
//                    }
//                }
//                if (selectedTaskVersion != null) {
//                    destTaskList.add(selectedTaskVersion);
//                } else {
//                    throw new RuntimeException(String.format("任务不存在：taskId = %s, version = %s ",
//                            item.getTaskID(), item.getVersion()));
//                }
//            }


            log.info("current parallel is {}", ForkJoinPool.commonPool().getParallelism());
            responseBuilder.setCode(NumberUtils.INTEGER_ZERO);
            responseBuilder.addAllInfo(destTaskList.parallelStream().map(this::parseTaskToPb).collect(Collectors.toList()));
        } catch (Exception e) {
            responseBuilder.setCode(NumberUtils.INTEGER_MINUS_ONE);
            responseBuilder.setMessage(ObjectUtils.defaultIfNull(e.getMessage(), "null"));
            log.error(e.getMessage(), e);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private TaskServiceApi.TaskInfo parseTaskToPb(Task task) {
        ScheduleJobOuterClass.ScheduleJob scheduleJob = taskService.toScheduleJob(task, false);

        return TaskServiceApi.TaskInfo.newBuilder()
                .setId(task.getId())
                .setName(task.getName())
                .setIsSparkTask(!"PythonShell".equals(task.getTemplateCode()) && !"Hive2Redshift".equals(task.getTemplateCode()))
                .setTemplateCode(task.getTemplateCode())
                .setRuntimeConfig(task.getRuntimeConfig())
                .setUpdateTime(task.getUpdateTime().getTime())
                .setOwner(task.getCreateBy())
                .setIsDelete(task.getDeleteStatus() == DeleteEntity.DELETE.intValue())
                .addAllEventDepends(scheduleJob.getTaskCode().getEventDependList())
                .build();
    }

    private TaskServiceApi.TaskInfo parseTaskToPb(TaskVersion taskVersion) {
        Task originTask = taskMapper.selectByPrimaryKey(taskVersion.getTaskId());
        Task task = taskService.getTaskByVersion(taskVersion.getTaskId(), taskVersion.getVersion());

        ScheduleJobOuterClass.ScheduleJob scheduleJob = taskService.toScheduleJob(task, false);

        return TaskServiceApi.TaskInfo.newBuilder()
                .setId(task.getId())
                .setName(task.getName())
                .setIsSparkTask(!"PythonShell".equals(task.getTemplateCode()) && !"Hive2Redshift".equals(task.getTemplateCode()))
                .setTemplateCode(task.getTemplateCode())
                .setRuntimeConfig(task.getRuntimeConfig())
                .setUpdateTime(task.getUpdateTime().getTime())
                .setOwner(task.getCreateBy())
                .setIsDelete(originTask.getDeleteStatus() == DeleteEntity.DELETE.intValue())
                .addAllEventDepends(scheduleJob.getTaskCode().getEventDependList())
                .build();
    }
}
