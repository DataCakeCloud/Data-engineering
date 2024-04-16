package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessTenantProduct;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/7
 */
@Mapper
public interface AccessTenantProductMapper extends CrudMapper<AccessTenantProduct> {
    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Select({"SELECT * FROM access_tenant_product WHERE tenant_id=#{tenantId}"})
    List<AccessTenantProduct> selectByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 根据name查询
     *
     * @param
     * @return
     */
    @Delete({"<script>" +
            "DELETE  FROM access_tenant_product  " +
            "WHERE tenant_id=#{tenantId}  " +
            "</script>"})
    int deleteByTenantId(@Param("tenantId") Integer tenantId);
}
