package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.SysDict;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface SysDictMapper extends CrudMapper<SysDict> {
    /**
     * parentCode
     *
     * @param parentCode
     * @return
     */
    @Select({"SELECT * FROM sys_dict WHERE status = 1 AND parent_code=#{parentCode} order by id"})
    List<SysDict> selectByParentCode(String parentCode);

    @Select({"<script>" +
            "select code,value,source from sys_dict " +
            "where status=1 " +
            " AND parent_code='INTERFACE_MAPPING' " +
            "<if test='uri!=null and \"\" neq uri'> AND code like concat('%', #{uri}, '%') </if>" +
            " order by code asc " +
            "</script>"})
    List<SysDict> selectCodeValue(@Param("uri") String uri);

    @Select({"<script>" +
            "select code,value,source from sys_dict " +
            "where status=1 " +
            " AND parent_code='INTERFACE_MAPPING' " +
            "<if test='source!=null and \"\" neq source'> AND source=#{source} </if>" +
            "<if test='name!=null and \"\" neq name'> AND value like concat('%', #{name}, '%') </if>" +
            " order by code asc " +
            "</script>"})
    List<SysDict> selectSource(@Param("source") String source, @Param("name") String name);
}
