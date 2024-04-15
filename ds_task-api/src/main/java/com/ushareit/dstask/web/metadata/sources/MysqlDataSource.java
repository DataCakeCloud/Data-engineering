package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

@Slf4j
public class MysqlDataSource extends AbstractDataSource {

    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {

        Connection conn = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            Integer port = connectConfig.getInteger("port");
            String password = connectConfig.getString("password");
            String host = connectConfig.getString("host");
            String username = connectConfig.getString("username");
            String database = connectConfig.getString("database");
            String jdbcUrlParams = connectConfig.getString("jdbc_url_params");
            if(StringUtils.isNotEmpty(jdbcUrlParams)) {
                jdbcUrlParams="?"+jdbcUrlParams;
            }else {
                jdbcUrlParams = "";
            }
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s%s",host,port,database,jdbcUrlParams), username, password);
            log.info("测试MySQL链接成功: {}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试MySQL链接失败: {}",e);
            throw new RuntimeException(e.getMessage());
        }finally {
            close(conn,null,null);
        }
        return true;
    }

}