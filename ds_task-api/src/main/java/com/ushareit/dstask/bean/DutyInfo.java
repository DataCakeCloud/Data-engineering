package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author xuebotao
 * @date 2022-11-25
 */
@Data
@Entity
@Builder
@Table(name = "duty_info")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("值班信息表")
public class DutyInfo extends DeleteEntity {

    @ApiModelProperty(value = "值班模块")
    private Integer tenantId;

    @ApiModelProperty(value = "值班模块")
    private String module;

    @ApiModelProperty(value = "用户id")
    @Column(name = "user_id")
    private Integer userId;

    @ApiModelProperty(value = "编号")
    @Column(name = "serial_number")
    private Integer serialNumber;

    @ApiModelProperty(value = "当前是否值班")
    @Column(name = "is_duty")
    private String isDuty;

    @ApiModelProperty(value = "值班日期")
    @Column(name = "duty_date")
    private String dutyDate;

}