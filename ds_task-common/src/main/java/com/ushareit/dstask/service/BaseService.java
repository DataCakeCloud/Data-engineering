package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.BaseEntity;

/**
 * Base Service
 *
 * @author Much
 * @date 2018/10/26
 */
public interface BaseService <T extends BaseEntity> extends CrudService<T>{
}
