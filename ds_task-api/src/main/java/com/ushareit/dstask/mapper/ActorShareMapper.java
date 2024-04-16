package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorShare;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface ActorShareMapper extends CrudMapper<ActorShare> {
}
