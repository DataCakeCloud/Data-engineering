package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowMapper extends CrudMapper<Workflow> {
}
