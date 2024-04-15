package com.ushareit.dstask.bean;


import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 CREATE TABLE `cost_monitor_notice` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `cost_monitor_id` bigint(20) DEFAULT NULL,
 `create_time` varchar(50) DEFAULT NULL,
 `name` varchar(255) DEFAULT NULL,
 `notice_time` varchar(50) DEFAULT NULL,
 `content` text,
 `create_shareit_id` varchar(255) DEFAULT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='成本监控通知表';
 * 成本监控通知表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "cost_monitor_notice")
public class CostMonitorNotice extends BaseEntity{

    @Column(name = "cost_monitor_id")
    private Integer costMonitorId;//成本监控任务id

    private String name;//监控名称
    @Column(name = "notice_time")
    private String noticeTime;//通知时间

    private String content;//监控内容
    @Column(name = "create_shareit_id")
    private String createShareitId;

}
