package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.TaskInstance;
import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author: xuebotao
 * @create: 2022-01-04
 */
@Mapper
public interface TaskScaleStrategyMapper extends CrudMapper<TaskScaleStrategy> {

    /**
     * 根据taskId查询
     */
    @Select({"select * from task_scale_strategy where 1=1 AND task_id=#{taskId}  AND  delete_status=0 "})
    List<TaskScaleStrategy> queryByTaskId(@Param("taskId") Integer taskId);

}
