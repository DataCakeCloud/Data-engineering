package com.ushareit.dstask.web.utils;

import com.google.common.collect.Maps;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author dongjiejie
 * @date 2017/2/16
 */
public class DateTimeUtils {

    private final static Map<Integer, String> INTEGER_HOUR_START_STRING_MAP = Maps.newHashMap();

    private final static Map<Integer, String> INTEGER_HOUR_END_STRING_MAP = Maps.newHashMap();

    static {
        INTEGER_HOUR_START_STRING_MAP.put(0, "00:00");
        INTEGER_HOUR_START_STRING_MAP.put(1, "01:00");
        INTEGER_HOUR_START_STRING_MAP.put(2, "02:00");
        INTEGER_HOUR_START_STRING_MAP.put(3, "03:00");
        INTEGER_HOUR_START_STRING_MAP.put(4, "04:00");
        INTEGER_HOUR_START_STRING_MAP.put(5, "05:00");
        INTEGER_HOUR_START_STRING_MAP.put(6, "06:00");
        INTEGER_HOUR_START_STRING_MAP.put(7, "07:00");
        INTEGER_HOUR_START_STRING_MAP.put(8, "08:00");
        INTEGER_HOUR_START_STRING_MAP.put(9, "09:00");
        INTEGER_HOUR_START_STRING_MAP.put(10, "10:00");
        INTEGER_HOUR_START_STRING_MAP.put(11, "11:00");
        INTEGER_HOUR_START_STRING_MAP.put(12, "12:00");
        INTEGER_HOUR_START_STRING_MAP.put(13, "13:00");
        INTEGER_HOUR_START_STRING_MAP.put(14, "14:00");
        INTEGER_HOUR_START_STRING_MAP.put(15, "15:00");
        INTEGER_HOUR_START_STRING_MAP.put(16, "16:00");
        INTEGER_HOUR_START_STRING_MAP.put(17, "17:00");
        INTEGER_HOUR_START_STRING_MAP.put(18, "18:00");
        INTEGER_HOUR_START_STRING_MAP.put(19, "19:00");
        INTEGER_HOUR_START_STRING_MAP.put(20, "20:00");
        INTEGER_HOUR_START_STRING_MAP.put(21, "21:00");
        INTEGER_HOUR_START_STRING_MAP.put(22, "22:00");
        INTEGER_HOUR_START_STRING_MAP.put(23, "23:00");

        INTEGER_HOUR_END_STRING_MAP.put(0, "00:59");
        INTEGER_HOUR_END_STRING_MAP.put(1, "01:59");
        INTEGER_HOUR_END_STRING_MAP.put(2, "02:59");
        INTEGER_HOUR_END_STRING_MAP.put(3, "03:59");
        INTEGER_HOUR_END_STRING_MAP.put(4, "04:59");
        INTEGER_HOUR_END_STRING_MAP.put(5, "05:59");
        INTEGER_HOUR_END_STRING_MAP.put(6, "06:59");
        INTEGER_HOUR_END_STRING_MAP.put(7, "07:59");
        INTEGER_HOUR_END_STRING_MAP.put(8, "08:59");
        INTEGER_HOUR_END_STRING_MAP.put(9, "09:59");
        INTEGER_HOUR_END_STRING_MAP.put(10, "10:59");
        INTEGER_HOUR_END_STRING_MAP.put(11, "11:59");
        INTEGER_HOUR_END_STRING_MAP.put(12, "12:59");
        INTEGER_HOUR_END_STRING_MAP.put(13, "13:59");
        INTEGER_HOUR_END_STRING_MAP.put(14, "14:59");
        INTEGER_HOUR_END_STRING_MAP.put(15, "15:59");
        INTEGER_HOUR_END_STRING_MAP.put(16, "16:59");
        INTEGER_HOUR_END_STRING_MAP.put(17, "17:59");
        INTEGER_HOUR_END_STRING_MAP.put(18, "18:59");
        INTEGER_HOUR_END_STRING_MAP.put(19, "19:59");
        INTEGER_HOUR_END_STRING_MAP.put(20, "20:59");
        INTEGER_HOUR_END_STRING_MAP.put(21, "21:59");
        INTEGER_HOUR_END_STRING_MAP.put(22, "22:59");
        INTEGER_HOUR_END_STRING_MAP.put(23, "23:59");
    }

    private final static int DAY_START_HOUR_INT = 0;

