package com.ushareit.dstask.web.utils;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author tianxu
 * @date 2023/12/18
 **/
@Service
public class TimestampUtil {
    public static final String FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    public ArrayList<Long> getYesterday() {
        ArrayList<Long> startEnd = new ArrayList<Long>();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        long startOfPreviousDay = getStartOfPreviousDayTimestamp(yesterday);
        long endOfPreviousDay = getEndOfPreviousDayTimestamp(yesterday);

        startEnd.add(startOfPreviousDay);
        startEnd.add(endOfPreviousDay);
        return startEnd;
    }

    private long getStartOfPreviousDayTimestamp(LocalDate yesterday) {
        LocalDateTime startOfYesterday = LocalDateTime.of(yesterday, LocalTime.MIN);
        return startOfYesterday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private long getEndOfPreviousDayTimestamp(LocalDate yesterday) {
        LocalDateTime endOfYesterday = LocalDateTime.of(yesterday, LocalTime.MAX);
        return endOfYesterday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public String formatYesterday(Long yesterday) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(yesterday), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    public String formatTimestamp(Long now) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_NOW);
        return localDateTime.format(formatter);
    }
}
