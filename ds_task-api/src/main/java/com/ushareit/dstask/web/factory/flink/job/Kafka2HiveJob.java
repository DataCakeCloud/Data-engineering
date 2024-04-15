package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.ddl.DdlFactory;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.ddl.model.IcebergDdl;
import com.ushareit.dstask.web.ddl.model.KafkaDdl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2021/11/2
 */
@Slf4j
public class Kafka2HiveJob extends SinkIcebergJob {

    private static final String SET = "SET flink.execution.packages=org.apache.flink:flink-connector-kafka_2.11:1.13.3,org.apache.flink:flink-json:1.13.3;\n";


    private static final String ICEBERG_FORMAT_VERSION = "1";

    public Kafka2HiveJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    @Override
    protected String getSet() {
        String configByParentCode = taskServiceImp.sysDictService.getConfigByParentCode(DsTaskConstant.TEMPLATE_DEP, TemplateEnum.Kafka2Hive.name());
        return String.format("SET flink.execution.packages=%s;\n", configByParentCode);
    }

    @Override
    protected String getIcebergFormatVersion() {
        return ICEBERG_FORMAT_VERSION;
    }

    @Override
    protected SqlDdl getSourceSqlDdl() {
        // 用户传过来的列
        List<Column> columns = runtimeConfig.getColumns();
        if (columns == null || columns.size() == 0) {
            throw new RuntimeException("Metis2HiveJob必须传Columns或SQL");
        }

        String topic = runtimeConfig.getTopic();
        String bootstrapServerUri = runtimeConfig.getBootstrapServerUri();
        Map<String, String> parameters = new HashMap<>(1);
        parameters.put("uri", bootstrapServerUri);
        Table sourceTable = new Table();

        columns.stream().forEach(column -> {
            if (column.getType().toUpperCase().contains("ROW") || column.getType().toUpperCase().contains("ARRAY") || column.getType().toUpperCase().contains("MAP")) {
                column.setType("STRING");
            }
        });
        sourceTable.setColumns(columns).setName(topic).setParameters(parameters);
        // 再次将类型转换后的Column设置进runtimeConfig供后续使用
        runtimeConfig.setColumns(columns);
        return new KafkaDdl(sourceTable, runTimeTaskBase.getId());
    }

    @Override
    protected SqlDdl getSinkSqlDdl(String SourceTableName, String region) {
        String targetName = getTargetTableName(SourceTableName, "%s.%s.kafka_%s_inc_hourly");
        Table targetTable = new Table().setName(targetName).setRegion(region).setColumns(runtimeConfig.getColumns());
        String partitionKeys = runtimeConfig.getPartitionKeys();
        return new IcebergDdl(targetTable, getIcebergFormatVersion(), inputDatasets.get(0).getPrimaryKey(), partitionKeys, taskServiceImp);
    }
}
