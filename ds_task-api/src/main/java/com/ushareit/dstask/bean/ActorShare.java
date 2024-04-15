package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 数据源共享表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "actor_share")
public class ActorShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "actor_id")
    private Integer actorId;
    @Column(name = "share_id")
    private String shareId;//userId或者groupId
    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private int type;//1 userId 2group

}
