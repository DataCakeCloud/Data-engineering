package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.HiveTable;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wuyan
 * @date 2021/8/26
 */
@Mapper
public interface HiveTablesMapper extends CrudMapper<HiveTable> {
    /**
     * 根据name查询
     *
     * @param id id
     * @return
     */
    @Select({"SELECT * FROM hive_tables WHERE task_id=#{id}"})
    HiveTable selectByTaskId(@Param("id") Integer id);
}
