package com.ushareit.dstask.constant;

import org.apache.commons.lang3.StringUtils;
import tk.mybatis.mapper.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum SchedulerCycleEnum {
    /**
     * 数据源类型
     */
    YEARLY("yearly"),
    MONTHLY("monthly"),
    WEEKLY("weekly"),
    DAILY("daily"),
    HOURLY("hourly"),
    MINUTELY("minutely");

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    SchedulerCycleEnum(String type) {
        this.type = type;
    }

    public static boolean isValid(String type) {
        for (SchedulerCycleEnum tmpType : SchedulerCycleEnum.values()) {
            if (tmpType.type.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 大于等于返回True
     * 小于返回False
     * @param e
     * @return
     */
    public boolean compare(SchedulerCycleEnum e) {
        int i = this.compareTo(e);
        if (i <= 0) {
            return true;
        } else {
            return false;
        }
    }
    public static String GetLowerCycleStr(SchedulerCycleEnum e){
        List<String> result = new ArrayList<>();
        if(e.compare(SchedulerCycleEnum.MONTHLY)){
            result.add(SchedulerCycleEnum.MONTHLY.getType());
        }
        if(e.compare(SchedulerCycleEnum.DAILY)){
            result.add(SchedulerCycleEnum.DAILY.getType());
        }
        if(e.compare(SchedulerCycleEnum.HOURLY)){
            result.add(SchedulerCycleEnum.HOURLY.getType());
        }
        if(e.compare(SchedulerCycleEnum.MINUTELY)){
            result.add(SchedulerCycleEnum.MINUTELY.getType());
        }
        return StringUtils.join(result,",");
    }

}
