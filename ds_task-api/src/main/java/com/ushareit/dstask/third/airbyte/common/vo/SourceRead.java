package com.ushareit.dstask.third.airbyte.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.OwnerApp;
import com.ushareit.dstask.third.airbyte.common.enums.ReleaseStage;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class SourceRead {

    private Integer sourceDefinitionId;
    private Integer sourceId;
    private JSONObject connectionConfiguration;
    private String name;
    private String region;
    private String sourceName;
    private String sourceIcon;
    private ReleaseStage sourceReleaseStage;
    private String createBy;
    private String updateBy;
    private String uuid;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String groups;
    private String ownerAppName;
    private Integer ownerAppId;

    public SourceRead(Actor actor, Map<Integer, ActorDefinition> definitionMap, OwnerApp ownerApp) {
        this.sourceDefinitionId = actor.getActorDefinitionId();
        this.sourceId = actor.getId();
        this.connectionConfiguration = JSONObject.parseObject(actor.getConfiguration());
        this.name = actor.getName();
        this.region = actor.getRegion();
        this.createBy = actor.getCreateBy();
        this.updateBy = actor.getUpdateBy();
        this.createTime = actor.getCreateTime();
        this.updateTime = actor.getUpdateTime();
        this.uuid=actor.getUuid();
        this.groups=actor.getGroups();
        ActorDefinition definition = definitionMap.get(actor.getActorDefinitionId());
        if (definition != null) {
            this.sourceName = definition.getName();
            this.sourceIcon = definition.getIcon();
            this.sourceReleaseStage = ReleaseStage.fromValue(definition.getReleaseStage());
        }
        this.ownerAppName = ownerApp == null ? null : ownerApp.getName();
        this.ownerAppId = ownerApp == null ? null : ownerApp.getId();
    }
    public SourceRead(Actor actor, Map<Integer, ActorDefinition> definitionMap) {
        new SourceRead(actor,definitionMap,null);
    }

}
