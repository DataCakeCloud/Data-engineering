package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Mapper
public interface AccessUserMapper extends CrudMapper<AccessUser> {
    /**
     * 是否存在相同信息
     * @param accessUser
     * @return
     */
    @Select({"<script>" +
            "select * from access_user where email=#{vo.email} " +
            "</script>"})
    List<AccessUser> selectByEmail(@Param("vo") AccessUser accessUser);
    @Select({"<script>" +
            "select * from access_user where name=#{vo.name} " +
            "</script>"})
    List<AccessUser> selectByUserName(@Param("vo") AccessUser accessUser);

    @Select({"<script>" +
            "select au.* from access_user au \n" +
            "inner join \n" +
            "(\n" +
            "\tselect aug.user_id from access_user_group aug \n" +
            "\tinner join \n" +
            "\t\t(select id from access_group where delete_status = 0 " +
            "<if test='ids!=null'> AND id in " +
            "   <foreach collection='ids' item='id' open='(' separator=',' close=')) ag'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "\t on aug.group_id = ag.id\n" +
                    ") tmp \n" +
                    "on au.id = tmp.user_id; " +
                    "</script>"})
    List<AccessUser> selectByGroupIds(@Param("ids") List<Integer> ids);

    @Select({"<script>" +
            "select au.* from access_user au inner join (select * from access_user_role where role_id = #{roleId})aur on au.id = aur.user_id where au.delete_status = 0 " +
            "</script>"})
    List<AccessUser> selectByRoleId(@Param("roleId") Integer roleId);

    @Select({"<script>" +
            "select au.* from access_user au inner join (select * from access_user_group where group_id = #{groupId})aug on au.id = aug.user_id where au.delete_status = 0 and au.freeze_status = 0;" +
            "</script>"})
    List<AccessUser> selectByGroupId(@Param("groupId") Integer groupId);


    @Select({"<script>" +
            "select * from  access_user " +
            "WHERE tenant_id=#{tenantId} " +
            "</script>"})
    List<AccessUser> selectByTenantId(@Param("tenantId") Integer tenantId);


    @Override
    @Select({"<script>" +
            "select * from access_user " +
            "WHERE name=#{name} " +
            "AND delete_status=0 " +
            "</script>"})
    AccessUser selectByName(@Param("name") String name);


    @Override
    @Select({"<script>" +
            "SELECT * FROM access_user " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.freeze!=null'> AND freeze_status = #{paramMap.freeze} </if> " +
            "<if test='paramMap.tenantId!=null'> AND tenant_id = #{paramMap.tenantId} </if> " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND " +
            " ( LOCATE(#{paramMap.name},name) &gt; 0   or LOCATE(#{paramMap.name},email) &gt; 0 )  </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<AccessUser> listByMap(@Param("paramMap") Map<String, String> paramMap);


    @Select({"<script>" +
            "select * from  access_user " +
            "WHERE 1=1 AND tenant_id =#{tenantId} AND delete_status =0  " +
            "<if test='name!=null and \"\" neq name'> AND LOCATE(#{name},name) &gt; 0 </if>" +
            "</script>"})
    List<AccessUser> selectByNames(@Param("name") String name, @Param("tenantId") Integer tenantId);

    @Select({"<script>" +
            "select name from access_user where id in (" +
            "    select user_id from access_role_menu menu " +
            "        join access_user_role role " +
            "    where menu.menu_id=110000 and menu.role_id=role.role_id " +
            ")" +
            "</script>"})
    List<String> selectAdmin();
}
