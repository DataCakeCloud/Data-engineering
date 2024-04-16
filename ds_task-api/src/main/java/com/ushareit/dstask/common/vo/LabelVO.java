package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Label;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.SymbolEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
@Data
public class LabelVO {

    private String labelName;
    private List<TaskVO> taskList = new ArrayList<>();

    public LabelVO(String tagName, List<Task> taskList) {
        this.labelName = tagName;
        this.taskList = taskList.stream().map(TaskVO::new).collect(Collectors.toList());
    }

    public LabelVO(Label label, Map<Integer, Task> taskMap) {
        this.labelName = label.getName();

        if (StringUtils.isBlank(label.getTasks())) {
            return;
        }

        this.taskList = Arrays.stream(label.getTasks().split(SymbolEnum.COMMA.getSymbol()))
                .filter(item -> taskMap.containsKey(Integer.parseInt(item)))
                .map(item -> new TaskVO(taskMap.get(Integer.parseInt(item))))
                .collect(Collectors.toList());
    }
}
