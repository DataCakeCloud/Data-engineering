package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.Auditlog;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditlogMapper  extends CrudMapper<Auditlog> {

    @Select({"<script>" +
            "SELECT distinct event_code from auditlog " +
            "WHERE 1 = 1 " +
            "<if test='type!=null'> AND module = #{type} </if> " +
            "</script>"})
    List<String> selectCode(@Param("type") String type);
}
