package com.ushareit.dstask.mapper;



import com.ushareit.dstask.bean.DutyInfo;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author xuebotao
 * @date 2022-11-25
 */
@Mapper
public interface DutyInfoMapper extends CrudMapper<DutyInfo> {


    @Select({"<script>" +
            "select * from  duty_info " +
            "WHERE module=#{module}  and tenant_id = #{tenantId}  and  delete_status =0 " +
            " order by serial_number asc" +
            "</script>"})
    List<DutyInfo> selectByTenant(@Param("tenantId") Integer tenantId, @Param("module") String module);

}
