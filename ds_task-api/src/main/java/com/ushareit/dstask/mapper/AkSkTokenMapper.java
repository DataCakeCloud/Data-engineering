package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AkSkToken;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * @author tianxu
 * @date 2023/4/19 16:10
 **/
@Mapper
public interface AkSkTokenMapper extends CrudMapper<AkSkToken> {
    @Select("select * from aksk_token where token=#{token} order by create_time desc limit 1")
    AkSkToken selectBytoken(@Param("token") String token);

    @Select({"<script>" +
            "select * from aksk_token where tenant_name=#{tenantName} and tenant_id=#{tenantId} and user_name=#{userName} order by create_time desc limit 1" +
            "</script>"})
    AkSkToken selectByUser(@Param("tenantName") String tenantName, @Param("tenantId") Integer tenantId, @Param("userName") String userName);
}
