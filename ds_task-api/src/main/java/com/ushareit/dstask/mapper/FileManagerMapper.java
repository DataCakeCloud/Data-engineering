package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.FileManager;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileManagerMapper extends CrudMapper<FileManager> {

    @Select({"SELECT * FROM file_manager WHERE NAME LIKE '%${name}%' AND MODULE = #{module} AND USER_GROUP = #{userGroup}"})
    List<FileManager> selectByNameLike(@Param("name") String name,@Param("module") String module,@Param("userGroup") String userGroup);

    @Select({"SELECT * FROM file_manager WHERE ENTITY_ID = #{entityId} AND MODULE = #{module}"})
    FileManager selectByEntityId(@Param("entityId") Integer entityId,@Param("module") String module);

    @Select({"SELECT * FROM file_manager WHERE PARENT_ID = #{id}"})
    List<FileManager> selectFolderSubset(Integer id);

    @Select({"SELECT * FROM file_manager WHERE MODULE = #{module} AND PARENT_ID = 0 AND USER_GROUP = #{userGroup}"})
    List<FileManager>  selectRootFolder(@Param("module") String module,@Param("userGroup") String userGroup);

    @Select({"SELECT * FROM file_manager WHERE ID = #{id}"})
    FileManager selectById(Integer id);

    @Select({"SELECT * FROM file_manager WHERE ID = #{id} for update"})
    FileManager selectByIdForUpdate(Integer id);

    @Update({"UPDATE file_manager A SET A.FILE_NUMS = A.FILE_NUMS + #{cnt} WHERE ID = #{parentId}"})
    void increaseFileNums(@Param("parentId") Integer parentId,@Param("cnt") Integer cnt);

    @Update({"UPDATE file_manager A SET A.FILE_NUMS = A.FILE_NUMS - #{cnt} WHERE ID = #{parentId}"})
    void decreaseFileNums(@Param("parentId") Integer parentId,@Param("cnt") Integer cnt);

    @Select({"SELECT PARENT_ID FROM file_manager WHERE ID = #{id}"})
    Integer selectParentId(Integer id);

    @Select({"SELECT * FROM file_manager WHERE PARENT_ID = #{id}"})
    List<FileManager> selectChildren(Integer id);

    @Select({"<script>",
            "SELECT * FROM file_manager",
            "WHERE ID IN",
            "<foreach collection='idList' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"})
    List<FileManager> selectByIdList(@Param("idList") List<Integer> idList);

    @Select({"SELECT * FROM file_manager WHERE MODULE = #{module}"})
    List<FileManager> selectByModule(String module);
}
