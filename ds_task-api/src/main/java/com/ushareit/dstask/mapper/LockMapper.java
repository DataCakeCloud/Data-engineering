package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.JDBCLock;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author: xuebotao
 * @create: 2021-11-24
 */
@Mapper
public interface LockMapper extends CrudMapper<JDBCLock> {


    /**
     * 通过tag查询锁
     *
     * @param tag
     * @return
     */
    @Select("select * from lock_info where tag=#{tag} and status=1")
    List<JDBCLock> getByTag(@Param("tag") String tag);

    /**
     * 通过tag删除锁
     *
     * @param tag
     */
    @Delete({"<script>" +
            "DELETE  FROM lock_info  " +
            "WHERE tag=#{tag} AND status=1  " +
            "</script>"})
    void deleteByTag(@Param("tag") String tag);


}
