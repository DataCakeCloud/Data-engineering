package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AkSk;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author tianxu
 * @date 2023/4/18 18:22
 **/
@Mapper
public interface AkSkMapper extends CrudMapper<AkSk> {
    @Select("select * from aksk where tenant_name=#{name}")
    AkSk selectByName(@Param("name") String name);
    @Select("select * from aksk where ak=#{ak} and sk=#{sk}")
    AkSk selectByAksk(@Param("ak") String ak, @Param("sk")String sk);
}
