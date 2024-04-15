package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskSnapshotMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.FlinkClusterService;
import com.ushareit.dstask.service.TaskInstanceService;
import com.ushareit.dstask.service.TaskSnapshotService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.FlinkApiUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class TaskSnapshotServiceImpl extends AbstractBaseServiceImpl<TaskSnapshot> implements TaskSnapshotService {

    @Resource
    private TaskSnapshotMapper taskSnapshotMapper;

    @Resource
    private TaskInstanceService taskInstanceService;

    @Resource
    private FlinkClusterService flinkClusterService;

    @Resource
    private CloudFactory cloudFactory;

    @Override
    public CrudMapper<TaskSnapshot> getBaseMapper() {
        return taskSnapshotMapper;
    }


    @Override
    public void trigger(TaskSnapshot taskSnapshot) {
        if (!match(taskSnapshot.getName(), DsTaskConstant.CLUSTER_NAME_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.NAME_NOT_MATCH);
        }

        TaskInstance taskInstance = taskInstanceService.getAliveJobs(taskSnapshot.getTaskId());
        String url = FlinkApiUtil.triggerSavepoint(taskInstance.getServiceAddress(), taskInstance.getEngineInstanceId());
        String params = getCancelWithSavepointParams();

        triggerAndGetResult(url, params, taskInstance, taskSnapshot, DsTaskConstant.SAVEPOINT);
    }

    private void triggerAndGetResult(String url, String params, TaskInstance taskInstance,TaskSnapshot taskSnapshot, String triggerKind) {
        BaseResponse response = HttpUtil.postWithJson(url, params);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.JOB_TRIGGER_SAVEPOINT_FAIL, "向flink集群中的job触发保存点失败,失败原因:" + response.getData());
        }

        //触发保存点成功，还要看查询保存点结果
        String requestId = response.get().getString("request-id");
        //查询保存点结果
        String querySavepointUrl = FlinkApiUtil.getSavepointResult(taskInstance.getServiceAddress(), taskInstance.getEngineInstanceId(), requestId);
        log.info("triggerAndGetResult querySavepointUrl:" + querySavepointUrl);
        String savepointResult = querySavepointResult(querySavepointUrl);
        log.info("triggerAndGetResult savepointResult:" + savepointResult);
        taskSnapshot.setTriggerKind(triggerKind)
                .setUrl(savepointResult)
                .setCreateBy(InfTraceContextHolder.get().getUserName());
        //无Name，以日期填充
        if (!StringUtils.isNotBlank(taskSnapshot.getName())) {
            taskSnapshot.setName(savepointResult.substring(savepointResult.lastIndexOf("/") + 1));
        }
        super.save(taskSnapshot);
    }

    /**
     * 查询保存点执行结果，直到查询返回COMPLETED为止
     *
     * @param url
     * @return
     */
    private String querySavepointResult(String url) {
        log.info(String.format("查询保存点执行结果的URL:%s", url));
        while (true) {
            BaseResponse response = HttpUtil.get(url);
            if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
                throw new ServiceException(BaseResponseCodeEnum.JOB_TRIGGER_SAVEPOINT_FAIL, "向flink集群中的job触发保存点失败,失败原因:" + response.getData());
            }

            JSONObject result = response.get();
            String status = result.getJSONObject("status").getString("id");

            if (DsTaskConstant.SAVEPOINT_STATUS_COMPLETED.equals(status)) {
                String location = result.getJSONObject("operation").getString("location");
                if (StringUtils.isBlank(location)) {
                    throw new ServiceException(BaseResponseCodeEnum.JOB_TRIGGER_SAVEPOINT_FAIL);
                }
                return location;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 组装手动触发保存点的body参数
     *
     * @return
     */
    private static String getCancelWithSavepointParams() {
        JSONObject o = new JSONObject();
        o.put("cancel-job", false);
        return o.toString();
    }

    @Override
    public void stopWithSavepoint(TaskInstance taskInstance) {
        String url = FlinkApiUtil.stopWithSavepoint(taskInstance.getServiceAddress(), taskInstance.getEngineInstanceId());
        String params = getStopWithSavepointParams();
        triggerAndGetResult(url, params, taskInstance, new TaskSnapshot().setTaskId(taskInstance.getTaskId()), DsTaskConstant.SAVEPOINT);
    }

    /**
     * 组装停止并触发保存点的body参数
     *
     * @return
     */
    private String getStopWithSavepointParams() {
        JSONObject o = new JSONObject();
        o.put("drain", true);
        return o.toString();
    }

    @Override
    public Map<String, Object> list(TaskSnapshot taskSnapshot) {
//        List<TaskSnapshot> checkpoint = saveCheckpoint(taskSnapshot.getTaskId());
        List<TaskSnapshot> checkpoints = saveCheckpoints(taskSnapshot.getTaskId());
        log.info("查询savepoints");
        taskSnapshot.setTriggerKind(DsTaskConstant.SAVEPOINT);
        List<TaskSnapshot> savepoints = taskSnapshotMapper.select(taskSnapshot);
        Collections.reverse(savepoints);
        log.info("开始组装结果");
        HashMap<String, Object> result = new HashMap<>(2);
        result.put("checkpoints", checkpoints);
        result.put("savepoints", savepoints);

        return result;
    }

    @Override
    public ObsS3Object getLatestCheckpoint(Integer taskid){
        if (taskid == null) {
            return null;
        }
        TaskInstance taskInstance = taskInstanceService.getLatestJobByTaskId(taskid);
        if (taskInstance == null){
            return  null;
        }

        FlinkCluster flinkCluster = flinkClusterService.getById(taskInstance.getClusterId());
        if (StringUtils.isEmpty(flinkCluster.getStatePath())) {
            log.info("jobId:" + taskid + "对应的集群id:" + taskInstance.getClusterId() + "无StatePath");
            return null;
        }

        String env = InfTraceContextHolder.get().getEnv();
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(flinkCluster.getRegion());
        ObsS3Object obsS3Object = cloudClientUtil.listObject(taskid, taskInstance.getEngineInstanceId(), flinkCluster.getStatePath(), env,flinkCluster.getRegion());
        return obsS3Object;
    }

    @Override
    public List<TaskSnapshot> getCheckpoints(Integer taskid){
        if (taskid == null) {
            return null;
        }
        TaskInstance taskInstance = taskInstanceService.getLatestJobByTaskId(taskid);
        if (taskInstance == null){
            return  null;
        }

        FlinkCluster flinkCluster = flinkClusterService.getById(taskInstance.getClusterId());
        if (StringUtils.isEmpty(flinkCluster.getStatePath())) {
            log.info("jobId:" + taskid + "对应的集群id:" + taskInstance.getClusterId() + "无StatePath");
            return null;
        }

        String env = flinkCluster.getEnv();
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(flinkCluster.getRegion());
        List<ObsS3Object> obsS3Objects = cloudClientUtil.listObjects(taskid, taskInstance.getEngineInstanceId(), flinkCluster.getStatePath(), env,flinkCluster.getRegion());

        List<TaskSnapshot> checkpoints = obsS3Objects.stream().map(o -> {
            TaskSnapshot snapshot = TaskSnapshot.convert(o);
            snapshot.setTaskId(taskid).setTriggerKind(DsTaskConstant.CHECKPOINT).setDeleteStatus(0);
            return snapshot;
        }).collect(Collectors.toList());
        return checkpoints;
    }

    /**
     * 存最新的checkpoint
     *
     * @param taskid
     */
    private List<TaskSnapshot> saveCheckpoint(Integer taskid) {
        log.info("正在查询最新的checkpoint");
        ArrayList<TaskSnapshot> list = new ArrayList<>(1);
        ObsS3Object obsS3Object = getLatestCheckpoint(taskid);
        if (obsS3Object == null || StringUtils.isBlank(obsS3Object.getPath())) {
            log.info("最新的checkpoint路径为空");
            return list;
        }

        String checkpointName = obsS3Object.getPath().substring(obsS3Object.getPath().lastIndexOf("/") + 1);

        TaskSnapshot checkpoint = new TaskSnapshot()
                .setTaskId(taskid)
                .setTriggerKind(DsTaskConstant.CHECKPOINT);

        TaskSnapshot checkpointFromDb = taskSnapshotMapper.selectOne(checkpoint);


        if (checkpointFromDb == null) {
            checkpoint.setName(checkpointName)
                    .setUrl(obsS3Object.getPath())
                    .setCreateTime(new Timestamp(obsS3Object.getLastModified().getTime()))
                    .setCreateBy("monitor");
            super.save(checkpoint);
            list.add(checkpoint);
            return list;
        } else {
            checkpointFromDb.setName(checkpointName)
                    .setUrl(obsS3Object.getPath())
                    .setCreateTime(new Timestamp(obsS3Object.getLastModified().getTime()));
            super.update(checkpointFromDb);
            list.add(checkpointFromDb);
            return list;
        }
    }

    private List<TaskSnapshot> saveCheckpoints(Integer taskid) {
        log.info("正在查询最新的checkpoint");
        List<TaskSnapshot> checkpoints = getCheckpoints(taskid);
        taskSnapshotMapper.updateCheckpoints(taskid);
        if (checkpoints == null || checkpoints.size() == 0) {
            return new ArrayList<>();
        }
        taskSnapshotMapper.insertList(checkpoints);

        //这里应该查出所有的
        TaskSnapshot taskSnapshot = new TaskSnapshot();
        taskSnapshot.setTaskId(taskid).setTriggerKind("CHECKPOINT").setDeleteStatus(0);
        return taskSnapshotMapper.select(taskSnapshot);
    }
}