    private final static int DAY_END_HOUR_INT = 23;

    /**
     * 获取一个小时的的字符串表示 例如 2点 => 2:00
     *
     * @param hour
     *
     * @return
     */
    public final static String getStartHourString(int hour) {
        if (hour < DAY_START_HOUR_INT || hour > DAY_END_HOUR_INT) {
            throw new IllegalArgumentException("参数错误");
        }
        return INTEGER_HOUR_START_STRING_MAP.get(hour);
    }

    public static String getCurrentDayStart(){
        String currentDay=getCurrentDateStr(DateTimeFormatterEnum.YYYYMMDD);
        return currentDay+"000000000";
    }

    public static String getCurrentDayEnd(){
        String currentDay=getCurrentDateStr(DateTimeFormatterEnum.YYYYMMDD);
        return currentDay+"235959999";
    }

    /**
     * 获取一个小时的的字符串表示 例如 2点 => 2:59
     *
     * @param hour
     *
     * @return
     */
    public final static String getEndHourString(int hour) {
        if (hour < DAY_START_HOUR_INT || hour > DAY_END_HOUR_INT) {
            throw new IllegalArgumentException("参数错误");
        }
        return INTEGER_HOUR_END_STRING_MAP.get(hour);
    }

    /**
     * 获取两个时间直接的秒数
     *
     * @param dateA
     * @param DateB
     *
     * @return
     */
    public final static long timeSecondsInterval(Date dateA, Date DateB) {
        LocalDateTime localDateTimeA = convert(dateA);

        LocalDateTime localDateTimeB = convert(DateB);
        long secondA = localDateTimeA.atZone(ZoneId.systemDefault()).toInstant().getLong(ChronoField.INSTANT_SECONDS);
        long secondB = localDateTimeB.atZone(ZoneId.systemDefault()).toInstant().getLong(ChronoField.INSTANT_SECONDS);
        return secondA - secondB;
    }

    /**
     * 当前时间往后推移 unit 单位的 amount 单位数时间
     *
     * @param date
     * @param amount
     * @param unit
     *
     * @return
     */
    public final static Date after(Date date, int amount, ChronoUnit unit) {
        LocalDateTime localDateTime = convert(date);
        localDateTime = localDateTime.plus(amount, unit);
        return convert(localDateTime);
    }

    /**
     * 当前时间往前推移 unit 单位的 amount 单位数时间
     *
     * @param date
     * @param amount
     * @param unit
     *
     * @return
     */
    public final static Date before(Date date, int amount, ChronoUnit unit) {
        LocalDateTime localDateTime = convert(date);
        localDateTime = localDateTime.minus(amount, unit);
        return convert(localDateTime);
    }

