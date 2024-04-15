package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessMenu;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Mapper
public interface AccessMenuMapper extends CrudMapper<AccessMenu> {
    @Select({"<script>" +
            "SELECT * FROM access_menu where delete_status=0 and parent_menu_id = #{id}" +
            "</script>"})
    List<AccessMenu> selectByParentId(@Param("id") Integer id);

    @Select("<script>SELECT DISTINCT(m.menu_id) from access_user_role u ,access_role_menu m where u.user_id=#{userId} and m.menu_id in(110000,110001) and u.role_id=m.role_id</script>")
    List<Integer> existAdminAndSupperAdminRole(Integer userId);

    @Select({"<script>" +
            "select am.* from access_menu am inner join (select arm.menu_id from access_role_menu arm inner join (select ar.* from access_role ar inner join (select * from access_user_role where user_id = #{userId}) aur on ar.id  = aur.role_id where ar.delete_status = 0\n" +
            ")tmp on arm.role_id = tmp.id)tmp2 on am.id = tmp2.menu_id where am.delete_status = 0 and am.valid = 0;" +
            "</script>"})
    List<AccessMenu> selectByUserId(@Param("userId") Integer userId);

    /**
     * 通过角色id找到租户，再找到产品，再找到菜单
     * 通过角色id找到这个角色所拥有的所有菜单的权限，注意不是找到这个角色选择了哪些菜单
     * @param roleId
     * @return
     */
    @Select({"<script>" +
            "select distinct am.* from access_menu am inner join (select atp.* from access_tenant_product atp inner join (select * from access_tenant_role where role_id = #{roleId})tmp on atp.tenant_id = tmp.tenant_id)tmp2 on am.product_id = tmp2.product_id where am.delete_status = 0" +
            "</script>"})
    List<AccessMenu> selectByRoleId(@Param("roleId") Integer roleId);

    /**
     * 通过角色找到这个角色选择了哪些菜单
     * @param roleId
     * @return
     */
    @Select({"<script>" +
            "select distinct am.* from access_menu am inner join (select * from access_role_menu where role_id = #{roleId})tmp on am.id = tmp.menu_id where am.delete_status = 0 and valid = 0" +
            "</script>"})
    List<AccessMenu> selectCheckedMenusByRoleId(@Param("roleId") Integer roleId);


    @Select({"<script>" +
            "select * from access_menu  where  delete_status = 0  " +
            "<if test='list!=null'> AND url in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "</script>"})
    List<AccessMenu> selectByUrl(@Param("list") List<String> list);
}
