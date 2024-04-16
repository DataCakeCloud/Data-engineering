package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.Artifact;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface ArtifactMapper extends CrudMapper<Artifact> {
    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Override
    @Select({"SELECT * FROM artifact WHERE name=#{name} AND delete_status=0"})
    Artifact selectByName(@Param("name") String name);

    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Select({"<script>" +
            "SELECT * FROM artifact " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.typeCode!=null and \"\" neq paramMap.typeCode'> AND type_code REGEXP REPLACE(#{paramMap.typeCode},',','|') </if> " +
            "<if test='paramMap.userGroupDetail!=null and \"\" neq paramMap.userGroupDetail'> AND user_group = #{paramMap.userGroupDetail} </if>" +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND LOCATE(#{paramMap.name},name) &gt; 0 </if>" +
            "<if test='paramMap.createBy!=null and \"\" neq paramMap.createBy'> AND create_by = #{paramMap.createBy}  </if> " +
            "<if test='paramMap.id!=null and \"\" neq paramMap.id'> AND id = #{paramMap.id}  </if> " +
            "<if test='paramMap.nonRoot!=null'> AND (is_public = #{paramMap.isPublic}   " +
            "<if test='list!=null'> OR create_by in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    " ) " +
                    "</if>" +
                    " ORDER BY update_time DESC" +
                    "</script>"})
    Page<Artifact> listByMap(@Param("list") List<String> list, @Param("paramMap") Map<String, String> paramMap);

}
