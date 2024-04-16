package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.DsIndicatorStatistical;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: xuebotao
 * @create: 2021-12-03
 */
@Mapper
public interface DsIndicatorStatisticalMapper extends CrudMapper<DsIndicatorStatistical> {


    @Select("select * from ds_indicators where dt=#{dt} AND  indicators=#{indicators} ")
    List<DsIndicatorStatistical> getDataByDtAndName(@Param("dt") String dt, @Param("indicators") String indicators);

}
