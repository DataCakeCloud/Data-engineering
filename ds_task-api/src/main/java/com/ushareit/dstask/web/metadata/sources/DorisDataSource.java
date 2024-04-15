package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class DorisDataSource extends AbstractDataSource {

    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {

        Connection conn = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            Integer queryport = connectConfig.getInteger("queryport");
            Integer httpport = connectConfig.getInteger("httpport");
            String password = connectConfig.getString("password");
            String host = connectConfig.getString("host");
            String username = connectConfig.getString("username");
            String database = connectConfig.getString("database");
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s",host,queryport,database), username, password);
            log.info("测试Doris链接成功：{}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试Doris链接失败：",e);
            throw new RuntimeException(e.getMessage());
        }finally {
            close(conn,null,null);
        }
        return true;
    }

}
