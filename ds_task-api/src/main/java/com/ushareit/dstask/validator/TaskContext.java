package com.ushareit.dstask.validator;

import com.ushareit.dstask.bean.SparkParamRestrict;
import com.ushareit.dstask.bean.Task;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/2/2
 */
@Data
public class TaskContext {


    public Map<String, String> taskSparkParam;

    public HashMap<String, SparkParamRestrict> dbSparkParamRestrict;

    /**
     * 从数据库中取出的 task
     */
    private Task taskFromDB;
}
