package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.TaskFolder;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskFolderMapper extends CrudMapper<TaskFolder> {
    @Select("select * from task_folder where name=#{vo.name} and parent_id=#{vo.parentId} and uuid=#{vo.uuid} ")
    List<TaskFolder> selectByNameAndParentId(@Param("vo") TaskFolder taskFolder);
}
