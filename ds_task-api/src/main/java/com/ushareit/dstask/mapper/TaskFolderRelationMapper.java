package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.TaskFolderRelation;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskFolderRelationMapper extends CrudMapper<TaskFolderRelation> {
    @Select("select task_id from task_folder_relation  where task_folder_id=#{folderId}")
    List<Integer> selectTaskIdByFolderId(@Param("folderId") Integer folderId);

    @Select("select * from  task_folder_relation where task_folder_id=#{folderId}")
    List<TaskFolderRelation> selectByFolderId(@Param("folderId") Integer folderId);

    @Select("<script>select task_id taskId from  task_folder_relation"+
            "        where " +
            "        task_folder_id IN " +
            "        <foreach collection=\"list\" item=\"id\" index=\"index\" " +
            "                 separator=\",\" open=\"(\" close=\")\"> " +
            "            #{id} " +
            "        </foreach>"
            +"</script>")
    List<Integer> selectByFolderIds(@Param("list") List<String> ids);

    @Delete("<script>delete from  task_folder_relation " +
            "        where " +
            "        task_folder_id IN " +
            "        <foreach collection=\"list\" item=\"id\" index=\"index\" " +
            "                 separator=\",\" open=\"(\" close=\")\"> " +
            "            #{id} " +
            "        </foreach>" +
            "</script>")
    void deleteByFolderIds(@Param("list") List<String> ids);
}
