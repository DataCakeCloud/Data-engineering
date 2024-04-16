package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @author fengxiao
 * @date 2022/7/27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "actor_catalog")
public class ActorCatalog extends DeleteEntity {
    private static final long serialVersionUID = -4528349025252180912L;

    private Integer actorId;
    private String catalog;
    private String catalogHash;
}
