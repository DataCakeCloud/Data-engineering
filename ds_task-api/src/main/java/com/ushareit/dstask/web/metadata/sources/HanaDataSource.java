package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class HanaDataSource extends AbstractDataSource {

    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {

        Connection conn = null;
        try{
            Class.forName("com.sap.db.jdbc.Driver");
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            String username = connectConfig.getString("username");
            String password = connectConfig.getString("password");
            String jdbcUrlParams = connectConfig.getString("jdbc_url_params");
            conn = DriverManager.getConnection(jdbcUrlParams, username, password);
            log.info("测试Hana链接成功: {}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试MySQL链接失败：",e);
            throw new RuntimeException(e.getMessage());
        }finally {
            close(conn,null,null);
        }
        return true;
    }
}
