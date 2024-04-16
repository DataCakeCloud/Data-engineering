package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.ddl.metadata.Table;

import java.util.List;
import java.util.Map;


public interface MetaDataService extends BaseService<Table> {
    List<Table> search(MetaDataParam metaDataParam);

    List<Table> search(MetaDataParam metaDataParam, String type, String actorId, String db,  String table, String sourceParam);

    Boolean clearCache(String type, String actorId, String db,  String table, String sourceParam);

    List<Map<String,String>> getStorageSchemaDetail(Actor actor,String type,String path,String fileType,String fieldDelimiter);

    String getDdl(MetaDataParam metaDataParam);

    Map<String, Object> getDisplayDdl(MetaDataParam metaDataParam);

    List<Map<String, String>> getTableSample(MetaDataParam metaDataParam);

    Boolean checkConnection(MetaDataParam metaDataParam);

    Boolean createTable(MetaDataParam metaDataParam);
}