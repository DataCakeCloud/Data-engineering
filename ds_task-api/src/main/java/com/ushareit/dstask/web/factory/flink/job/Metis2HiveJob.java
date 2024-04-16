package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.ddl.DdlFactory;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.ddl.model.IcebergDdl;
import com.ushareit.dstask.web.ddl.model.MetisDdl;
import com.ushareit.dstask.web.factory.flink.job.Kafka2HiveJob;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author wuyan
 * @date 2021/8/5
 */
@Slf4j
public class Metis2HiveJob extends Kafka2HiveJob {
    private static final String METIS_DATEPART_SQL = "DATE_FORMAT(metis_message_context.metis_timestamp, 'yyyyMMdd') as datapart,";

    private static final String METIS_HOUR_SQL = "DATE_FORMAT(metis_message_context.metis_timestamp, 'HH') as `hour` ";

    public Metis2HiveJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    @Override
    protected SqlDdl getSourceSqlDdl() {
        // 用户传过来的列
        List<Column> columns = runtimeConfig.getColumns();
        if (columns == null || columns.size() == 0) {
            throw new RuntimeException("Metis2HiveJob必须传Columns或SQL");
        }

        Table sourceTable = DdlFactory.getMetadataTableByQualifiedName(inputDatasets.get(0));
        columns.stream().forEach(column -> {
            if (column.getType().toUpperCase().contains("ROW") || column.getType().toUpperCase().contains("ARRAY") || column.getType().toUpperCase().contains("MAP")) {
                column.setType("STRING");
            }
        });
        sourceTable.setColumns(columns);
        // 再次将类型转换后的Column设置进runtimeConfig供后续使用
        runtimeConfig.setColumns(columns);
        return new MetisDdl(sourceTable, runTimeTaskBase.getId());
    }

    @Override
    protected SqlDdl getSinkSqlDdl(String SourceTableName, String region) {
        String targetName = getTargetTableName(SourceTableName, "%s.%s.metis_%s_inc_hourly");
        Table targetTable = new Table().setName(targetName).setColumns(runtimeConfig.getColumns());
        String partitionKeys = runtimeConfig.getPartitionKeys();
        return new IcebergDdl(targetTable, getIcebergFormatVersion(), inputDatasets.get(0).getPrimaryKey(), partitionKeys, taskServiceImp);
    }

    @Override
    public Object getSelectColumns(List<Column> columns) {
        StringBuffer sb = getBaseSelectColumns(columns);
        sb.append(METIS_DATEPART_SQL).append("\n");
        sb.append(METIS_HOUR_SQL);
        return sb.toString();
    }
}
