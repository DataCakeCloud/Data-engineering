package com.ushareit.dstask.mapper;


import com.ushareit.dstask.bean.DeptInfo;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * author:xuebotao
 * date:2022-03-07
 */
@Mapper
public interface DeptMapper extends CrudMapper<DeptInfo> {


}
