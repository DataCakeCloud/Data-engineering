package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class SqlServerDataSource extends AbstractDataSource {
    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        Connection conn = null;
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            Integer port = connectConfig.getInteger("port");
            String password = connectConfig.getString("password");
            String host = connectConfig.getString("host");
            String username = connectConfig.getString("username");
            String database = connectConfig.getString("database");
            String jdbcUrlParams = connectConfig.getString("jdbc_url_params");
            if(StringUtils.isNotEmpty(jdbcUrlParams)) {
                jdbcUrlParams=";"+jdbcUrlParams+";trustServerCertificate=true;Encrypt=false";
            }else {
                jdbcUrlParams = ";trustServerCertificate=true;Encrypt=false";
            }

            System.out.println(String.format("jdbc:sqlserver://%s:%s;databasename=%s%s",host,port,database,jdbcUrlParams));
            conn = DriverManager.getConnection(String.format("jdbc:sqlserver://%s:%s;databasename=%s%s",host,port,database,jdbcUrlParams), username, password);
            log.info("测试SQLServer链接成功: {}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试SQLServer链接失败：",e);
            throw new RuntimeException(e.getMessage());
        }finally {
            close(conn,null,null);
        }
        return true;
    }

}