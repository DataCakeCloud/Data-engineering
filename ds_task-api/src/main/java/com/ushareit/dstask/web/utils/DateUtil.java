package com.ushareit.dstask.web.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * author xuebotao
 * 2021-12-03
 * 日期工具类
 */
public class DateUtil {

    public static final String FORMAT_ONE = "yyyy-MM-dd HH:mm:ss";

    public static final String YEAR_MONTH_DAY_HOUR_MINUTES_SECONDS = "yyyy-MM-dd HH:mm:ss";

    public static final String YEAR_MONTH_DAY_HOUR_MINUTES = "yyyy-MM-dd HH:mm";

    public static final String YEAR_MONTH_DAY_HOUR = "yyyy-MM-dd HH";

    private static final String YEAR_MONTH_DAY = "yyyy-MM-dd";

    public static String getNowDateStr() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(YEAR_MONTH_DAY);
        return df.format(date);
    }

    public static String getDateStr(String pattern, Date date) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String getDateStr(long time) {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String current = formatter.format(date);
        return current;
    }

    public static Date getDateFromStr(String pattern, String dateStr) throws Exception {
        if (dateStr == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.parse(dateStr);
    }

    public static Date addYear(Date date, int amount) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.YEAR, amount);
        return cal.getTime();
    }

    public static Date addMonth(Date date, int amount) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MONTH, amount);
        return cal.getTime();
    }

    public static Date addDate(Date date, int amount) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, amount);
        return cal.getTime();
    }

    public static Date addHour(Date date, int amount) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.HOUR, amount);
        return cal.getTime();
    }

    public static Date addMinute(Date date, int amount) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, amount);
        return cal.getTime();
    }

    public static String firstDayOfQuarter(){
        int i=(Integer.valueOf(DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.MM))-1)%3;
        return DateTimeUtils.getDateStr(DateTimeUtils.before(new Date(),i,ChronoUnit.MONTHS), DateTimeUtils.DateTimeFormatterEnum.YYYY_MM)+"-01";
    }

    public static String lastDay(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),1, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }
    public static String lastDay(){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(new Date(),1, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String lastWeek(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),7, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String last2Week(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),14, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String lastMonth(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),30, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String last2Month(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),60, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String last3Month(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),90, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

    public static String last6Month(String date){
        return DateTimeUtils.getDateStr(DateTimeUtils.before(convertDayStr(date),180, ChronoUnit.DAYS),DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
    }

//    public static String lastMonth(String date) {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(convertDayStr(date));
//        cale.add(Calendar.MONTH, -1);
//        String day = format.format(cale.getTime());
//        return day;
//    }
//
//    public static String lastMonth() {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(new Date());
//        cale.add(Calendar.MONTH, -1);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last2Month(String date) {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(convertDayStr(date));
//        cale.add(Calendar.MONTH, -2);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last2Month() {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(new Date());
//        cale.add(Calendar.MONTH, -2);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last3Month(String date) {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(convertDayStr(date));
//        cale.add(Calendar.MONTH, -3);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last3Month() {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(new Date());
//        cale.add(Calendar.MONTH, -3);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last6Month(String date) {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(convertDayStr(date));
//        cale.add(Calendar.MONTH, -6);
//        String day = format.format(cale.getTime());
//        return day;
//    }

//    public static String last6Month() {
//        Calendar cale = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        cale.setTime(new Date());
//        cale.add(Calendar.MONTH, -6);
//        String day = format.format(cale.getTime());
//        return day;
//    }

    public static Date convertDayStr(String date){
        LocalDateTime localDateTime = DateTimeUtils.convert(DateTimeUtils.parse(date, DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD));
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static String add8Hour(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_ONE);
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
        LocalDateTime newDateTime = dateTime.plus(8, ChronoUnit.HOURS);
        return newDateTime.format(formatter);
    }
}