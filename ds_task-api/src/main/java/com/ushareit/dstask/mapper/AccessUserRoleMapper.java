package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessUserRole;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Mapper
public interface AccessUserRoleMapper extends CrudMapper<AccessUserRole> {
    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT role_id FROM access_user_role WHERE user_id=#{userId}"})
    List<Integer> selectByUserId(@Param("userId") Integer userId);


    @Delete({"delete from access_user_role WHERE user_id=#{userId}"})
    int deleteByUserId(@Param("userId") Integer userId);

    @Select("select user_group_id from user_group_relation where user_id=#{userId} and owner=0 ")
    List<String> selectTeamowner(@Param("userId") Integer userId);

    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT user_id FROM access_user_role WHERE role_id=#{roleId}"})
    List<Integer> selectByRoleId(@Param("roleId") Integer roleId);

    @Delete({"<script>" +
            "Delete from access_user_role " +
            "WHERE user_id= #{userId} and role_id= #{roleId}" +
                    "</script>"})
    int deleteByRoleIdAndUserId(@Param("roleId") Integer roleId, @Param("userId") Integer userId);
}
