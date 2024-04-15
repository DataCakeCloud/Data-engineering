package com.ushareit.dstask.validator.checker;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.validator.CheckFor;
import com.ushareit.dstask.validator.ItemChecker;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.impl.OutputValidator;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fengxiao
 * @date 2023/2/13
 */
@Slf4j
@Component
//@CheckFor(value = OutputValidator.class, desc = "校验生成数据集重复", mandatory = false, order = 200)
public class OutputDuplicatedChecker implements ItemChecker {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void checkImpl(Task task, TaskContext context) {
        if (TemplateEnum.valueOf(task.getTemplateCode()).isStreamingTemplate()) {
            return;
        }

        String id = task.getOutputGuids();
        log.info("task from web guid is " + id);
        List<Task> offlines = taskMapper.selectOfflineTaskWithSameOutputId(id);

        for (Task offline : offlines) {
            if (task.getId() != null && offline.getId().equals(task.getId())) {
                log.info("task id from web: " + task.getId());
                continue;
            }

            String outputDataset = offline.getOutputDataset();
            List<Dataset> outputs = JSON.parseArray(outputDataset, Dataset.class);
            if (outputs.stream().map(Dataset::getId).anyMatch(id::equalsIgnoreCase)) {
                throw new ServiceException(BaseResponseCodeEnum.TASK_OUTPUT_DATASET_ERROR.name(),
                        String.format(BaseResponseCodeEnum.TASK_OUTPUT_DATASET_ERROR.getMessage(), offline.getName()));
            }
        }
    }

}
