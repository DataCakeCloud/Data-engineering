package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;


@Data
@Entity
@Builder
@Table(name = "ds_indicators")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("DS指标统计")
@Getter
@Setter
public class DsIndicatorStatistical extends BaseEntity {

    /**
     * 日期
     */
    @Column(name = "dt", nullable = false)
    private String dt;

    /**
     * 指标名称
     */
    @Column(name = "indicators")
    private String indicators;

    /**
     * 指标值
     */
    @Column(name = "value")
    private Integer value;

    @Column(name = "create_time")
    private Timestamp createTime;
    @Column(name = "update_time")
    private Timestamp updateTime;

}
