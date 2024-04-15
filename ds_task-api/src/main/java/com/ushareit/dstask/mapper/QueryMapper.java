package com.ushareit.dstask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.bean.qe.PersonQuery;
import com.ushareit.dstask.bean.qe.QueryTop;
import com.ushareit.dstask.bean.qe.TaskAndScan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("query")
public interface QueryMapper {
    @Select({"<script>" +
            "select\n" +
            "a.id,\n" +
            "a.uuid,\n" +
            "a.create_time,\n" +
            "a.execute_duration,\n" +
            "a.processed_bytes \n" +
            "from\n" +
            "  (\n" +
            "    select\n" +
            "      id,\n" +
            "      uuid,\n" +
            "      query_sql,\n" +
            "      create_time,\n" +
            "      left(statusZh, 3) as status,\n" +
            "      statusZh,\n" +
            "      create_by,\n" +
            "      execute_duration,\n" +
            "      processed_bytes\n" +
            "    from\n" +
            "      query_history\n" +
            "    <where>\n" +
            "      create_time &lt; #{startTime}\n" +
            "      and create_time &gt;= #{endTime}\n" +
            "     <if test='userGroup!=null'> and user_group = #{userGroup} </if> " +
            "     and status =0 </where>\n" +
            "  )a\n" +
            " order by " +
            "     <if test='orderColumn==\"execute_duration\"'> a.execute_duration desc </if> \n" +
            "     <if test='orderColumn==\"processed_bytes\"'> a.processed_bytes desc </if> \n" +
            ", a.create_time desc\n" +
            " limit 10\n" +
            "</script>"})
    List<QueryTop> selectQueryTop(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userGroup") String userGroup, @Param("orderColumn") String orderColumn);

    @Select({"<script>" +
            "select\n" +
            "a.create_time,\n" +
            "count(1) as query_num\n" +
            " from\n" +
            "  (\n" +
            "    select\n" +
            "      id,\n" +
            "      uuid,\n" +
            "      query_sql,\n" +
            "      date(create_time) as create_time,\n" +
            "      left(statusZh, 3) as status,\n" +
            "      statusZh,\n" +
            "      create_by,\n" +
            "      execute_duration,\n" +
            "      processed_bytes\n" +
            "    from\n" +
            "      query_history\n" +
            "    where\n" +
            "      create_time &lt; #{startTime}\n" +
            "      and create_time &gt;= #{endTime}\n" +
            "     <if test='createBy!=null'> and create_by = #{createBy} </if> \n" +
            "     and status = '已完成'" +
            "  )a\n" +
            " group by a.create_time\n" +
            " order by query_num desc\n" +
            "</script>"})
    List<PersonQuery> selectQueryPerson(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("createBy") String createBy);

    @Select({"<script>" +
            "select\n" +
            "  a.create_time,\n" +
            "  count(1) as query_num,\n" +
            "  sum(\n" +
            "    CASE\n" +
            "       WHEN a.status = '已完成' THEN 1\n" +
            "       ELSE 0" +
            "    END\n" +
            "  ) as success_num,\n" +
            "  sum(a.execute_duration) as sum_execute_duration,\n" +
            "  sum(a.processed_bytes) as sum_processed_bytes \n" +
            "from\n" +
            "  (\n" +
            "    select\n" +
            "      id,\n" +
            "      uuid,\n" +
            "      query_sql,\n" +
            "      date(create_time) as create_time,\n" +
            "      left(statusZh, 3) as status,\n" +
            "      statusZh,\n" +
            "      create_by,\n" +
            "      execute_duration,\n" +
            "      processed_bytes\n" +
            "    from\n" +
            "      query_history\n" +
            "    <where>\n" +
            "      create_time &lt; #{startTime}\n" +
            "      and create_time &gt;= #{endTime}\n" +
            "     <if test='userGroup!=null'> and user_group = #{userGroup} </if> \n" +
            "     and status = '已完成' </where> \n" +
            "  )a\n" +
            " group by a.create_time\n" +
            "</script>"})
    List<TaskAndScan> selectTaskAndScan(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userGroup") String userGroup);
}
