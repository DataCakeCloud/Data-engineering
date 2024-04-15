package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Account;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: xuebotao
 * @create: 2022-08-03
 */
@Mapper
public interface AccountMapper extends CrudMapper<Account> {
}
