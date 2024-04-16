package com.ushareit.dstask.web.metadata;

import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.ddl.metadata.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AbstractMetaData implements MetaData {

    public MetaDataParam metaDataParam;

    public AbstractMetaData(MetaDataParam metaDataParam) {
        this.metaDataParam = metaDataParam;
    }


    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        return new ArrayList<>();
    }

    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        return new Table();
    }

    @Override
    public List<Map<String, String>> getTableSample(MetaDataParam metaDataParam) {
        return new ArrayList<>();
    }

    @Override
    public Boolean checkConnection(MetaDataParam metadataParam) {
        return null;
    }

    @Override
    public Boolean createTable(MetaDataParam metaDataParam) {
        return null;
    }
}
