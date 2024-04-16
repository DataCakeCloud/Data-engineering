package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.ActorCatalog;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author fengxiao
 * @date 2022/7/27
 */
@Mapper
public interface ActorCatalogMapper extends CrudMapper<ActorCatalog> {
}
