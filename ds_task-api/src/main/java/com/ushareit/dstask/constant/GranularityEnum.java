package com.ushareit.dstask.constant;

import com.ushareit.dstask.common.module.CronConfig;
import com.ushareit.dstask.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author fengxiao
 * @date 2022/11/18
 */
@Getter
@AllArgsConstructor
public enum GranularityEnum {

    /**
     * 分钟级
     */
    MINUTELY(cron -> {
        if (cron.getMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "分钟参数不能为空");
        }
        if (cron.getMinute() < NumberUtils.INTEGER_ONE) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "分钟参数不能小于1");
        }
        if (cron.getMinute() > NumberUtils.INTEGER_ONE && cron.getFromMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "第几分钟参数不能为空");
        }
        return Optional.empty();
    }, cron -> isOneTimeUnit(cron.getMinute()) ? CronConfig.EVERY_MINUTE :
            String.format(CronConfig.EVERY_MORE_MINUTE, cron.getFromMinute(), cron.getMinute())),

    /**
     * 小时级
     */
    HOURLY(cron -> {
        if (cron.getHour() == null || cron.getMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "小时和分钟参数不能为空");
        }
        if (cron.getHour() < NumberUtils.INTEGER_ONE) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "小时参数不能小于1");
        }
        if (cron.getHour() > NumberUtils.INTEGER_ONE && (cron.getFromHour() == null || cron.getMinute() == null)) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "第几小时和分钟参数不能为空");
        }
        return Optional.empty();
    }, cron -> isOneTimeUnit(cron.getHour()) ? String.format(CronConfig.EVERY_HOUR, cron.getMinute()) :
            String.format(CronConfig.EVERY_MORE_HOUR, cron.getMinute(), cron.getFromHour(), cron.getHour())),

    /**
     * 天级
     */
    DAILY(cron -> {
        if (cron.getHour() == null || cron.getMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "小时和分钟参数不能为空");
        }
        return Optional.empty();
    }, cron -> String.format(CronConfig.EVERY_DAY, cron.getMinute(), cron.getHour())),

    /**
     * 周级
     */
    WEEKLY(cron -> {
        if (cron.getDayOfWeek() == null || cron.getHour() == null || cron.getMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "周内日期小时和分钟参数不能为空");
        }
        return Optional.empty();
    }, cron -> String.format(CronConfig.EVERY_WEEK, cron.getMinute(), cron.getHour(), cron.getDayOfWeek())),

    /**
     * 月级
     */
    MONTHLY(cron -> {
        if (cron.getDayOfMonth() == null || cron.getHour() == null || cron.getMinute() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "月内日期小时和分钟参数不能为空");
        }
        return Optional.empty();
    }, cron -> String.format(CronConfig.EVERY_MONTH, cron.getMinute(), cron.getHour(), cron.getDayOfMonth()));


    private final Function<CronConfig, Optional<ServiceException>> validate;
    private final Function<CronConfig, String> expressionMapper;

    private static boolean isOneTimeUnit(Integer time) {
        return time == NumberUtils.INTEGER_ONE.intValue();
    }

    public static GranularityEnum of(String type) {
        for (GranularityEnum granularityEnum : GranularityEnum.values()) {
            if (StringUtils.equalsIgnoreCase(granularityEnum.name(), type)) {
                return granularityEnum;
            }
        }

        throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), String.format("不支持的粒度 %s", type));
    }

}
