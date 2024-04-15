package com.ushareit.dstask.web.api;

import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.common.vo.TaskInfoVO;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/9/13
 */
@Lazy
@RestController
@RequestMapping("api/task")
public class TaskApiController {

    @Autowired
    private TaskMapper taskMapper;

    @GetMapping("info")
    public BaseResponse<TaskInfoVO> getStatistic(@RequestParam("name") String taskName) {
        Example example = new Example(Task.class);
        example.or()
                .andEqualTo("name", taskName)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<Task> taskList = taskMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), String.format("任务不存在：%s", taskName));
        }

        return BaseResponse.success(new TaskInfoVO(taskList.get(NumberUtils.INTEGER_ZERO)));
    }
}
