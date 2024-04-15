package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.TaskInstance;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface TaskInstanceMapper extends CrudMapper<TaskInstance> {
    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM task_instance " +
            "WHERE 1=1 " +
            "<if test='paramMap.taskId!=null'> AND task_id = #{paramMap.taskId} </if> " +
            "<if test='paramMap.name!=null'> AND name = #{paramMap.name} </if> " +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<TaskInstance> listByMap(@Param("paramMap") Map<String, String> paramMap);
    /**
     * 根据部署id和job状态查询
     *
     * @param stateList
     * @return
     */
    @Select({"<script>" +
            "SELECT * FROM task_instance " +
            "WHERE 1= 1 " +
            "<if test='taskId!=null'> AND task_id=#{taskId} </if>" +
            "<if test='stateList!=null'> AND status_code in " +
            "   <foreach collection='stateList' item='state' open='(' separator=',' close=')'>",
            "   #{state}",
            "   </foreach>" +
            "</if>" +
            "   order by create_time desc" +
            "</script>"})
    List<TaskInstance> queryByAppIdAndStatus(@Param("taskId") Integer taskId, @Param("stateList") List<String> stateList);

}
