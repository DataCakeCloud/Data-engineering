package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessRoleMenu;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Mapper
public interface AccessRoleMenuMapper extends CrudMapper<AccessRoleMenu> {
    /**
     *
     *
     * @param roleId
     */
    @Delete({"<script>" +
            "DELETE  FROM access_role_menu  " +
            "WHERE role_id=#{roleId}  " +
            "</script>"})
    void deleteByRoleId(@Param("roleId") Integer roleId);


    /**
     *
     *
     * @param roleId
     */
    @Select({"<script>" +
            "select * from access_role_menu  " +
            "WHERE role_id=#{roleId}  " +
            "</script>"})
    List<AccessRoleMenu> selectByRoleId(@Param("roleId") Integer roleId);
}
