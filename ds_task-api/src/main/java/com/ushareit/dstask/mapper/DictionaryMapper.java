package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.Dictionary;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface DictionaryMapper extends CrudMapper<Dictionary> {

    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM dictionary " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.componentCode!=null and \"\" neq paramMap.componentCode'> AND component_code = #{paramMap.componentCode} </if>" +
            "<if test='paramMap.keyword!=null and \"\" neq paramMap.keyword'> AND ( LOCATE(#{paramMap.keyword},chinese_name) &gt; 0 OR LOCATE(#{paramMap.keyword},english_name) &gt; 0 OR LOCATE(#{paramMap.keyword},description) &gt; 0 )</if>" +
            "<if test='paramMap.createBy!=null and \"\" neq paramMap.createBy'> AND create_by = #{paramMap.createBy} </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<Dictionary> listByMap(@Param("paramMap") Map<String, String> paramMap);
}
