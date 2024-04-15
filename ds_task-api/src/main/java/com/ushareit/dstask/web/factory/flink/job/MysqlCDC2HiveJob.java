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
import com.ushareit.dstask.web.ddl.model.MysqlCDCDdl;

import java.util.List;


/**
 * @author wuyan
 * @date 2021/8/5
 */
public class MysqlCDC2HiveJob extends SinkIcebergJob {

    private static final String ICEBERG_FORMAT_VERSION = "2";

    public MysqlCDC2HiveJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    @Override
    protected String getIcebergFormatVersion() {
        return ICEBERG_FORMAT_VERSION;
    }

    @Override
    protected String getSet() {
        String configByParentCode = taskServiceImp.sysDictService.getConfigByParentCode(DsTaskConstant.TEMPLATE_DEP, TemplateEnum.MysqlCDC2Hive.name());
        return String.format("SET flink.execution.packages=%s; \n", configByParentCode);
    }

    @Override
    protected SqlDdl getSourceSqlDdl() {
        Table sourceTable = runtimeConfig.getTables().get(0);
        sourceTable.setName(sourceTable.getSourceTable());
        sourceTable.setUrl(runtimeConfig.getConnectionUrl());
        sourceTable.setUsername(runtimeConfig.getDbUser());
        sourceTable.setPassword(runtimeConfig.getDbPassword());
        sourceTable.setRegion(runtimeConfig.getSourceRegion());
        sourceTable.setDbName(runtimeConfig.getSourceDb());
        sourceTable.setPrimaryKey(getPkColumn(sourceTable));
        return new MysqlCDCDdl(sourceTable, runtimeConfig.getMysqlCdcType());
    }

    @Override
    protected SqlDdl getSinkSqlDdl(String SourceTableName, String region) {
        Table sourceTable = runtimeConfig.getTables().get(0);
        Table sinkTable = new Table();
        sinkTable.setColumns(sourceTable.getColumns());
        sinkTable.setDbName(runtimeConfig.getTargetDb());
        sinkTable.setRegion(runtimeConfig.getSourceRegion());
        sinkTable.setName(sourceTable.getTargetTable());
        sinkTable.setPrimaryKey(getPkColumn(sourceTable));
        return new IcebergDdl(sinkTable, getIcebergFormatVersion(), sinkTable.getPrimaryKey(), null, taskServiceImp);
    }

    private String getPkColumn(Table table){
        List<Column> columns = table.getColumns();
        for (Column column:columns) {
            if (column.getIsPK())
                return column.getColumnName();
        }
        return null;
    }
}
