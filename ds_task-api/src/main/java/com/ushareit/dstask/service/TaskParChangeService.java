package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.TaskParChange;

import java.util.List;

/**
 * @author: xuebotao
 * @create: 2022-01-04
 */
public interface TaskParChangeService extends BaseService<TaskParChange> {

    void insertTaskParChange(TaskParChange taskParChange);

}
