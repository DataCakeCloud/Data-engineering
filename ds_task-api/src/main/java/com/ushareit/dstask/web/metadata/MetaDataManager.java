package com.ushareit.dstask.web.metadata;

import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.metadata.airbyte.AirByteMetaData;
import com.ushareit.dstask.web.metadata.atlas.AtlasMetaData;

import java.util.List;
import java.util.Map;

public class MetaDataManager {

    public static final String ATLAS = "ATLAS";

    public static final String AIRBYTE = "AIRBYTE";


    public static List<Table> getMetaDataDiscover(MetaDataParam metaDataParam) {
        AbstractMetaData abstractMetaData = getAbstractMetaData(metaDataParam);
        return abstractMetaData.search(metaDataParam);
    }


    public static List<Map<String, String>> getTableSample(MetaDataParam metaDataParam) {
        AbstractMetaData abstractMetaData = getAbstractMetaData(metaDataParam);
        return abstractMetaData.getTableSample(metaDataParam);
    }

    public static Boolean checkConnection(MetaDataParam metaDataParam) {
        AbstractMetaData abstractMetaData = getAbstractMetaData(metaDataParam);
        return abstractMetaData.checkConnection(metaDataParam);
    }

    public static Boolean createTable(MetaDataParam metaDataParam) {
        AbstractMetaData abstractMetaData = getAbstractMetaData(metaDataParam);
        return abstractMetaData.createTable(metaDataParam);
    }

    public static Table getMetaDataSchema(MetaDataParam metaDataParam) {
        AbstractMetaData abstractMetaData = getAbstractMetaData(metaDataParam);
        return abstractMetaData.getDdl(metaDataParam);
    }

    /**
     * 兼容之前库中数据获取表结构
     *
     * @param dataset
     * @return
     */
    public static Table getMetaDataSchema(Dataset dataset) {
        MetaDataParam metaDataParam = new MetaDataParam(dataset);
        return getMetaDataSchema(metaDataParam);
    }

    public static AbstractMetaData getAbstractMetaData(MetaDataParam metaDataParam) {
        switch (metaDataParam.getMetaFlag()) {
            case ATLAS:
                return new AtlasMetaData(metaDataParam);
            case AIRBYTE:
                return new AirByteMetaData(metaDataParam);
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this source" + metaDataParam.getMetaFlag());
        }
    }


}
