package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 CREATE TABLE `cost_monitor` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `create_shareit_id` varchar(50) DEFAULT NULL,
 `execute_time` varchar(50) DEFAULT NULL,
 `create_time` varchar(50) DEFAULT NULL,
 `dps` text DEFAULT NULL,
 `jobs` text DEFAULT NULL,
 `frep` varchar(200) NOT NULL,
 `monitor_level` int(11) NOT NULL,
 `name` varchar(255) DEFAULT NULL,
 `notice_persons` text DEFAULT NULL,
 `notice_self` varchar(50) DEFAULT NULL,
 `notice_type` varchar(10) DEFAULT NULL,
 `owners` text DEFAULT NULL,
 `pus` text DEFAULT NULL,
 `ratio` varchar(10) DEFAULT NULL,
 `trial_range` int(11) NOT NULL,
 `type` int(11) NOT NULL,
 `valid` bit(1) NOT NULL,
 `send_notice` bit(1) NOT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='成本监控表';
 * 成本监控表
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "cost_monitor")
public class CostMonitor extends BaseEntity {

    private String name;//监控名称
    private Integer type;//监控类型 1 新任务 2：同比分析  3： 环比分析
    @Column(name = "monitor_level")
    private Integer  monitorLevel;//监控维度 1部门 2PU 3owner 4任务
    @Column(name = "trial_range")
    private Integer trialRange;//试用范围 1所有维度  2自定义
    @Column(name = "notice_type")
    private String noticeType;//1 钉钉
    @Column(name = "notice_persons")
    private String noticePersons;//通知范围人
    @Column(name = "notice_self")
    private String noticeSelf;//是否通知自己
    private String dps;//
    private String pus;
    private String owners;
    private String jobs;
    private String frep;//通知频率 0 每天
    private String ratio;//通知比例
    private Boolean valid;
    @Column(name = "create_shareit_id")
    private String createShareitId;
    @Column(name = "create_time")
    private String createTime;
    @Column(name = "execute_time")
    private String executeTime;//执行时间
    @Column(name = "send_notice")
    private Boolean sendNotice;//




}
