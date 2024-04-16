package com.ushareit.dstask.common.param;

import com.ushareit.dstask.common.module.CronConfig;
import com.ushareit.dstask.constant.GranularityEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author fengxiao
 * @date 2022/11/18
 */
@Data
public class CronParam extends CronConfig {

    @Pattern(regexp = "minutely|hourly|daily|weekly|monthly", message = "粒度取值不存在")
    @NotBlank(message = "粒度不能为空")
    private String granularity;

    private Integer minute;
    private Integer hour;
    private Integer dayOfMonth;
    private Integer month;
    private Integer dayOfWeek;
    private Integer fromHour;
    private Integer fromMinute;
    private String zone;

    public void validate() {
        GranularityEnum.valueOf(granularity.toUpperCase()).getValidate().apply(this);
    }

    public String toExpression() {
        return GranularityEnum.valueOf(granularity.toUpperCase()).getExpressionMapper().apply(this);
    }

}
