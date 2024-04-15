package com.ushareit.dstask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.bean.CostMonitor;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CostMonitorMapper extends CrudMapper<CostMonitor> {
    @Select("select * from cost_monitor where valid=1 and create_shareit_id=#{createShareitId} order by id desc")
    List<CostMonitor> selectByCreateShareitId(@Param("createShareitId") String createShareitId);
    @Select("select * from cost_monitor where valid=1")
    List<CostMonitor> selectAllByValid();
}
