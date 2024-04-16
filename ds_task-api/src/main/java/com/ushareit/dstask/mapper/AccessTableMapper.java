package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccessTable;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tianxu
 * @date 2023/12/14
 **/
@Mapper
public interface AccessTableMapper extends CrudMapper<AccessTable> {
    /**
     * 存入用户组下每张表每天的访问次数
     */
    @Insert({"<script>" +
            "insert into access_table(table_name, database_name, count, user_group, stat_date) " +
            "values" +
            " <foreach collection='info' item='access' separator=','>" +
            "   (#{access.tableName}, #{access.databaseName}, #{access.count}, #{access.userGroup}, #{stat_date})" +
            " </foreach>" +
            "</script>"})
    void insertAccessTable(@Param("info") List<AccessTable> info, @Param("stat_date") String stat_date);
}
