package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.AccessRole;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Mapper
public interface AccessRoleMapper extends CrudMapper<AccessRole> {
    @Override
    @Select({"<script>" +
            "SELECT * FROM access_role " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND LOCATE(#{paramMap.name},name) &gt; 0 </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<AccessRole> listByMap(@Param("paramMap") Map<String, String> paramMap);

    /**
     * 通过用户名查找角色列表，用户名重复有问题
     * @param userName
     * @return
     */
    @Select({"<script>" +
            "select ar.* from access_role ar inner join (select aur.* from access_user_role aur inner join (select id from access_user where name = #{userName})tmp on aur.user_id = tmp.id)tmp2 on ar.id = tmp2.role_id where ar.delete_status  = 0;" +
            "</script>"})
    List<AccessRole> selectByUserName(@Param("userName") String userName);

    @Select({"<script>" +
            "select ar.* from access_role ar inner join (select aur.* from access_user_role aur inner join (select id from access_user where id = #{userId})tmp on aur.user_id = tmp.id)tmp2 on ar.id = tmp2.role_id where ar.delete_status  = 0;" +
            "</script>"})
    List<AccessRole> selectByUserId(@Param("userId") Integer userId);

    @Select({"select a.* from access_role a,access_tenant_role b where a.id = b.role_id and b.tenant_id = #{id} and a.name='common'"})
    AccessRole getCommonRoleId(@Param("id") Integer id);

    @Override
    @Select({"SELECT * FROM access_role WHERE name=#{name} AND delete_status=0"})
    AccessRole selectByName(@Param("name") String name);
}
