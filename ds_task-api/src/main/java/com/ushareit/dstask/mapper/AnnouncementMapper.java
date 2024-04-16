package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.Announcement;
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
public interface AnnouncementMapper extends CrudMapper<Announcement> {
    @Override
    @Select({"<script>" +
            "SELECT * from announcement " +
            "WHERE 1=1 and delete_status = 0 " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND  LOCATE(#{paramMap.name},name) &gt; 0  </if>" +
            "<if test='paramMap.online!=null and \"\" neq paramMap.online'> AND online = #{paramMap.online} </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<Announcement> listByMap(@Param("paramMap") Map<String, String> paramMap);

    @Select({"<script>" +
            "SELECT * from announcement " +
            "WHERE online = 1 and delete_status = 0 and date(create_time) >= #{date} " +
            " ORDER BY create_time DESC" +
            "</script>"})
    List<Announcement> limit(@Param("date") String date);
}
