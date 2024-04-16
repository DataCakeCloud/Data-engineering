package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.FeedbackProcessItem;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Mapper
public interface FeedbackProcessItemMapper extends CrudMapper<FeedbackProcessItem> {


    /**
     * 根据feedbackId查询
     *
     * @return
     */
    @Select({"SELECT * FROM feedback_process_item" +
            " WHERE feedback_id=#{feedbackId}" +
            " AND delete_status=0 " +
            " AND id >#{maxId}  order by create_time asc "})
    List<FeedbackProcessItem> selectByFeedId(@Param("feedbackId") Integer feedbackId
            , @Param("maxId") Integer maxId);
}
