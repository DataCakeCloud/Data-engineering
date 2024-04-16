package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Mapper
public interface ActorDefinitionMapper extends CrudMapper<ActorDefinition> {
}
