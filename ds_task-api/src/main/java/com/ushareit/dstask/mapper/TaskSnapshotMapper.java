package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.TaskSnapshot;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.*;

import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface TaskSnapshotMapper extends CrudMapper<TaskSnapshot> {

    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM task_snapshot " +
            "WHERE 1=1 " +
            "<if test='paramMap.taskId!=null'> AND task_id = #{paramMap.taskId} </if> " +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<TaskSnapshot> listByMap(@Param("paramMap") Map<String, String> paramMap);

    @Delete({"<script>" +
            "DELETE  FROM task_snapshot  " +
            "WHERE task_id=#{taskId} and trigger_kind = 'CHECKPOINT' " +
            "</script>"})
    void deleteCheckpointsByTaskId(@Param("taskId") Integer taskId);

    @Update({"<script>" +
            "update task_snapshot set delete_status=1 where task_id=#{taskId} " +
            " AND trigger_kind = 'CHECKPOINT'" +
            " AND create_time &lt;  DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
            "</script>"})
    void updateCheckpoints(@Param("taskId") Integer taskId);
}
