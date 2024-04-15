package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.SysDictService;
import io.prestosql.jdbc.PrestoResultSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author: licg
 * @create: 2022-01-20 15:24
 **/
@Slf4j
@Component
public class OlapGateWayUtil {

    private static final String DS_SPARK_CONF_DEMO =
            "--conf bdp-query-engine=spark-submit-sql-3\n" +
                    "--conf bdp-query-user={0}\n" +
                    "--conf bdp-query-region={1}\n" +
                    "--conf spark.hive.exec.dynamic.partition.mode=nonstrict\n" +
                    "--conf spark.hive.exec.dynamic.partition=true";

    private static final String DS_SPARK_CONF_DEMO_Tendency =
            "--conf bdp-query-engine=spark-submit-sql-3\n" +
                    "--conf bdp-query-user={0}\n" +
                    "--conf bdp-query-region={1}\n" +
                    "--conf bdp-query-tenancy={2}\n" +
                    "--conf spark.hive.exec.dynamic.partition.mode=nonstrict\n" +
                    "--conf spark.hive.exec.dynamic.partition=true";

    @Resource
    public SysDictService sysDictService;

    private static final String OLAP_SERVICE_URL = "jdbc:presto://olap-service-direct-prod.ushareit.org:443/hive/default";

    private static final String COMMON_USER = "xxx";

    private static final String COMMON_PASSWORD = "xxx";

    private static Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty("SSL", "true");

        return properties;
    }

    private String execute(String sql) throws SQLException {
        log.info("execute sql:{}", sql);

        Map<String, String> olapGateWayInfo = getOlapGateWayInfo();
        Properties properties = getProperties();
        if (olapGateWayInfo.get("OLAP_SERVICE_URL").contains("80")){
            properties.setProperty("SSL", "false");
            properties.setProperty("user", olapGateWayInfo.get("COMMON_USER"));
        }else{
            properties.setProperty("user", olapGateWayInfo.get("COMMON_USER"));
            properties.setProperty("password", olapGateWayInfo.get("COMMON_PASSWORD"));
            properties.setProperty("SSL", "true");
        }

        Connection connection = null;
        Statement statement = null;
        StringBuffer sb = new StringBuffer();
        try {
            connection = DriverManager.getConnection(olapGateWayInfo.get("OLAP_SERVICE_URL"), properties);
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            String queryId = rs.unwrap(PrestoResultSet.class).getQueryId();
            log.info("queryId:{}", queryId);
            while (rs.next()) {
                sb.append(rs.getString(1)).append("\n");
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public Map<String, String> getOlapGateWayInfo() {
        Map<String, String> olapGatWay = sysDictService.getConfigByParentCode("OLAP_GATE_WAY");
        if (!olapGatWay.containsKey("OLAP_SERVICE_URL") ||
                !olapGatWay.containsKey("COMMON_USER") || !olapGatWay.containsKey("COMMON_PASSWORD")) {
            throw new ServiceException(BaseResponseCodeEnum.LOAP_GATE_WAY_NOT_FOUND);
        }
        return olapGatWay;
    }

    private List execute(String sql, Class outputClass) throws SQLException {
        log.info("execute sql:{}", sql);

        Properties properties = getProperties();
        Map<String, String> olapGateWayInfo = getOlapGateWayInfo();
        if (olapGateWayInfo.get("OLAP_SERVICE_URL").contains("80")){
            properties.setProperty("SSL", "false");
            properties.setProperty("user", olapGateWayInfo.get("COMMON_USER"));
        }else{
            properties.setProperty("user", olapGateWayInfo.get("COMMON_USER"));
            properties.setProperty("password", olapGateWayInfo.get("COMMON_PASSWORD"));
            properties.setProperty("SSL", "true");
        }
        Connection connection = null;
        Statement statement = null;
        ResultSetMapper resultSetMapper = new ResultSetMapper();
        List pojoList;
        try {
            connection = DriverManager.getConnection(olapGateWayInfo.get("OLAP_SERVICE_URL"), properties);
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            String queryId = rs.unwrap(PrestoResultSet.class).getQueryId();
            log.info("queryId:{}", queryId);
            if (rs == null) {
                return null;
            }
            pojoList = resultSetMapper.mapRersultSetToObject(rs, outputClass);
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return pojoList;
    }


    public String executeByPresto(String sql) throws SQLException {
        return execute(sql);
    }

    public List executeByPresto(String sql, Class outputClass) throws SQLException {
        return execute(sql, outputClass);
    }

    public String executeBySpark(String sql, String provider, String region) throws SQLException {
        String conf = MessageFormat.format(DS_SPARK_CONF_DEMO, provider, region);
        sql = conf + "\n" + sql;
        return execute(sql);
    }

    public String executeBySparkByTendency(String sql, String provider, String region, String tendency) throws SQLException {
        String conf = MessageFormat.format(DS_SPARK_CONF_DEMO_Tendency, provider, region, tendency);
        sql = conf + "\n" + sql;
        return execute(sql);
    }


    public List executeBySpark(String sql, String provider, String region, Class outputClass) throws SQLException {
        String conf = MessageFormat.format(DS_SPARK_CONF_DEMO, provider, region);
        sql = conf + "\n" + sql;
        return execute(sql, outputClass);
    }

    public String explainByPresto(String sql) throws SQLException {
        sql = "explain " + sql;
        return executeByPresto(sql);
    }

    public String explainBySpark(String sql, String provider, String region) throws SQLException {
        sql = "explain " + sql;
        return executeBySpark(sql, provider, region);
    }
}
