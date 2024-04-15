package com.ushareit.dstask.third.airbyte.connector.vo;

import lombok.Data;

import java.util.Properties;

/**
 * @author fengxiao
 * @date 2022/8/18
 */
@Data
public class JdbcInfo {

    private String jdbcUrl;
    private Properties properties;

    public JdbcInfo(String jdbcUrl, Properties properties) {
        this.jdbcUrl = jdbcUrl;
        this.properties = properties;
    }
}
