package com.ushareit.dstask.third.scmp.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author fengxiao
 * @date 2022/3/10
 */
@Data
public class SCMPOnDutyItem implements Serializable {
    private static final long serialVersionUID = -1277284618428462283L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private String start;
    private String end;
    private List<Integer> members;

    public boolean onDuty(long timeMillis) {
        LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);

        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        return currentSecond >= startTime.toEpochSecond(ZoneOffset.UTC)
                && currentSecond <= endTime.toEpochSecond(ZoneOffset.UTC);
    }
}
