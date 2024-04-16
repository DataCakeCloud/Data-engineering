package com.ushareit.dstask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.bean.meta.TaskUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("meta")
public interface MateMapper {
    @Select({"select avg(score) avg_score from gov_job_score_record " +
            "where dt>=#{startTime} and dt<=#{endTime} " +
            "and job_name in (select job_name from gov_job_info where owner=#{createBy})"})
    Double getTaskAvgScore(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("createBy") String createBy);

    @Select({"select dt, avg(score) num from gov_job_score_record " +
            "where dt>=#{startTime} and dt<=#{endTime} " +
            "and job_name in (select job_name from gov_job_info where owner=#{createBy}) group by dt order by num desc\n"})
    Double getMetaPersonScore(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("createBy") String createBy);


    @Select({"select dt, avg(score) num from gov_job_score_record " +
            "where dt>=#{startTime} and dt<=#{endTime} group by dt order by num desc\n"})
    List<TaskUsage> getMetaAvgScore(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select({"select dt, sum(job_name_quantity) num from gov_spark_cost " +
            "where engine='Spark' and owner=#{createBy} and dt>=#{startTime} and dt<=#{endTime} " +
            "and job_name in (select job_name from gov_job_info where owner=#{createBy}) group by dt order by dt asc\n"})
    List<TaskUsage> getMetaTaskUsages(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("createBy") String createBy);
}
