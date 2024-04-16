package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessUserGroup;
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
public interface AccessUserGroupMapper extends CrudMapper<AccessUserGroup> {
    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT role_id FROM access_user_role WHERE user_id=#{userId}"})
    List<Integer> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT user_id FROM access_user_group WHERE group_id=#{groupId}"})
    List<Integer> selectByGroupId(@Param("groupId") Integer groupId);


    @Delete({"<script>" +
            "Delete from access_user_group " +
            "WHERE user_id= #{userId} and group_id= #{groupId}" +
                    "</script>"})
    int deleteByGroupIdAndUserId(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

}
