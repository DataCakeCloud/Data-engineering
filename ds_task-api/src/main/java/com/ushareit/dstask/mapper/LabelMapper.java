package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Label;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2021/9/22
 */
@Mapper
public interface LabelMapper extends CrudMapper<Label> {
    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Override
    @Select({"SELECT * FROM label WHERE name=#{name} AND delete_status=0"})
    Label selectByName(@Param("name") String name);


    /**
     * 根据name查询
     *
     * @param id id
     * @return
     */
    @Select({"SELECT * FROM label WHERE id=#{id} AND delete_status=0"})
    Label getById(@Param("id") Integer id);


    @Override
    @Select({"<script>" +
            "SELECT * FROM label " +
            "WHERE delete_status=0 " +
            "<if test='label.name!=null and \"\" neq label.name'> AND name REGEXP REPLACE(#{label.name},',','|') </if>" +
            "<if test='label.createBy!=null and \"\" neq label.createBy'> AND create_by = #{label.createBy} </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    List<Label> select(@Param("label") Label label);

    @Select({"<script>" +
            "SELECT * FROM label " +
            "WHERE delete_status=0 " +
            "<if test='label.name!=null and \"\" neq label.name'> AND name REGEXP REPLACE(#{label.name},',','|') </if>" +
            "<if test='label.createBy!=null and \"\" neq label.createBy'> AND create_by &lt;&gt; #{label.createBy} </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    List<Label> selectOthers(@Param("label") Label label);


    @Select({"SELECT date_format(create_time, '%Y-%m-%d') as `time`, count(id) as num FROM label WHERE delete_status=0 and date_format(create_time, '%Y-%m-%d') < date_format(#{time}, '%Y-%m-%d') group by  date_format(create_time, '%Y-%m-%d')"})
    List<Map<String, Integer>> count(@Param("time") Timestamp time);
}
