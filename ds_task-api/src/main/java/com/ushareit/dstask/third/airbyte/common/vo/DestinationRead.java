package com.ushareit.dstask.third.airbyte.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.third.airbyte.common.enums.ReleaseStage;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationRead {

    private Integer destinationDefinitionId;
    private Integer destinationId;
    private JSONObject connectionConfiguration;
    private String name;
    private String region;
    private String destinationName;
    private String destinationIcon;
    private ReleaseStage destinationReleaseStage;
    private String createBy;
    private String updateBy;
    private Timestamp createTime;
    private Timestamp updateTime;

    public DestinationRead(Actor actor, Map<Integer, ActorDefinition> definitionMap) {
        this.destinationDefinitionId = actor.getActorDefinitionId();
        this.destinationId = actor.getId();
        this.connectionConfiguration = JSONObject.parseObject(actor.getConfiguration());
        this.name = actor.getName();
        this.region = actor.getRegion();
        this.createBy = actor.getCreateBy();
        this.updateBy = actor.getUpdateBy();
        this.createTime = actor.getCreateTime();
        this.updateTime = actor.getUpdateTime();

        ActorDefinition definition = definitionMap.get(actor.getActorDefinitionId());
        if (definition != null) {
            this.destinationName = definition.getName();
            this.destinationIcon = definition.getIcon();
            this.destinationReleaseStage = ReleaseStage.fromValue(definition.getReleaseStage());
        }
    }
}
