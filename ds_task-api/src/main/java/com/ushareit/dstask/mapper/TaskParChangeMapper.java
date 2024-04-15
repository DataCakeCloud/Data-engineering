package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.TaskParChange;
import com.ushareit.dstask.bean.TaskScaleStrategy;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author: xuebotao
 * @create: 2022-01-12
 */
@Mapper
public interface TaskParChangeMapper extends CrudMapper<TaskParChange> {


}
