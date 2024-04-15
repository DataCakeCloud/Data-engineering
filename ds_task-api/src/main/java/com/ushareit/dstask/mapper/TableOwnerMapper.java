package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.TableOwner;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @auther tiyongshuai
 * @data 2024/3/28
 * @description
 */
@Mapper
public interface TableOwnerMapper extends CrudMapper<TableOwner> {
}
