package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.ShortUrlEntity;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShortUrlMapper extends CrudMapper<ShortUrlEntity> {
    @Select("select * from short_url where url_id=#{urlId}")
    ShortUrlEntity selectByUrlId(@Param("urlId") String urlId);

    @Select("select * from short_url where real_url=#{realUrl}")
    ShortUrlEntity selectByRealUrl(@Param("realUrl") String realUrl);
}

