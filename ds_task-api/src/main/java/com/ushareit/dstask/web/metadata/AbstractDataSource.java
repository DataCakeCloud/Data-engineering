package com.ushareit.dstask.web.metadata;

import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.sources.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractDataSource implements DataSource {
    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        return null;
    }

    @Override
    public void close(Connection conn, Statement st, ResultSet rs) {

        try {
            if(conn != null) conn.close();
            if(st != null) st.close();
            if(rs != null) rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
