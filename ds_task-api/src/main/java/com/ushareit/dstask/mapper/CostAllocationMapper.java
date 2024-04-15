package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.CostAllocation;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author xuebotao
 * @date 2022/12/02
 */
@Mapper
public interface CostAllocationMapper extends CrudMapper<CostAllocation> {

//    @Select({"<script>" +
//            "select usergroup.id ,usergroup.name from ( \n" +
//            "select group_id from cost_allocation where " +
//            " delete_status =0 AND task_id=#{taskId}  ) cost\n" +
//            "left join \n" +
//            "(select id,name from access_group ag where delete_status =0 and type =0 ) usergroup \n" +
//            "on cost.group_id = usergroup.id \n" +
//            "where usergroup.id  is not null   " +
//            "</script>"})
//    List<CostAllocation> selectByTaskId(@Param("taskId") Integer taskId);

}
