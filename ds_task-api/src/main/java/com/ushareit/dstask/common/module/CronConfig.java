package com.ushareit.dstask.common.module;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CrontabParam;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.GranularityEnum;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/11/14
 */
@Data
public class CronConfig {

    public static final String EVERY_MINUTE = "* * * * *";
    public static final String EVERY_MORE_MINUTE = "%s/%s * * * *";
    public static final String EVERY_HOUR = "%s * * * *";
    public static final String EVERY_MORE_HOUR = "%s %s/%s * * *";
    public static final String EVERY_DAY = "%s %s * * *";
    public static final String EVERY_WEEK = "%s %s * * %s";
    public static final String EVERY_MONTH = "%s %s %s * *";

    private Integer minute;
    private Integer hour;
    private Integer dayOfMonth;
    private Integer month;
    private Integer dayOfWeek;
    private Integer fromHour;
    private Integer fromMinute;

    private String zone;

    public static String parseToExpression(String granularity, String cronInfo) {
        GranularityEnum granularityEnum = GranularityEnum.of(granularity.toUpperCase());
        CronConfig cronConfig = JSONObject.parseObject(cronInfo, CronConfig.class);
        return granularityEnum.getExpressionMapper().apply(cronConfig);
    }

    public TriggerParam toTriggerParam(String granularity) {
        GranularityEnum granularityEnum = GranularityEnum.of(granularity);

        TriggerParam triggerParam = new TriggerParam();
        triggerParam.setType(DsTaskConstant.CRON_TRIGGER);
        triggerParam.setOutputGranularity(granularity);
        triggerParam.setCrontab(granularityEnum.getExpressionMapper().apply(this));
        triggerParam.setCrontabParam(new CrontabParam());
        return triggerParam;
    }

}
