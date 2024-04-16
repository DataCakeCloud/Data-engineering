package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessTenantRole;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Mapper
public interface AccessTenantRoleMapper extends CrudMapper<AccessTenantRole> {
    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT role_id FROM access_tenant_role WHERE tenant_id=#{tenantId}"})
    List<Integer> selectByTenantId(@Param("tenantId") Integer tenantId);


    @Select({"SELECT tenant_id FROM access_tenant_role WHERE role_id=#{roleId} limit 1"})
    Integer selectByRoleId(@Param("roleId") Integer roleId);
}
