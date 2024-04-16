package com.ushareit.engine.param;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.List;

@Data
public class RuntimeConfig {
    private String sourceRegion;
    private String sourceType;
    private String executeMode;
    private String sourceId;
    private String destinationId;
    private String destinationType;
    private Catalog catalog;
    private AdvanceParam advancedParameters;
    private String alertModel;
    private List<Cost> cost;
    private String regularAlert;
    private String createTableSql;
    public List<String> preSql;
    public List<String> postSql;

    public TaskParam taskParam;
    public String sourceParam;




    @Data
    public static class Cost {
        public String key;
        public String value;
        public Integer currentGroup = 0;

        public Cost() {

        }

        public Cost(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @Data
    public static class TaskParam {
        String location;
        String tableComment;
        String tableLevel;
        String writeMode;
        String path;
        String fieldDelimiter;
        String fileType;
        String syncmode;
        Boolean nullPointExit;
        int flushInterval;
        int maxBatchRows;
        int batchSize;
        int bandwidth;
    }


    @Data
    public static class AdvanceParam {
        public String userGroupName;
        public Integer parallelism;
        public String huaweiIam;
        public String awsIam;
        private Double tmCpu;
        public Double tmMemory;
        public String owner;
        public String dsGroups;
        public List<String> collaborators;
        public String emails;
        public List<Kv> params;
        public Boolean checkpoint;
        public Integer checkpointInterval;
        public Integer checkpointTimeout;
        public String checkpointMode;
        public String sourceType;
        /**
         * 通过何种方式创建任务，目前有两种：task/workflow
         */
        public String source;
        public String content;
        public Boolean isAutoScaleMode = false;
        public String groupId;

        public String topic;
        public String bootstrapServerUri;

        public Long lowerBound;
        public Long upperBound;
        private String sourceDb="";
        private List<Table> tables;
        private String targetDb;
        private String connectionUrl;
        private String dbUser;
        private String dbPassword;
        private String dataOriginType;
        private Boolean existDatabase = true;
        private String engineConfig;

        // shareStore 相关
        public String restEndpoint;
        public String clusterLoad;
        public String segmentLoad;

        public Boolean isBatchTask = false;

        //Python shell模板相关
        public String sourceRegion;
        public String image;
        public String batchParams;

        public String startDate;
        public String endDate;

        public Integer maxActiveRuns;
        public Integer executionTimeout = 0;
        public Integer retries = 3;
        public Integer retryInterval = 0;

        public Boolean isNewFormat = false;

        public String clusterSla = "normal";

        public String cluster;

        public String resourceLevel;
        public String lifecycle;
        public String acrossCloud = "common";
        public Boolean existTargetTable ;
        private String cmds;
        private Integer syncType;
        private String exportMode;
        private Integer checkExpirationTime = 24;
        private String lakeHouseType = "ICEBERG";

        public static class Kv {
            public String key;
            public String value;

            public Kv() {

            }

            public Kv(String key, String value) {
                this.key = key;
                this.value = value;
            }
        }
    }

    public static RuntimeConfig convert(String oldStrJson) {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        AdvanceParam advanceParam = JSON.parseObject(oldStrJson, AdvanceParam.class);
        runtimeConfig.setAdvancedParameters(advanceParam);
        runtimeConfig.setSourceRegion(advanceParam.getSourceRegion());
        runtimeConfig.setSourceType(advanceParam.getSourceType());
        return runtimeConfig;
    }


}
