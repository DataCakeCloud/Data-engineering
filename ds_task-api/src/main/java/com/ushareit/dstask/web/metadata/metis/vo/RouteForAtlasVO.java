package com.ushareit.dstask.web.metadata.metis.vo;

import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/6/2
 */
@Slf4j
@Data
public class RouteForAtlasVO {

    private String appName;

    private String clusterName;

    private Boolean containDataSchema;

    private List<FieldForAtlasVO> logFormat;

    private String groupName;

    private String region;

    private String customTimeField;

    private Long dailyTotalNum;

    private Double dailyTotalSize;

    private Integer dailyUserReportNum;

    private String dataType;

    private Integer dau;

    private String isDegrade;

    private Long degradeStart;

    private Long degradeEnd;

    private String description;

    private Integer esSinkId;

    private String esIndexName;

    private Integer esStoreDays;

    private Integer esThreshold;

    private Double inbound;

    private Double inboundTimeByMinute;

    private Double inboundTimeByHour;

    private String logStore;

    private String logFrom;

    private String logSample;

    private String parseFormat;

    private String principal;

    private Double sampleValue;

    private String sampleType;

    private Double tps;

    private Double sizeAvg;

    private Double tpsTime;

    public Table toTable() {
        Table table = new Table();
        table.setRegion(this.region);
        table.setSourceType("metis");
        table.setTypeName("metis");
        table.setQualifiedName(String.format("%s@%s@%s", this.logStore, this.groupName, this.appName));

        Map<String, String> parameters = new HashMap<>();
        parameters.put("appName", this.appName);
        parameters.put("groupName", this.groupName);
        parameters.put("logStore", this.logStore);
        parameters.put("region", this.region);
        table.setParameters(parameters);
        table.setColumns(CollectionUtils.emptyIfNull(logFormat).stream()
                .map(item -> {
                    Column column = new Column();
                    column.setName(item.getField());
                    column.setType(item.getType());
                    column.setPosition(0);
                    return column;
                }).collect(Collectors.toList()));
        return table;
    }

}
