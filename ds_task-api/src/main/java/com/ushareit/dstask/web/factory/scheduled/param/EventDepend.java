package com.ushareit.dstask.web.factory.scheduled.param;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


@Data
@NoArgsConstructor
public class EventDepend {
    private int type;
    private String taskId;
    @JSONField(name = "depend_id", alternateNames = "dependId")
    private String dependId;
    @JSONField(name = "depend_gra", alternateNames = "dependGra")
    private String dependGra;
    private String granularity;
    @JSONField(name = "use_date_calcu_param", alternateNames = "useDateCalcuParam")
    private Boolean useDateCalcuParam;
    private String metadataId;
    private Boolean isDelete = false;
    private String unitOffset;
    @JSONField(name = "date_calculation_param", alternateNames = "dateCalculationParam")
    private DataCaculationParam dateCalculationParam;

    private String taskKey;
    private String crontab;

    /**
     * 转换成与调度约定的PB数据格式
     */
    public ScheduleJobOuterClass.EventDepend toPbEventDepend() {
        ScheduleJobOuterClass.EventDepend.Builder builder = ScheduleJobOuterClass.EventDepend.newBuilder()
                .setTaskId(this.getTaskId())
                .setDependId(this.getDependId())
                .setGranularity(this.getGranularity())
                .setUseDateCalcuParam(this.getUseDateCalcuParam());

        if (this.getUnitOffset() != null) {
            builder.setUnitOffset(this.getUnitOffset());
        }

        if (this.getDateCalculationParam() != null) {
            builder.setDateCalculationParam(this.getDateCalculationParam().toPbDataCaculationParam());
        }
        return builder.build();
    }

    @Data
    @NoArgsConstructor
    @JSONType(orders = {"year", "month", "week", "day", "hour", "minute"})
    public static class DataCaculationParam {
        DateUnitParam year;
        DateUnitParam month;
        DateUnitParam week;
        DateUnitParam day;
        DateUnitParam hour;
        DateUnitParam minute;

        public void compatibleOldOffset() {
            if (this.getYear() != null) {
                this.getYear().compatibleOldOffset();
            }
            if (this.getMonth() != null) {
                this.getMonth().compatibleOldOffset();
            }
            if (this.getWeek() != null) {
                this.getWeek().compatibleOldOffset();
            }
            if (this.getDay() != null) {
                this.getDay().compatibleOldOffset();
            }
            if (this.getHour() != null) {
                this.getHour().compatibleOldOffset();
            }
            if (this.getMinute() != null) {
                this.getMinute().compatibleOldOffset();
            }
        }

        /**
         * 转换成与调度约定的PB数据格式
         */
        public ScheduleJobOuterClass.DataCaculationParam toPbDataCaculationParam() {
            ScheduleJobOuterClass.DataCaculationParam.Builder builder = ScheduleJobOuterClass.DataCaculationParam.newBuilder();
            if (this.getMonth() != null) {
                builder.setMonth(this.getMonth().toPbDataUnitParam());
            }

            if (this.getWeek() != null) {
                builder.setWeek(this.getWeek().toPbDataUnitParam());
            }

            if (this.getDay() != null) {
                builder.setDay(this.getDay().toPbDataUnitParam());
            }

            if (this.getHour() != null) {
                builder.setHour(this.getHour().toPbDataUnitParam());
            }
            return builder.build();
        }
    }

    @Data
    @NoArgsConstructor
    public static class DateUnitParam {
        String type;
        Integer offset;
        String unitOffset;
        List<String> range;

        public DateUnitParam(String type, String unitOffset, List<String> range, Integer offset) {
            this.type = type;
            this.unitOffset = unitOffset;
            this.range = range;
            this.offset = offset;
        }

        public void compatibleOldOffset() {
            if ((this.getUnitOffset() == null || this.getUnitOffset().isEmpty()) && this.getOffset() != null) {
                this.setUnitOffset(String.valueOf(this.getOffset()));
            }
        }

        /**
         * 转换成与调度约定的PB数据格式
         */
        public ScheduleJobOuterClass.DataCaculationParam.DateUnitParam toPbDataUnitParam() {
            return ScheduleJobOuterClass.DataCaculationParam.DateUnitParam.newBuilder()
                    .setType(this.getType())
                    .setUnitOffset(this.getUnitOffset())
                    .addAllRange(CollectionUtils.emptyIfNull(this.getRange()))
                    .build();
        }
    }
}


