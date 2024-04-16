package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.AccumulateUser;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wuyan
 * @date 2022/4/20
 */
@Mapper
public interface AccumulateUserMapper extends CrudMapper<AccumulateUser> {
}
