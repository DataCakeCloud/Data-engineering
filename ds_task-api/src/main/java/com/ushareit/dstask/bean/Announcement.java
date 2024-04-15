package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author wuyan
 * @date 2022-09-07
 */
@Data
@Builder
@Table(name = "announcement")
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "announcement")
public class Announcement extends OperatorEntity {

    private String name;

    private String content;

    private Integer online;

    @Column(name = "delete_status")
    private Integer deleteStatus;
}
