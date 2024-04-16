package com.ushareit.dstask.web.autoscale.strategy;


import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.web.autoscale.Context;
import lombok.extern.slf4j.Slf4j;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * author xuebotao
 * date 2022-06-14
 * 周期性策略
 */
@Slf4j
public class PeriodicStrategy extends AbstractStrategy {

    public PeriodicStrategy(TaskScaleStrategy taskScaleStrategy, Context context) {
        super(taskScaleStrategy, context);
    }

    /**
     * 获取建议并行度
     *
     * @return
     */
    @Override
    public Integer getAdvicePar() {
        Integer advicePar = 1;
        List<Strategy.PeriodRule> periodicList = strategy.getPeriodicList();
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("H");
        String hour = format.format(date);
        int hour_8 = Integer.parseInt(hour) > 15 ? (Integer.parseInt(hour) + 8) % 24 : Integer.parseInt(hour) + 8;
        for (Strategy.PeriodRule periodRule : periodicList) {
            int startTime = Integer.parseInt(periodRule.getStartTime());
            int endTime = Integer.parseInt(periodRule.getEndTime());
            if (hour_8 >= startTime && hour_8 < endTime) {
                advicePar = periodRule.getPar();
            }
        }
        return advicePar;
    }

}
