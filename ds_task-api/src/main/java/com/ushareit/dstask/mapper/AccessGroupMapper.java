package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.AccessGroup;
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
public interface AccessGroupMapper extends CrudMapper<AccessGroup> {


    @Select({"<script>" +
            "SELECT * FROM access_group " +
            "WHERE delete_status=0  AND type = 0 " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND LOCATE(#{paramMap.name},name) &gt; 0 </if>" +
            "<if test='paramMap.user!=null and \"\" neq paramMap.user'> AND create_by=#{paramMap.user} </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<AccessGroup> listByMap(@Param("paramMap") Map<String, String> paramMap);


    @Select({"<script>" +
            "select * from access_group  where  delete_status = 0 and parent_id is null  " +
            "</script>"})
    List<AccessGroup> listRootGroup();

    @Select({"<script>" +
            "select * from access_group  where  delete_status = 0 and type = #{type}    " +
            "<if test='list!=null'> AND parent_id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "</script>"})
    List<AccessGroup> seletByParentIds(@Param("list") List<Integer> list, @Param("type") Integer type);

    @Select({"<script>" +
            "select * from access_group  where  delete_status = 0 and type = 1   " +
            "<if test='list!=null'> AND user_id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "</script>"})
    List<AccessGroup> selectByUserIds(@Param("list") List<Integer> list);

}
