package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.bean.meta.TaskUsage;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Mapper
public interface FeedbackMapper extends CrudMapper<Feedback> {
    /**
     * @author wuyan
     * @return
     */
    @Select({"<script>" +
            "SELECT * FROM feedback WHERE handle_by = '' and status='UN_ACCEPT' order by create_time desc" +
            "</script>"})
    List<Feedback> selectNonAcceptFeedbacks();


    /**
     * @author wuyan
     * @return
     */
    @Select({"<script>" +
            "SELECT date(create_time) dt, sum(id) num FROM feedback WHERE 1=1 " +
            "<if test='create!=null'> AND create_by = #{create} </if> " +
            "and (date(create_time) &lt; #{end} and date(create_time) &gt;= #{start} ) " +
            "group by date(create_time) order by num desc" +
            "</script>"})
    List<TaskUsage> selectDayFeedbacks(@Param("create") String create, @Param("start") String start, @Param("end") String end);
}
