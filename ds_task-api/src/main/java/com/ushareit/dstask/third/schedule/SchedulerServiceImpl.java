package com.ushareit.dstask.third.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.xpath.operations.Bool;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 调用调度层的API
 */
@Slf4j
@Service
public class SchedulerServiceImpl {
    @GrpcClient("pipeline-server")
    private static TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;


    public String render(String sql, String executionDate, String taskName){
        TaskSchedulerApi.TaskCommonResponse taskRenderInfo = taskSchedulerRpcApiBlockingStub.getTaskRenderInfo(
                TaskSchedulerApi
                        .RenderRequest
                        .newBuilder()
                        .setContent(sql)
                        .setTaskName(taskName)
                        .setExecutionDate(executionDate)
                        .build()
        );
        if (taskRenderInfo == null) {
            log.error(String.format("[%s]failed to pipeline render sql: %s", taskName, "pipeline service exception"));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, "pipeline service exception");
        }
        Integer code = taskRenderInfo.getCode();
        if (code != 0) {
            log.error(String.format("[%s]failed to pipeline render sql[code: %d]: %s", taskName, code, taskRenderInfo.getData()));
            throw new ServiceException(BaseResponseCodeEnum.SPARKSQL_JINJA_DATA_ERR, String.format("failed to render sql: %s", taskRenderInfo.getMessage()));
        }
        return taskRenderInfo.getData();
    }

    public String dateTransform(String content,boolean is_data_trigger,String cycle,String airflowCrontab,boolean is_sql){
        // 入参日期取当前日期
        if(content == null){
            content = "";
        }
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.dateTransform(
                TaskSchedulerApi.DateTransformRequest
                        .newBuilder()
                        .setAirflowContent(content)
                        .setIsDataTrigger(is_data_trigger)
                        .setAirflowCrontab(airflowCrontab)
                        .setTaskGra(cycle)
                        .setIsSql(is_sql)
                        .build()
        );

        if (taskCommonResponse == null) {
            log.error(String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
        }
        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("Sql render failed ,code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR.name(), String.format("error: %s", msg));
        }
        return taskCommonResponse.getData();
    }

    public JSONObject getDatasetInfo(String taskName){
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.datasetInfo(
                TaskSchedulerApi.DataInfoRequest
                        .newBuilder()
                        .setName(taskName)
                        .build()
        );
        if (taskCommonResponse == null) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
        }
        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }
        JSONObject respData = JSON.parseObject(taskCommonResponse.getData());
        return  respData;
    }

    public JSONObject getDatePreview(String taskGra, String taskCrontab, String dataDepend, String taskDepend){
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.datePreview(
                TaskSchedulerApi.DatePreviewRequest
                        .newBuilder()
                        .setTaskGra(taskGra)
                        .setDataDepend(dataDepend)
                        .setTaskDepend(taskDepend)
                        .setTaskCrontab(taskCrontab)
                        .build()
        );

        Integer code = taskCommonResponse.getCode();
        if (code != 0) {
            String msg = taskCommonResponse.getMessage();
            log.error(String.format("code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }
        JSONObject respData = JSON.parseObject(taskCommonResponse.getData());
        return  respData;
    }

    public String parseJinja(String sql) {
        // sql包含了jinja格式的日期参数,需要先做一次替换,调用调度层的render接口
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return render(sql, df.format(new Date()),"");
    }
    public Boolean isIrregularSheduler(Task task){
        String triggerParamString = task.getTriggerParam();
        TriggerParam triggerParam = JSON.parseObject(triggerParamString, TriggerParam.class);
        Integer isIrregularSheduler = triggerParam.getIsIrregularSheduler();
        return isIrregularSheduler == 2;
    }

    public String hasCycleParam(String content){
        String param = "";
        String regex = "\\{\\{\\s*(prev_execution_date|prev_2_execution_date|prev_execution_date_utc0|prev_2_execution_date_utc0|next_execution_date|next_execution_date_utc0)";
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(content);
        if (matcher.find()){
            param = matcher.group(1);
        }
        return param;
    }

}
