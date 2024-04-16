package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.TaskVersion;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface TaskVersionMapper extends CrudMapper<TaskVersion> {

    /**
     * 根据应用ID查询最大版本号
     *
     * @param id
     * @return
     */
    @Select("select max(version) from task_version where task_id=#{id} ")
    Integer getMaxVersionById(@Param("id") Integer id);

    @Select({"<script>" +
            "SELECT * FROM task_version " +
            "WHERE 1=1 " +
            "<if test='ids!=null'> AND task_id in " +
            "   <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
            "</if>" +
            "</script>"})
    List<TaskVersion> getTaskVersionByIds(@Param("ids") List<Integer> ids);
}
