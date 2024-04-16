package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "actor")
public class Actor extends DeleteEntity {
    private static final long serialVersionUID = -7403333971490969520L;

    private Integer actorDefinitionId;
    private String name;
    private String region;
    private String configuration;
    private String actorType;
    private String uuid;

    private String groups ;
    private String createUserGroupUuid;
}
