package com.ushareit.dstask.web.ddl;


import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.ddl.model.*;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.metadata.MetaDataManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ushareit.dstask.web.metadata.MetaDataManager.*;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
public class DdlFactory {

    public static SqlDdl getDdl(MetaDataParam metaDataParam) {
        if (metaDataParam != null && StringUtils.isNotEmpty(metaDataParam.getGuid())) {
            metaDataParam.setMetaFlag(ATLAS);
        }
        return getDdlByTable(MetaDataManager.getMetaDataSchema(metaDataParam));
    }

    public static SqlDdl getDdlByTable(Table table) {
        switch (SourceTypeEnum.valueOf(table.getTypeName())) {
            case kafka_topic:
                return new KafkaDdl(table, null);
            case metis:
            case metis_dev:
            case metis_test:
            case metis_pro:
                return new MetisDdl(table, null);
            case rdbms_table:
            case clickhouse:
                return new MysqlDdl(table);
            case file:
            case es:
            case sharestore:
            case hive_table:
            case iceberg:
                return new SqlDdl(table);
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this type:" + table.getSourceType());
        }
    }

    public static SqlDdl getDdl(String guId) {
        Table table = getMetadataTableByGuid(guId);
        return getDdlByTable(table);
    }

    public static Table getMetadataTableByGuid(String guId) {
        MetaDataParam metaDataParam = new MetaDataParam();
        metaDataParam.setGuid(guId);
        metaDataParam.setMetaFlag(ATLAS);
        return MetaDataManager.getMetaDataSchema(metaDataParam);
    }

    public static Table getMetadataTableByQualifiedName(Dataset dataset) {
        return MetaDataManager.getMetaDataSchema(dataset);
    }
}
