package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.bean.CrontabParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CrontabUtil {


    public static String getCrontab(CrontabParam crontabParam){
        switch (crontabParam.getCycle()){
            case "minutely":
                return minutelyCrontabGenerate(crontabParam);
            case "hourly":
                return hourlyCrontabGenerate(crontabParam);
            case "daily":
                return dailyCrontabGenerate(crontabParam);
            case "weekly":
                return weeklyCrontabGenerate(crontabParam);
            case "monthly":
                return monthlyCrontabGenerate(crontabParam);
        }
        return "";
    }

    public static String minutelyCrontabGenerate(CrontabParam crontabParam){
        if(crontabParam.getInterval() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        String minuteCrontabTemplate = "{0} {1} * * *";
        String startTime = "00:00";
        String endTime = "23:59";
        if (crontabParam.getAdvancedSetting()) {
            if (StringUtils.isNotBlank(crontabParam.getStartTime())){
                startTime = crontabParam.getStartTime();
            }
            if (StringUtils.isNotBlank(crontabParam.getEndTime())){
                endTime = crontabParam.getEndTime();
            }
            String minuteUnit = MessageFormat.format("*/{0}", crontabParam.getInterval());
            String dayUnit = MessageFormat.format("{0}-{1}", startTime.split(":")[0], endTime.split(":")[0]);
            return MessageFormat.format(minuteCrontabTemplate, minuteUnit, dayUnit);
        } else {
            String minuteUnit = MessageFormat.format("*/{0}", crontabParam.getInterval());
            return MessageFormat.format(minuteCrontabTemplate, minuteUnit,'*');
        }


    }

    public static String hourlyCrontabGenerate(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }

        String hourlyCrontabTemplate = "{0} {1} * * *";
        String hourUnit = "*";
        String minuteUnit = crontabParam.getFixedTime();
        String startTime = "00:00";
        String endTime = "23:59";
        if (!crontabParam.getAdvancedSetting()){
            return MessageFormat.format(hourlyCrontabTemplate,minuteUnit,hourUnit);
        }

        if (crontabParam.getRange() != null && crontabParam.getRange().size() > 0 ){
            hourUnit = StringUtils.join(crontabParam.getRange(),",");
        }else{
            if (StringUtils.isNotBlank(crontabParam.getStartTime())){
                startTime = crontabParam.getStartTime();
            }
            if (StringUtils.isNotBlank(crontabParam.getEndTime())){
                endTime = crontabParam.getEndTime();
            }

            String range = MessageFormat.format("{0}-{1}",startTime.split(":")[0],endTime.split(":")[0]);
            hourUnit = MessageFormat.format("{0}/{1}",range,crontabParam.getInterval());
        }

        return MessageFormat.format(hourlyCrontabTemplate,minuteUnit,hourUnit);
    }

    public static String dailyCrontabGenerate(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        String dailyCrontabTemplate = "{0} {1} * * *";
        String hourUnit = crontabParam.getFixedTime().split(":")[0];
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        return MessageFormat.format(dailyCrontabTemplate,minuteUnit,hourUnit);
    }

    public static String weeklyCrontabGenerate(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null || crontabParam.getRange() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        String weeklyCrontabTemplate = "{0} {1} * * {2}";
        String hourUnit = crontabParam.getFixedTime().split(":")[0];
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        String weekUnit = StringUtils.join(crontabParam.getRange(),",");
        return MessageFormat.format(weeklyCrontabTemplate,minuteUnit,hourUnit,weekUnit);
    }

    public static String monthlyCrontabGenerate(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null || crontabParam.getRange() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        String monthlyCrontabTemplate = "{0} {1} {2} * *";
        String hourUnit = crontabParam.getFixedTime().split(":")[0];
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        String dayUnit = StringUtils.join(crontabParam.getRange(),",");
        return MessageFormat.format(monthlyCrontabTemplate,minuteUnit,hourUnit,dayUnit);
    }


    public static String getCrontabUtc0(CrontabParam crontabParam){
        switch (crontabParam.getCycle()){
            case "minutely":
                return minutelyCrontabGenerateUtc0(crontabParam).replace("23-23","23").replace("0-0","0");
            case "hourly":
                return hourlyCrontabGenerateUtc0(crontabParam).replace("23-23","23").replace("0-0","0");
            case "daily":
                return dailyCrontabGenerateUtc0(crontabParam);
            case "weekly":
                return weeklyCrontabGenerateUtc0(crontabParam);
            case "monthly":
                return monthlyCrontabGenerateUtc0(crontabParam);
        }
        return "";
    }

    public static String minutelyCrontabGenerateUtc0(CrontabParam crontabParam){
        if(crontabParam.getInterval() == null || crontabParam.getStartTime() == null || crontabParam.getEndTime() == null ){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }

        String minuteCrontabTemplate = "{0} {1} * * *";
        String minuteUnit = MessageFormat.format("*/{0}",crontabParam.getInterval());
        Integer startHour = Integer.valueOf(crontabParam.getStartTime().split(":")[0]);
        Integer endHour = Integer.valueOf(crontabParam.getEndTime().split(":")[0]);

        if (startHour == 0 && endHour == 23){
            String hourUnit = MessageFormat.format("{0}-{1}",startHour,endHour);
            return MessageFormat.format(minuteCrontabTemplate,minuteUnit,hourUnit);
        }

        Boolean startCarry = false;
        Boolean endCarry = false;

        startHour = startHour - 8;
        endHour = endHour - 8;
        if (startHour  < 0){
            startCarry = true;
            startHour = startHour + 24;
        }

        if (endHour < 0){
            endCarry = true;
            endHour = endHour + 24;
        }

        if (endCarry){
            String hourUnit = MessageFormat.format("{0}-{1}",startHour,endHour);
            return MessageFormat.format(minuteCrontabTemplate,minuteUnit,hourUnit);
        }

        if (!endCarry && startCarry){
            String hourUnit = MessageFormat.format("0-{0},{1}-23",endHour,startHour);
            return MessageFormat.format(minuteCrontabTemplate,minuteUnit,hourUnit);
        }

        String hourUnit = MessageFormat.format("{0}-{1}",startHour,endHour);
        return MessageFormat.format(minuteCrontabTemplate,minuteUnit,hourUnit);
    }

    public static String hourlyCrontabGenerateUtc0(CrontabParam crontabParam){
        if((crontabParam.getRange() == null && crontabParam.getInterval() == null )|| crontabParam.getStartTime() == null || crontabParam.getEndTime() == null ){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }

        String hourlyCrontabTemplate = "{0} {1} * * *";
        String hourUnit = "";
        String minuteUnit = "00";
        if (crontabParam.getRange() != null && crontabParam.getRange().size() > 0 ){
            List<String> ranges = crontabParam.getRange().stream().map(
                    x->{
                        return String.valueOf((Integer.valueOf(x) - 8 + 24 ) % 24);
                    }
            ).collect(Collectors.toList());
            hourUnit = StringUtils.join(ranges,",");
        }else{
            minuteUnit = crontabParam.getStartTime().split(":")[1];
            Integer startHour = Integer.valueOf(crontabParam.getStartTime().split(":")[0]);
            Integer endHour = Integer.valueOf(crontabParam.getEndTime().split(":")[0]);

            if (startHour == 0 && endHour == 23){
                hourUnit = MessageFormat.format("0-23/{0}",crontabParam.getInterval());
                return MessageFormat.format(hourlyCrontabTemplate,minuteUnit,hourUnit);
            }
            int nums = (endHour - startHour) / crontabParam.getInterval();
            List<Integer> hourArr = new ArrayList<>();
            hourArr.add(startHour);
            int tempHour = startHour;
            for (int i = 0; i < nums; i++) {
                tempHour = tempHour + crontabParam.getInterval();
                hourArr.add(tempHour);
            }

            List<String> ranges = hourArr.stream().map(
                    x->{
                        return String.valueOf((x - 8 + 24 ) % 24);
                    }
            ).collect(Collectors.toList());
            hourUnit = StringUtils.join(ranges,",");
        }

        return MessageFormat.format(hourlyCrontabTemplate,minuteUnit,hourUnit);
    }

    public static String dailyCrontabGenerateUtc0(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        String dailyCrontabTemplate = "{0} {1} * * *";
        Integer startHour = Integer.valueOf(crontabParam.getFixedTime().split(":")[0]);
        String hourUnit = String.valueOf((startHour - 8 + 24 ) % 24);
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        return MessageFormat.format(dailyCrontabTemplate,minuteUnit,hourUnit);
    }

    public static String weeklyCrontabGenerateUtc0(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null || crontabParam.getRange() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        Boolean startCarry = false;

        String weeklyCrontabTemplate = "{0} {1} * * {2}";
        Integer startHour = Integer.valueOf(crontabParam.getFixedTime().split(":")[0]);
        if (startHour - 8 < 0){
            startCarry = true;
            startHour = (startHour - 8 + 24) % 24;
        }
        String hourUnit = String.valueOf(startHour);
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        List<String> ranges ;
        if (startCarry){
            ranges = crontabParam.getRange().stream().map(
                    x -> {
                        int day =  Integer.valueOf(x) - 1;
                        if (day == 0){
                            return "7";
                        }else {
                            return String.valueOf(day);
                        }
                    }
            ).collect(Collectors.toList());
        } else {
            ranges = crontabParam.getRange();
        }
        String weekUnit = StringUtils.join(ranges,",");
        return MessageFormat.format(weeklyCrontabTemplate,minuteUnit,hourUnit,weekUnit);
    }

    public static String monthlyCrontabGenerateUtc0(CrontabParam crontabParam){
        if(crontabParam.getFixedTime() == null || crontabParam.getRange() == null){
            log.warn("CrontabGenerate miss param");
            return "0 0 0 0 0";
        }
        Boolean startCarry = false;
        String monthlyCrontabTemplate = "{0} {1} {2} * *";
        Integer startHour = Integer.valueOf(crontabParam.getFixedTime().split(":")[0]);
        if (startHour - 8 < 0){
            startCarry = true;
            startHour = (startHour - 8 + 24) % 24;
        }
        String hourUnit = String.valueOf(startHour);
        String minuteUnit = crontabParam.getFixedTime().split(":")[1];
        List<String> ranges ;
        if (startCarry) {
            ranges = crontabParam.getRange().stream().map(
                    x -> {
                        if (x.equals("1")) {
                            return "L";
                        } else if (x.equals("L")) {
                            return "L-1";
                        } else {
                            return String.valueOf((Integer.valueOf(x) -1));
                        }
                    }
            ).collect(Collectors.toList());
        } else {
            ranges = crontabParam.getRange();
        }

        String dayUnit = StringUtils.join(ranges,",");
        return MessageFormat.format(monthlyCrontabTemplate,minuteUnit,hourUnit,dayUnit);
    }
}
