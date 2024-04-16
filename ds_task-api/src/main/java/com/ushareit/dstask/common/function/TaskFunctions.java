package com.ushareit.dstask.common.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author fengxiao
 * @date 2022/12/7
 */
public class TaskFunctions {

    public static Function<Integer, Task> idToTask(TaskService taskService) {
        return taskService::getById;
    }

    public static Function<Task, String> entityToGranularity() {
        return t -> {
            TriggerParam triggerParam = JSONObject.parseObject(t.getTriggerParam(), TriggerParam.class);
            return triggerParam == null ? null : triggerParam.getOutputGranularity();
        };
    }

    public static Function<Task, List<Dataset>> entityToDatasets() {
        return t -> {
            if (StringUtils.isBlank(t.getOutputDataset())) {
                return Collections.emptyList();
            }

            return JSON.parseArray(t.getOutputDataset(), Dataset.class);
        };
    }

}
