package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.common.vo.UserGroupInfoVo;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserGroupRelationMapper extends CrudMapper<UserGroupRelation> {
    @Select("select * from user_group_relation where user_id=#{vo.userId} and user_group_id=#{vo.userGroupId}")
    List<UserGroupRelation> selectByUserIdAndGroupId(@Param("vo") UserGroupRelation userGroupRelation);

    @Select("select g.name from user_group_relation u left join user_group g on u.user_group_id= g.id where u.user_id=#{userId} and g.delete_status=0")
    List<String> selectByUserId(@Param("userId") Integer userId);

    @Select("select * from user_group_relation where user_name=#{name}")
    List<UserGroupRelation> selectByUserName(@Param("name") String name);

    @Select({"<script>" +
            "select r.owner, u.id, u.name, u.uuid ,u.default_hive_db_name  from user_group_relation r join user_group u\n" +
            "where r.user_id=#{userId} and r.user_group_id=u.id and u.delete_status=0" +
            "</script>"})
    List<UserGroupInfoVo> selectGroupByUseId(@Param("userId") Integer userId);

    @Select({"<script>" +
            "select user_name from user_group_relation where user_group_id=#{userGroupId} and owner=0" +
            "</script>"})
    List<String> selectOwner(@Param("userGroupId") Integer userGroupId);
}
