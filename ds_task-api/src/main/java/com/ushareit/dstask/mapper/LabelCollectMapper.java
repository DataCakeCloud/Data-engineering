package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.LabelCollect;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wuyan
 * @date 2022/1/19
 */
@Mapper
public interface LabelCollectMapper extends CrudMapper<LabelCollect> {
    /**
     * 根据name查询
     *
     * @param user
     * @return
     */
    @Select({"SELECT * FROM label_collect WHERE create_by=#{user}"})
    LabelCollect selectByUser(@Param("user") String user);
}
