package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserGroupMapper extends CrudMapper<UserGroup> {
    @Select("select * from user_group where name=#{name} and delete_status=0")
    List<UserGroup> selectUserGroupByName(@Param("name") String name);

    @Select("select * from user_group where name=#{name} and id!=#{id} and delete_status=0 ")
    List<UserGroup> selectUserGroupByNameAndIdNot(@Param("name") String name,@Param("id") Integer id);

    @Select({"<script>" +
            "select * from user_group\n" +
            "        <where> \n" +
            "         delete_status=0\n" +
            "            <if test=\"paramMap.name!=null and paramMap.name!=''\">\n" +
            "                and name=#{paramMap.name}\n" +
            "            </if>\n" +
            "            <if test=\"paramMap.user_id!=null and paramMap.user_id!=''\">\n" +
            "                and id in (SELECT user_group_id from user_group_relation where user_id=#{paramMap.user_id} and `owner`=0)\n" +
            "            </if>\n" +
            "        </where> order by create_time desc "+
            "</script>"})
    Page<UserGroup> listByMap(@Param("paramMap") Map<String, String> paramMap);

    @Select("select * from user_group where id=#{id}")
    UserGroup selectUserGroupById(@Param("id") Integer id);

    @Select("select * from user_group where uuid=#{uuid}")
    UserGroup selectUserGroupByUuid(@Param("uuid") String uuid);

    @Select("select * from user_group where delete_status=0")
    UserGroup selectUserGroupByExists();
}
