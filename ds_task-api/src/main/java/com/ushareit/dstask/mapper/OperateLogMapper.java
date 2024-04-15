package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.OperateLog;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
@Mapper
public interface OperateLogMapper extends CrudMapper<OperateLog> {

    @Select({"select COALESCE(avg(cost_time), 0.0) from operate_log where request_time >= DATE_FORMAT(CURDATE(),'%Y-%m-%d %H:%i:%s')"})
    Double getDailyAverageCostTime();


    @Select({"select date_format(request_time, '%Y-%m-%d') as `time`, count(DISTINCT user_name )as user_num from operate_log where date_format(request_time, '%Y-%m-%d') <> current_date group by date_format(request_time , '%Y-%m-%d')"})
    List<Map<String, Integer>> getDayUsers();

    @Select({"select count(DISTINCT user_name )as user_num from operate_log where request_time >= #{start} and request_time <= #{end}"})
    Integer getWeekUsers(@Param("start") Timestamp start, @Param("end") Timestamp end);

    @Select({"select distinct(user_name) as user_name from operate_log \n" +
            " where  user_name!=''  \n" +
            " and (uri = '/task/page' or uri = '/task/catalog' or uri = '/dict/list' \n" +
            " or uri = '/task/last7' or uri = '/collect/my' or uri = '/sysdict/getTemplateList' \n" +
            " or uri = '/label/list'  or uri = '/user/admin' or uri like '/metadata/source/%' )  "})
    List<String> getCumulativeUsers();
}
