package com.ushareit.dstask.web.factory.flink.param;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.Strategy;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class RuntimeConfig {
    private Integer parallelism;
    private String huaweiIam;
    private String awsIam;
    private Double tmCpu;
    private Double tmMemory;
    private String owner;
    private String dsGroups;
    private List<String> collaborators;
    private String emails;
    private List<Integer> alertType;
    private List<Kv> params;

    private Boolean checkpoint = false;
    private Integer checkpointInterval;
    private Integer checkpointTimeout;
    private String checkpointMode;
    private List<Column> columns;
    private String partitionKeys;
    private String primaryKey;
    private String sourceType;
    /**
     * 通过何种方式创建任务，目前有两种：task/workflow
     */
    private String source;
    private String content;
    private Boolean isAutoScaleMode = false;
    private String groupId;

    private String topic;
    private String bootstrapServerUri;

    private Long lowerBound;
    private Long upperBound;

    // shareStore 相关
    private String restEndpoint;
    private String clusterLoad;
    private String segmentLoad;

    //策略实现
    private List<Strategy> strategyList = new ArrayList<>();

    //成本归属
//    public List<Cost> cost = new ArrayList<>();

    private Boolean isBatchTask = false;

    //Python shell模板相关
    private String sourceRegion;
    private String image;
    private String batchParams;

    private List<Table> tables;
    private String connectionUrl;
    private String dbUser;
    private String dbPassword;
    private String sourceDb;
    //group-offsets,earliest-offset,latest-offset
    private String startConumerPosition ="group-offsets";
    private String consumerGroup;
    private String targetDb;
    private Integer syncType;
    private String mysqlCdcType = "latest-offset";
    private String lakeHouseType = "ICEBERG";  //PAIMON
    private Integer actorId;
    private String bootstrap;

    public Actor sourceActor;

    public Boolean isNewFormat = false;
    public class Kv {
        public String key;
        public String value;

        public Kv() {

        }

        public Kv(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

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
}
