package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Mapper
public interface ActorMapper extends CrudMapper<Actor> {
    @Select({"SELECT * FROM actor where JSON_EXTRACT(configuration,'$.catalog_config.database') = $database"})
    List<Actor> selectActorByDatabase(@Param("$database") String database);

    @Select({"SELECT * FROM actor where delete_status = 0 and ${where}"})
    List<Actor> selectActorByWhere(@Param("where") String where);


}