    public final static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = convert(date);
        localDateTime = localDateTime.toLocalDate().atStartOfDay();
        return convert(localDateTime);
    }

    public final static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = convert(date);
        localDateTime = localDateTime.toLocalDate().atTime(23, 59, 59);
        return convert(localDateTime);
    }

    public final static Date getStartOfDay(String date) {
        LocalDateTime localDateTime = convert(parse(date, DateTimeFormatterEnum.YYYY_MM_DD));
        localDateTime = localDateTime.toLocalDate().atStartOfDay();
        return convert(localDateTime);
    }

    public final static Date getEndOfDay(String date) {
        LocalDateTime localDateTime = convert(parse(date, DateTimeFormatterEnum.YYYY_MM_DD));
        localDateTime = localDateTime.toLocalDate().atTime(23, 59, 59);
        return convert(localDateTime);
    }

    public final static Date getCurrentDate() {
        Instant instant = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public final static String getCurrentDateStr(DateTimeFormatterEnum dateTimeFormatterEnum) {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(dateTimeFormatterEnum.dateTimeFormatter);
    }

    /**
     * 在 @see DateTimeFormatterEnum 中选择格式，格式化Date。
     *
     * @param date
     * @param dateTimeFormatterEnum
     *
     * @return
     */
    public final static String getDateStr(Date date, DateTimeFormatterEnum dateTimeFormatterEnum) {
        if (date == null) {
            return null;
        }
        LocalDateTime res = convert(date);
        return res.format(dateTimeFormatterEnum.dateTimeFormatter);
    }

    /**
     * 获取时间字符串  某天的开始
     * @param date
     * @param dateTimeFormatterEnum
     * @return  如：2017-11-22 00:00
     */
    public final static String getDateStrStartOfDay(Date date, DateTimeFormatterEnum dateTimeFormatterEnum) {
        if (date == null) {
            return null;
        }
        Date startOfDay = getStartOfDay(date);
        return getDateStr(startOfDay, dateTimeFormatterEnum);
    }


    /**
     * 注意抛出异常 DateTimeParseException
     *
     * @param dateStr
     * @param dateTimeFormatterEnum
     *
     * @return
     */
    public final static Date parse(String dateStr, DateTimeFormatterEnum dateTimeFormatterEnum) {
        if (Objects.equals(DateTimeFormatterEnum.HH_MM_SS, dateTimeFormatterEnum)) {
            throw new IllegalArgumentException("parse date string to date must have date");
        }
        if (Objects.equals(DateTimeFormatterEnum.YYYY_MM_DD, dateTimeFormatterEnum)) {
            Instant instant = LocalDate.parse(dateStr, dateTimeFormatterEnum.dateTimeFormatter).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        }
        Instant instant = LocalDateTime.parse(dateStr, dateTimeFormatterEnum.dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static final LocalDateTime convert(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime;
    }

    private static final Date convert(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * 获取两个时间间隔的天数
     *
     * @param startDate
     * @param endDate
     *
     * @return
     */
    public static int getIntervalDays(Date startDate, Date endDate) {
        long days = (endDate.getTime() - startDate.getTime()) / (3600 * 24 * 1000);
        return (int) days;
    }

    /**
     * 获取当前天是周几
     *
     * @param date
     *
     * @return 1 2 3 4 5 6 7
     */
    public static int getDayInWeek(Date date) {
        LocalDateTime localDateTime = convert(date);
        return localDateTime.getDayOfWeek().getValue();
    }

    /**
     * 获取日期的小时数
     *
     * @param date
     *
     * @return
     */
    public static int getHourInDay(Date date) {
        LocalDateTime localDateTime = convert(date);
        return localDateTime.getHour();
    }

    /**
     * 获取日期的分钟数
     *
     * @param date
     *
     * @return
     */
    public static int getMinuteInHour(Date date) {
        LocalDateTime localDateTime = convert(date);
        return localDateTime.getMinute();
    }

    public enum DateTimeFormatterEnum {
        /**
         * 将时间格式化成 yyyy-MM-dd 例如：2017-09-12
         */
        YYYY_MM_DD(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        /**
         * 将时间格式化成 yyyy-MM-dd 例如：2017-09-12
         */
        YYYYMMDD(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        YYYYMM(DateTimeFormatter.ofPattern("yyyyMM", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        YYYY_MM(DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH).withZone(ZoneId.systemDefault())),
        MM(DateTimeFormatter.ofPattern("MM", Locale.ENGLISH).withZone(ZoneId.systemDefault())),
        /**
         * 将时间格式化成 HH:mm:ss 例如 12:09:45
         */
        HH_MM_SS(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        HH_MM(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        /**
         * 将时间格式化成 yyyy-MM-dd HH:mm:ss 例如: 2017-09-12 12:09:45
         */
        YYYY_MM_DD_HH_MM(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        /**
         * 将时间格式化成 yyyy-MM-dd HH:mm:ss 例如: 2017-09-12 12:09:45
         */
        YYYY_MM_DD_HH_MM_SS(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        /**
         * 将时间格式化成 yyyyMMddHHmmssSSS 例如: 20170912120945000
         */
        YYYY_MM_DD_HH_MM_SS_SSS(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ENGLISH).withZone(ZoneId.systemDefault())),

        /**
         * 将时间格式化成 yyyy-MM-dd HH:mm:ss 例如: 2017-09-12 12:09:45
         */
        YYYYMMDDHHMM(DateTimeFormatter.ofPattern("yyyyMMddHHmm", Locale.ENGLISH).withZone(ZoneId.systemDefault())),
        YYYYMMDDTHHMMSS(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).withZone(ZoneId.systemDefault())),
        /**
         * 将时间格式化成 dd/MMM/yyyy:HH:mm:ss Z 例如: 14/Feb/2017:14:24:56 +0800
         */
        DD_MMM_YYYY_HH_MM_SS_Z_WITH_SLASH(DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH).withZone(ZoneId.systemDefault()));

        private DateTimeFormatter dateTimeFormatter;

        DateTimeFormatterEnum(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }
    }

}
