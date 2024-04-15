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
 * @date 2022-01-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "task_scale_strategy")
public class TaskScaleStrategy extends DeleteEntity {

    @Column(name = "task_id")
    public Integer taskId;

    @Column(name = "name")
    public String name;

    @Column(name = "specific_strategy")
    public String specificStrategy;

    @Column(name = "cooling_time")
    public Integer coolingTime;


}
