package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Column;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/3/9
 */
public class DataLakeJob extends FlinkSqlJob {
    public DataLakeJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    protected String getSet() {
        return null;
    }

    protected SqlDdl getSourceSqlDdl() {
        return null;
    }

    protected String getInsertSql(SqlDdl sourceDdl, SqlDdl sinkDdl) throws Exception {
        return null;
    }

    protected SqlDdl getSinkSqlDdl(String SourceTableName, String region) {
        return null;
    }
}
