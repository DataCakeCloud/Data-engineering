package com.ushareit.dstask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.bean.ApiGateway;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("api_gateway")
public interface ApiGatewayMapper {
    @Select({"<script>" +
            "select id,user_name,source,uri,request_time,response_time,cost_time,params,result_code,result_message from operate_log " +
            "where uri != '/api-gateway/qe/query/logs' " +
            "<if test='info.source!=null and \"\" neq info.source'> AND source = #{info.source} </if>" +
            "<if test='info.userName!=null and \"\" neq info.userName'> AND user_name = #{info.userName} </if>" +
            "<if test='info.uri!=null and \"\" neq info.uri'> AND uri LIKE CONCAT('%', #{info.uri}, '%') </if>" +
            "<if test='info.params!=null and \"\" neq info.params'> AND params LIKE CONCAT('%', #{info.params}, '%') </if>" +
            "<if test='info.startTimeFormat!=null and \"\" neq info.startTimeFormat'> AND request_time &gt;= #{info.startTimeFormat} </if>" +
            "<if test='info.endTimeFormat!=null and \"\" neq info.endTimeFormat'> AND request_time &lt;= #{info.endTimeFormat} </if>" +
            "<if test='info.eventUri != null and info.eventUri.size() > 0'>" +
            " AND (" +
            "        <foreach collection='info.eventUri' item='uriItem' separator=' OR '>" +
            "            uri = CONCAT('/api-gateway', #{uriItem})" +
            "        </foreach>" +
            "      )" +
            "</if>" +
            " order by request_time desc " +
            "</script>"})
    List<ApiGateway> getAuditLog(@Param("info") ApiGateway info);
}
