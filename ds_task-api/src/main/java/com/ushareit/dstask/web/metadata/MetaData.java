package com.ushareit.dstask.web.metadata;


import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.ddl.metadata.Table;

import java.util.List;
import java.util.Map;


public interface MetaData {

    List<Table> search(MetaDataParam metaDataParam);

    Table getDdl(MetaDataParam metaDataParam);

    List<Map<String, String>> getTableSample(MetaDataParam metaDataParam);

    Boolean checkConnection(MetaDataParam metadataParam);

    Boolean createTable(MetaDataParam metaDataParam);
}