package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/7
 */
@Mapper
public interface AccessTenantMapper extends CrudMapper<AccessTenant> {
    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Override
    @Select({"SELECT * FROM access_tenant WHERE name=#{name} AND delete_status=0"})
    AccessTenant selectByName(@Param("name") String name);

    @Override
    @Select({"<script>" +
            "SELECT * FROM access_tenant " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.freeze!=null'> AND freeze_status = #{paramMap.freeze} </if> " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND LOCATE(#{paramMap.name},name) &gt; 0  </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<AccessTenant> listByMap(@Param("paramMap") Map<String, String> paramMap);
}
