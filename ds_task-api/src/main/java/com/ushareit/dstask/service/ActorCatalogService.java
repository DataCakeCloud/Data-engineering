package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.ActorCatalog;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/7/27
 */
public interface ActorCatalogService extends BaseService<ActorCatalog> {

    /**
     * 保存数据源目录信息
     *
     * @param actorId        数据源ID
     * @param airbyteCatalog 数据源目录
     */
    void saveOrUpdate(Integer actorId, AirbyteCatalog airbyteCatalog);


    List<ActorCatalog> selectByActorId(Integer actorId);

}
