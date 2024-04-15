package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
public interface ActorDefinitionService extends BaseService<ActorDefinition> {

    List<Actor> getActorByTypeAndRegion(String type, String region);

    /**
     * 向租户导入数据源定义信息
     *
     * @param tenantName 租户
     */
    void initActorDefinitions(String tenantName);

}
