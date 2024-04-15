package com.ushareit.dstask.configuration;

import com.ushareit.dstask.common.function.CheckedConsumer;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author fengxiao
 * @date 2023/1/5
 */
@Slf4j
@Data
@Configuration
public class DataCakeSourceConfig {

    @Value("${data-cake.username}")
    private String username;
    @Value("${data-cake.password}")
    private String password;
    @Value("${data-cake.sql-files}")
    private String sqlFiles;
    @Value("${data-cake.admin-sql-files}")
    private String initAdminSqlFiles;
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.type}")
    private String driverClassName;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${data-cake.super-tenant}")
    public String superTenant;

    public void execute(CheckedConsumer<Connection> consumer) {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), e.getMessage());
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            consumer.accept(connection);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "创建租户空间失败");
        }
    }

}
