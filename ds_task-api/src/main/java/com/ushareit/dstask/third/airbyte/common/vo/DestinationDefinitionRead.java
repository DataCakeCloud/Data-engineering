package com.ushareit.dstask.third.airbyte.common.vo;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.third.airbyte.common.enums.ReleaseStage;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationDefinitionRead {

    private Integer destinationDefinitionId;
    private String name;
    private String dockerRepository;
    private String dockerImageTag;
    private String documentationUrl;
    private String icon;
    private ReleaseStage releaseStage;
    private Date releaseDate;
    // private ActorDefinitionResourceRequirements resourceRequirements;

    private Integer isOpen;
    private String createBy;
    private String updateBy;
    private Timestamp createTime;
    private Timestamp updateTime;

    public DestinationDefinitionRead(ActorDefinition actorDefinition) {
        this.destinationDefinitionId = actorDefinition.getId();
        this.name = actorDefinition.getName();
        this.dockerRepository = actorDefinition.getDockerRepository();
        this.dockerImageTag = actorDefinition.getDockerImageTag();
        this.documentationUrl = actorDefinition.getDocumentationUrl();
        this.icon = actorDefinition.getIcon();
        this.releaseStage = ReleaseStage.fromValue(actorDefinition.getReleaseStage());
        this.releaseDate = actorDefinition.getReleaseDate();
        // this.resourceRequirements = StringUtils.isBlank(actorDefinition.getResourceRequirements()) ? null
        //        : Jsons.convertValue(actorDefinition.getResourceRequirements(), ActorDefinitionResourceRequirements.class);

        this.isOpen = actorDefinition.getIsOpen();
        this.createBy = actorDefinition.getCreateBy();
        this.updateBy = actorDefinition.getUpdateBy();
        this.createTime = actorDefinition.getCreateTime();
        this.updateTime = actorDefinition.getUpdateTime();
    }
}
