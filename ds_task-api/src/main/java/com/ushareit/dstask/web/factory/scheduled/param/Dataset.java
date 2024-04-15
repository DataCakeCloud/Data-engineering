package com.ushareit.dstask.web.factory.scheduled.param;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.utils.SpringUtil;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
@Data
public class Dataset {

    private String id;
    private String guid;
    private Boolean placeholder;
    private String granularity;
    private int offset;
    @JSONField(name = "ready_time", alternateNames = "readyTime")
    private String readyTime;
    @JSONField(name = "success_time", alternateNames = "successTime")
    private String successTime;
    private Metadata metadata;
    private String location;
    private List<String> partitions;
    private String fileName;
    @JSONField(name = "check_path", alternateNames = "checkPath")
    private String checkPath;
    private String primaryKey;
    @JSONField(name = "detailed_gra", alternateNames = "detailedGra")
    private String detailedGra;
    @JSONField(name = "detailed_dependency", alternateNames = "detailedDependency")
    private List<String> detailedDependency;
    @JSONField(name = "use_date_calcu_param", alternateNames = "useDateCalcuParam")
    private Boolean useDateCalcuParam;
    private String unitOffset;
    @JSONField(name = "date_calculation_param", alternateNames = "dateCalculationParam")
    private EventDepend.DataCaculationParam dateCalculationParam;

    public void compatibleOldOffset() {
        if (this.getUseDateCalcuParam() == null) {
            SchedulerServiceImpl schedulerService = SpringUtil.getBean(SchedulerServiceImpl.class);
            JSONObject datasetResp = schedulerService.getDatasetInfo(this.getMetadata().toString());
            String outputGra;
            if (datasetResp.getInteger("is_external") == 1) {
                if (this.getDetailedGra() != null && !this.getDetailedGra().isEmpty()) {
                    outputGra = this.getDetailedGra();
                } else {
                    outputGra = this.getGranularity();
                }
            } else {
                outputGra = datasetResp.getString("granularity");
                if (outputGra.equals(this.getGranularity())) {
                    if (this.getDetailedGra() == null) {
                        this.setUnitOffset(String.valueOf(this.getOffset()));
                        this.setUseDateCalcuParam(false);
                    }
                }
            }
            if (!outputGra.equals(this.getGranularity())) {
                this.setUseDateCalcuParam(true);
                this.offset2DateCalcuParam(this.getGranularity(), String.valueOf(this.getOffset()), this.getDetailedDependency());
                this.setGranularity(outputGra);
            } else {
                this.setUseDateCalcuParam(false);
                this.setUnitOffset(String.valueOf(this.getOffset()));
            }


        }
    }

    /**
     * 将旧的数据前置依赖的调度配置转换为新的, 吐了...
     *
     * @param gra
     * @param offset
     * @param detailedDependency
     */
    private void offset2DateCalcuParam(String gra, String offset, List<String> detailedDependency) {
        Boolean isAll = false;
        EventDepend.DataCaculationParam dataCaculationParam = new EventDepend.DataCaculationParam();
        List<String> range = detailedDependency;
        String detailedD = "";
        if (detailedDependency != null && detailedDependency.size() > 0) {
            detailedD = String.join(",", detailedDependency);
            if (detailedD.toUpperCase().contains("ALL")) {
                isAll = true;
            }
        } else {
            isAll = true;
        }

        if (gra.equals("hourly")) {

            dataCaculationParam.setMonth(new EventDepend.DateUnitParam("offset", "0", null, 0));
            dataCaculationParam.setDay(new EventDepend.DateUnitParam("offset", "0", null, 0));
            dataCaculationParam.setHour(new EventDepend.DateUnitParam("offset", offset, null, 0));
        }
        if (gra.equals("daily")) {
            if (isAll) {
                range = Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "0"});
            }
            dataCaculationParam.setMonth(new EventDepend.DateUnitParam("offset", "0", null, 0));
            dataCaculationParam.setDay(new EventDepend.DateUnitParam("offset", offset, null, 0));
            dataCaculationParam.setHour(new EventDepend.DateUnitParam("range", offset, range, 0));
        }
        if (gra.equals("weekly")) {
            if (isAll) {
                range = Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7"});
            }
            dataCaculationParam.setMonth(new EventDepend.DateUnitParam("offset", "0", null, 0));
            dataCaculationParam.setWeek(new EventDepend.DateUnitParam("offset", offset, null, 0));
            dataCaculationParam.setDay(new EventDepend.DateUnitParam("range", offset, range, 0));
        }
        if (gra.equals("monthly")) {
            if (isAll) {
                range = Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"});
            }
            dataCaculationParam.setMonth(new EventDepend.DateUnitParam("offset", offset, null, 0));
            dataCaculationParam.setDay(new EventDepend.DateUnitParam("range", offset, range, 0));
        }
        this.setDateCalculationParam(dataCaculationParam);

    }

    /**
     * 转换成于调度约定的数据格式
     */
    public ScheduleJobOuterClass.Dataset toPbDataset() {
        ScheduleJobOuterClass.Dataset.Builder builder = ScheduleJobOuterClass.Dataset.newBuilder()
                .setId(this.getId())
                .setOffset(this.getOffset());

        if (this.getUseDateCalcuParam() != null) {
            builder.setUseDateCalcuParam(this.getUseDateCalcuParam());
        }

        if (this.getGranularity() != null) {
            builder.setGranularity(this.getGranularity());
        }
        // 就绪时间更新了一种新的内容格式，使用新的字段代替旧字段
        if (this.getSuccessTime() != null) {
            builder.setReadyTime(this.getSuccessTime());
        }

        if (this.getLocation() != null) {
            builder.setLocation(this.getLocation());
        }

        if (this.getFileName() != null) {
            builder.setFileName(this.getFileName());
        }

        if (this.getCheckPath() != null) {
            builder.setCheckPath(this.getCheckPath());
        }

        if (this.getUnitOffset() != null) {
            builder.setUnitOffset(this.getUnitOffset());
        }

        if (this.getDateCalculationParam() != null) {
            builder.setDataCaculationParam(this.getDateCalculationParam().toPbDataCaculationParam());
        }

        return builder.build();
    }

    @Data
    public static class Metadata {
        private String type;
        private String region;
        private String source;
        private String db;
        private String table;

        //展示使用这个
        private String sourceName;

        @Override
        public String toString() {
            String newTable = this.table == null ? "" : this.table.trim();
            String newDb = this.db == null ? "" : this.db.trim();
            switch (type) {
                case "mysql":
                case "clickhouse":
                    return String.format("%s.%s@%s",newDb, newTable ,region);
                case "kafka":
                    return String.format("%s.%s.%s.%s", type, region, source, newTable);
                case "hive":
                    if (StringUtils.isNotBlank(newTable) && StringUtils.isNotBlank(newDb) && StringUtils.isNotBlank(this.region)) {
                        return String.format("%s.%s@%s", newDb, newTable, region);
                    }

                    if (StringUtils.isNotBlank(newTable)) {
                        return newTable;
                    }

                    return StringUtils.EMPTY;
                case "metis":
//                    return String.format("%s@%s@%s@%s@%s", source, db.trim(), table.trim(), region, type);
                    // 解决元数据返回的table中本就包含source/db/region/type
                    return newTable;
                case "redshift":
                    return String.format("redshift.%s.%s.%s", region, newDb, newTable);
                case "tikv":
                    return String.format("tikv.%s.%s.%s", region, newDb, newTable);
                default:
                    return String.format("%s.%s@%s",newDb, newTable ,region);
            }
        }
    }
}
