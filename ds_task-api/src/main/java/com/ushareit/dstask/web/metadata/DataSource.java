package com.ushareit.dstask.web.metadata;

import com.ushareit.dstask.common.param.MetaDataParam;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public interface DataSource {

    Boolean checkConnection(MetaDataParam metaDataParam) ;

    void close(Connection conn, Statement st, ResultSet rs);
}
