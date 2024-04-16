package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.WechatAlert;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WechatAlertMapper extends CrudMapper<WechatAlert> {

    @Select({"<script>" +
            "select * from wechat_alert where name=#{info.name} and delete_status=0" +
            "<if test='info.id!=null and \"\" neq info.id'> AND id != #{info.id} </if>" +
            "</script>"})
    WechatAlert selectByNameUuid(@Param("info") WechatAlert info);

    @Select({"<script>" +
            "select * from wechat_alert " +
            "where delete_status=0 " +
            "<if test='wechatAlert.userGroupId!=null and \"\" neq wechatAlert.userGroupId'> AND user_group_id = #{wechatAlert.userGroupId} </if>" +
            "<if test='wechatAlert.name!=null and \"\" neq wechatAlert.name'> AND name = #{wechatAlert.name} </if>" +
            "<if test='wechatAlert.startTime!=null and \"\" neq wechatAlert.startTime'> AND create_time &gt;= #{wechatAlert.startTime} </if>" +
            "<if test='wechatAlert.endTime!=null and \"\" neq wechatAlert.endTime'> AND create_time &lt;= #{wechatAlert.endTime} </if>" +
            " order by create_time desc " +
            "</script>"})
    List<WechatAlert> selectByConditions(@Param("wechatAlert") WechatAlert wechatAlert);
}
