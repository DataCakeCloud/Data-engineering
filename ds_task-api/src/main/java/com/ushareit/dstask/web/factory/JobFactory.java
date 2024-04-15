package com.ushareit.dstask.web.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.flink.job.*;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.scheduled.*;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
public class JobFactory {

    public static Job getJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImpl) {

        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        Boolean isBatchTask = runtimeConfig.getAdvancedParameters().getIsBatchTask();
        JSONObject runtimeConfigObject = JSON.parseObject(task.getRuntimeConfig());
        Boolean isSharestore = runtimeConfigObject.getBoolean("isSharestore");
        Boolean isTable = runtimeConfigObject.getBoolean("isTable");
        boolean isTableList = (boolean) runtimeConfigObject.getOrDefault("isTableList", false);
        Integer syncType = runtimeConfig.getAdvancedParameters().getSyncType();
        switch (TemplateEnum.valueOf(task.getTemplateCode())) {
            case StreamingJAR:
                return isBatchTask ? new FlinkBatchJarJob(task, tagId, savepointId, taskServiceImpl) : new FlinkJarJob(task, tagId, savepointId, taskServiceImpl);
            case StreamingSQL:
                return isBatchTask ? new FlinkBatchSqlJob(task, tagId, savepointId, taskServiceImpl) : new FlinkSqlJob(task, tagId, savepointId, taskServiceImpl);
            case Metis2Hive:
                String sourceType = runtimeConfig.getSourceType();
                if ("metis".equals(sourceType))  return new Metis2HiveJob(task, tagId, savepointId, taskServiceImpl);
                return new Kafka2HiveJob(task, tagId, savepointId, taskServiceImpl);
            case Hive2Sharestore:
                return isSharestore ? new RocksDbETLJob(task, taskServiceImpl) : new TikvJob(task,taskServiceImpl);
            case Mysql2Hive:
                if (syncType == null) {
                    syncType = runtimeConfig.getCatalog().getSync_mode();
                }
                if(isTableList && syncType == 2) return new MysqlCDC2HiveJob(task, tagId, savepointId, taskServiceImpl);
                if(isTableList) return new DBExportJob(task, taskServiceImpl);
            case Hive2Mysql:
            case Hive2Clickhouse:
            case Hive2Doris:
                return new DbETLJob(task, taskServiceImpl);
            case Oracle2Hive:
            case SqlServerHive:
            case Doris2Hive:
                return new DBExportJob(task,taskServiceImpl);
            case DataMigration:
                return isTable ? new MigrationJob(task, taskServiceImpl) : new CopyFileJob(task,taskServiceImpl);
            case Hive2Hive:
                String engine = runtimeConfigObject.getString("engine");
                if(engine!=null && engine.contains("spark")){
                    return new SparkLocalJob(task, taskServiceImpl);
                }else{
                    return new ETLJob(task, taskServiceImpl);
                }
            case SPARKJAR:
                return new SparkLocalJob(task, taskServiceImpl);
            case MergeSmallFiles:
                return new ETLJob(task, taskServiceImpl);
            case PythonShell:
                return new ScriptJob(task,taskServiceImpl);
            case Hive2Redis:
                return new RedisJob(task,taskServiceImpl);
            case Hive2Redshift:
                return new RedShift(task,taskServiceImpl);
            case TfJob:
                return new TfJob(task,taskServiceImpl);
            case Hive2File:
                return new FileJob(task,taskServiceImpl);
            case TrinoJob:
                return new TrinoJob(task,taskServiceImpl);
            case QueryEdit:
                return new QEJob(task,taskServiceImpl);
            case File2Lakehouse:
                return new DBExportJob(task,taskServiceImpl);
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this type:" + task.getTemplateCode());
        }
    }
}
