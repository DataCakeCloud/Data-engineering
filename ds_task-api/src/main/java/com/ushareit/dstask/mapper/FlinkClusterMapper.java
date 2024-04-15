package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.FlinkCluster;
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
public interface FlinkClusterMapper extends CrudMapper<FlinkCluster> {

    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM flink_cluster " +
            "WHERE delete_status=0 " +
            "<if test='paramMap.name!=null and \"\" neq paramMap.name'> AND LOCATE(#{paramMap.name},name) &gt; 0 </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    Page<FlinkCluster> listByMap(@Param("paramMap") Map<String, String> paramMap);

    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Override
    @Select({"SELECT * FROM flink_cluster WHERE name=#{name} and delete_status=0"})
    FlinkCluster selectByName(@Param("name") String name);

    @Select({"SELECT * FROM flink_cluster WHERE delete_status=0"})
    List<FlinkCluster> selectExist();
}
