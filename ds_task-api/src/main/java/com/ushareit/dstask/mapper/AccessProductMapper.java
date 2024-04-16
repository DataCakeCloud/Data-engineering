package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessProduct;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/13
 */
@Mapper
public interface AccessProductMapper extends CrudMapper<AccessProduct> {
    /**
     * 通过租户找到产品
     * @param tenantId
     * @return
     */
    @Select({"<script>" +
            "select ap.* from access_product ap inner join (select * from access_tenant_product where tenant_id = #{tenantId})tmp on ap.id = tmp.product_id" +
            "</script>"})
    List<AccessProduct> selectByTenantId(@Param("tenantId") Integer tenantId);
}
