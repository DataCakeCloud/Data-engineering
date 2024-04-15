package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.WorkflowTask;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowTaskMapper extends CrudMapper<WorkflowTask> {

}
