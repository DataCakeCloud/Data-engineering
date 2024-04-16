package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author fengxiao
 * @date 2022/11/11
 */
@Mapper
public interface WorkflowVersionMapper extends CrudMapper<WorkflowVersion> {
}
