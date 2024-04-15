package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Catalog;
import com.ushareit.dstask.bean.Dictionary;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface CatalogMapper extends CrudMapper<Catalog> {

}
