package com.ushareit.dstask.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.sql.Timestamp;

/**
 * @author xuebotao
 * @date 2022-01-12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "task_par_change")
public class TaskParChange extends BaseEntity {

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "strategy_name")
    protected String strategyName;

    @Column(name = "original_par")
    protected Integer originalPar;

    @Column(name = "update_par")
    protected Integer updatePar;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "restart_time")
    private Timestamp restartTime;


}
