package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "actor_definition")
public class ActorDefinition extends DeleteEntity {
    private static final long serialVersionUID = 3153828100592580099L;

    private String name;
    private String dockerRepository;
    private String dockerImageTag;
    private String documentationUrl;
    private String icon;
    private String actorType;
    private String sourceType;
    private String spec;
    private String releaseStage;
    private Date releaseDate;
    private String resourceRequirements;
    private Integer forDsTemplate;
    private Integer isOpen;

}
