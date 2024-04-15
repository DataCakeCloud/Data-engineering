package com.ushareit.dstask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.bean.CostMonitorNotice;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CostMonitorNoticeMapper extends CrudMapper<CostMonitorNotice> {
    @Select("select * from cost_monitor_notice where  create_shareit_id=#{shareitId} order by id desc")
    List<CostMonitorNotice> selectByShareitId(@Param("shareitId") String shareitId);
    @Select("select * from cost_monitor_notice where  create_shareit_id=#{shareitId} and notice_time=#{noticeTime}")
    List<CostMonitorNotice> selectByShareitIdAndNoticeTime(@Param("shareitId") String shareitId,@Param("noticeTime")String noticeTime);
}
