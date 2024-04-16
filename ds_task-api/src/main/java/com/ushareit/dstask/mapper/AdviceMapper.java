package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Advice;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xuebotao
 * @date 2022-08-10
 */
@Mapper
public interface AdviceMapper extends CrudMapper<Advice> {
}
