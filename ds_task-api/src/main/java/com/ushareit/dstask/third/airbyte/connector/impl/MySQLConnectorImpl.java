package com.ushareit.dstask.third.airbyte.connector.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.api.DataSourceQueryApi;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.connector.MySQLConnector;
import com.ushareit.dstask.third.airbyte.connector.vo.ColumnInfo;
import com.ushareit.dstask.third.airbyte.connector.vo.JdbcInfo;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.web.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;

/**
 * @author fengxiao
 * @date 2022/8/17
 */
@Slf4j
@Service
public class MySQLConnectorImpl implements MySQLConnector {

    @Resource
    public ActorService actorService;

    public String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public String CLICKHOUSE_DRIVER = "ru.yandex.clickhouse.ClickHouseDriver";

    public String MYSQL_URL = "jdbc:mysql://%s:%s/%s";

    public String CLICKHOUSE_URL = "jdbc:clickhouse://%s:%s/%s";

    @Override
    public List<String> getTableList(JsonNode config, String type) throws Exception {
        String type1 = SourceTypeEnum.clickhouse.getType();
        if (type.equals(SourceTypeEnum.clickhouse.getType())) {
            return getClickouseTableList(config, type);
        } else if (type.equals(SourceTypeEnum.sql_server.getType())) {
            return getSqlServerTableList(config);
        } else if (type.equals(SourceTypeEnum.oracle.getType())) {
            return getOracleTableList(config);
        }
        return getMysqlTableList(config, type);
    }


    public List<String> getMysqlTableList(JsonNode config, String type) throws Exception {
        JdbcInfo jdbcInfo = toDatabaseConfig(config, type);
        Class.forName(MYSQL_DRIVER);
        try (Connection connection = DriverManager.getConnection(jdbcInfo.getJdbcUrl(), jdbcInfo.getProperties())) {
            ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(3));
            }
            return tableNames;
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    public List<String> getClickouseTableList(JsonNode config, String type) throws Exception {
        JdbcInfo jdbcInfo = toDatabaseConfig(config, type);
        Class.forName(CLICKHOUSE_DRIVER);
        try (Connection connection = DriverManager.getConnection(jdbcInfo.getJdbcUrl(), jdbcInfo.getProperties())) {
            Statement statement = connection.createStatement();
            String sql = "show tables;";
            ResultSet rs = statement.executeQuery(sql);
            List<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                tableNames.add(rs.getString(1));
            }
            return tableNames;
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    private List<String> getSqlServerTableList(JsonNode config) throws ClassNotFoundException, SQLException {
        String jdbcUrl = String.format(DsTaskConstant.SQLSERVER_URL,
                config.get("host").asText(),
                config.get("port").asText(),
                config.get("database").asText());
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            Class.forName(DsTaskConstant.SQLSERVER_DRIVER);
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            statement = connection.createStatement();
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'";
            resultSet = statement.executeQuery(query);
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("TABLE_NAME"));
            }

            return tableNames;
        } catch (SQLException | ClassNotFoundException e) {
            log.error(String.format("failed to getSqlServerTableList: %s", CommonUtil.printStackTraceToString(e)));
            throw new RuntimeException("failed to get sqlserver tableList", e);
        } finally {
            assert resultSet != null;
            resultSet.close();
            statement.close();
            connection.close();
        }
    }

    private List<String> getOracleTableList(JsonNode config) {
        String jdbcUrl = String.format(DsTaskConstant.ORACLE_URL,
                config.get("host").asText(),
                config.get("port").asText(),
                config.get("sid").asText());
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        Connection connection = null;
        try {
            Class.forName(DsTaskConstant.ORACLE_DRIVER);
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            String sql = String.format("SELECT table_name FROM all_tables WHERE owner = '%s' ", config.get("schemas").get(0).asText());
            ResultSet resultSet = statement.executeQuery(sql);

            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }

            resultSet.close();
            statement.close();
            connection.close();
            return tableNames;
        } catch (ClassNotFoundException | SQLException e) {
            log.error(String.format("failed to getOracleTableList: %s", CommonUtil.printStackTraceToString(e)));
            throw new RuntimeException("failed to get oracle tableList", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //    @Cacheable(cacheNames = {"metadata"}, key = "'actor_id-'+#actorId+'-'+#table")
    public Map<String, ColumnInfo> getColumnMap(Integer actorId, String table, String type) throws Exception {
        Actor byId = actorService.getById(actorId);
        if (byId == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        JsonNode jsonNode = Jsons.deserialize(byId.getConfiguration());
        return getColumnMap(jsonNode, table, type);
    }

    @Override
    public Map<String, ColumnInfo> getColumnMap(JsonNode config, String table, String metaType) throws Exception {
        if (metaType.equals(SourceTypeEnum.sql_server.getType()) || metaType.equals(SourceTypeEnum.oracle.getType())) {
            return getSqlServerOracleColumnMap(config, table, metaType);
        } else {
            return getMySqlColumnMap(config, table, metaType);
        }
    }

    public Map<String, ColumnInfo> getSqlServerOracleColumnMap(JsonNode config, String table, String metaType) throws SQLException {
        Map<String, ColumnInfo> columnMap = new HashMap<>();

        String username = config.get("username").asText();
        String password = config.get("password").asText();

        String url;
        Connection connection = null;
        ResultSet resultSet = null;
        ResultSet primaryKeyResultSet = null;
        try {
            if (metaType.equals(SourceTypeEnum.sql_server.getType())) {
                url = String.format(DsTaskConstant.SQLSERVER_URL,
                        config.get("host").asText(),
                        config.get("port").asText(),
                        config.get("database").asText());
                Class.forName(DsTaskConstant.SQLSERVER_DRIVER);
            } else {
                url = String.format(DsTaskConstant.ORACLE_URL,
                        config.get("host").asText(),
                        config.get("port").asText(),
                        config.get("sid").asText());
                Class.forName(DsTaskConstant.ORACLE_DRIVER);
            }

            connection = DriverManager.getConnection(url, username, password);
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getColumns(null, null, table, null);
            primaryKeyResultSet = metaData.getPrimaryKeys(null, null, table);

            String primaryKey = null;
            while (primaryKeyResultSet.next()) {
                primaryKey = primaryKeyResultSet.getString("COLUMN_NAME");
            }

            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String dataType = resultSet.getString("TYPE_NAME");
                int columnSize = resultSet.getInt("COLUMN_SIZE");
                String comment = resultSet.getString("REMARKS");

                Boolean isPk = null;
                if (columnName.equalsIgnoreCase(primaryKey)) {
                    isPk = true;
                }
                columnMap.put(columnName, new ColumnInfo(columnName, dataType, columnSize, comment, isPk));
            }
            return columnMap;
        } catch (SQLException | ClassNotFoundException e) {
            log.error(String.format("failed to getSqlServerOracleColumnMap: %s", CommonUtil.printStackTraceToString(e)));
            throw new RuntimeException("failed to get sqlserver or oracle tableList", e);
        } finally {
            assert resultSet != null;
            resultSet.close();

            assert primaryKeyResultSet != null;
            primaryKeyResultSet.close();

            connection.close();
        }
    }

    public Map<String, ColumnInfo> getMySqlColumnMap(JsonNode config, String table, String metaType) throws Exception {
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        JdbcInfo jdbcInfo = toDatabaseConfig(config, metaType);
        Class.forName(MYSQL_DRIVER);
        try (Connection connection = DriverManager.getConnection(jdbcInfo.getJdbcUrl(), jdbcInfo.getProperties())) {
            String db = config.get("database").asText();
            ResultSet resultSet = connection.getMetaData().getColumns(db, null, table, null);
            ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(db, null, table);
            String primaryKey = null;
            while (primaryKeys.next()) {
                primaryKey = primaryKeys.getString("COLUMN_NAME");
            }
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                String type = resultSet.getString("TYPE_NAME");
                int size = resultSet.getInt("COLUMN_SIZE");
                String comment = resultSet.getString("REMARKS");
                Boolean isPk = null;
                if (name.equalsIgnoreCase(primaryKey)){
                    isPk = true;
                }
                columnMap.put(name, new ColumnInfo(name, type, size, comment, isPk));
            }

            return columnMap;
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    @Override
    public List<Map<String, String>> getTableSample(JsonNode config, String tableName, String metaType) throws Exception {
        if (metaType.equals(SourceTypeEnum.sql_server.getType())) {
            return getSqlServerTableSample(config, tableName);
        } else if (metaType.equals(SourceTypeEnum.oracle.getType())) {
            return getOracleTableSample(config, tableName);
        } else  {
            return getMySqlTableSample(config, tableName, metaType);
        }
    }

    private List<Map<String, String>> getOracleTableSample(JsonNode config, String tableName) throws SQLException {
        List<Map<String, String>> result = new ArrayList<>();
        String url = String.format(DsTaskConstant.ORACLE_URL,
                config.get("host").asText(),
                config.get("port").asText(),
                config.get("sid").asText());
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            Class.forName(DsTaskConstant.ORACLE_DRIVER);
            connection = DriverManager.getConnection(url, username, password);
            String sql = String.format("SELECT * FROM %s.%s WHERE ROWNUM <= %d", config.get("schemas").get(0).asText(), tableName, DsTaskConstant.SAMPLE_SIZE);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, String> columnMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getObject(i) != null ? resultSet.getObject(i).toString(): null;
                    columnMap.put(columnName, columnValue);
                }
                result.add(columnMap);
            }

            return result;
        } catch (ClassNotFoundException | SQLException e) {
            log.error(String.format("failed to getOracleTableSample: %s", CommonUtil.printStackTraceToString(e)));
            throw new RuntimeException("failed to get oracle tableSample", e);
        } finally {
            assert resultSet != null;
            resultSet.close();
            statement.close();
            connection.close();
        }
    }

    private List<Map<String, String>> getSqlServerTableSample(JsonNode config, String tableName) throws SQLException {
        List<Map<String, String>> result = new ArrayList<>();

        String jdbcUrl = String.format(DsTaskConstant.SQLSERVER_URL,
                config.get("host").asText(),
                config.get("port").asText(),
                config.get("database").asText());
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        Statement statement = null;
        Connection connection = null;
        ResultSet sampleResultSet = null;
        try {
            Class.forName(DsTaskConstant.SQLSERVER_DRIVER);
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            // 获取表的元数据
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, null);

            // 构造查询字段列表
            StringBuilder columnNames = new StringBuilder();
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                columnNames.append(columnName).append(", ");
            }
            columnNames.delete(columnNames.length() - 2, columnNames.length()); // 移除最后的逗号和空格

            String query = String.format("SELECT TOP %d %s FROM %s", DsTaskConstant.SAMPLE_SIZE, columnNames.toString(), tableName);
            statement = connection.createStatement();
            sampleResultSet = statement.executeQuery(query);

            ResultSetMetaData sampleMetaData = sampleResultSet.getMetaData();
            int columnCount = sampleMetaData.getColumnCount();

            while (sampleResultSet.next()) {
                Map<String, String> columnMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = sampleMetaData.getColumnName(i);
                    String columnValue = sampleResultSet.getString(i);
                    columnMap.put(columnName, columnValue);
                }
                result.add(columnMap);
            }

            return result;
        } catch (ClassNotFoundException | SQLException e) {
            log.error(String.format("failed to getSqlServerTableSample: %s", CommonUtil.printStackTraceToString(e)));
            throw new RuntimeException("failed to get sqlserver tableSample", e);
        } finally {
            assert sampleResultSet != null;
            sampleResultSet.close();

            statement.close();
            connection.close();
        }
    }

    public List<Map<String, String>> getMySqlTableSample(JsonNode config, String tableName, String metaType) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        JdbcInfo jdbcInfo = toDatabaseConfig(config, metaType);
        Class.forName(MYSQL_DRIVER);
        try (Connection connection = DriverManager.getConnection(jdbcInfo.getJdbcUrl(), jdbcInfo.getProperties())) {
            String sql = "SELECT * FROM " + tableName + " limit ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, 5);
            ResultSet resultSet = stmt.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, String> columnMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    String value = resultSet.getString(i);
                    columnMap.put(columnName, value);
                }

                result.add(columnMap);
            }
            resultSet.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    private JdbcInfo toDatabaseConfig(final JsonNode config, String type) {
        String JDBC_URL = MYSQL_URL;
        if (type.equals(SourceTypeEnum.clickhouse.getType())) {
            JDBC_URL = CLICKHOUSE_URL;
        }

        String port;
        if (type.equals(SourceTypeEnum.doris.getType())) {
            port = config.get("queryport").asText();
        } else {
            port = config.get("port").asText();
        }

        String jdbcUrl = String.format(JDBC_URL, config.get("host").asText(), port, config.get("database").asText());

        Properties properties = new Properties();
        properties.setProperty("user", config.get("username").asText());
        properties.setProperty("password", config.has("password") ? config.get("password").asText() : null);

        properties.setProperty("useCursorFetch", "true");
        properties.setProperty("zeroDateTimeBehavior", "convertToNull");
        properties.setProperty("tinyInt1isBit", "true");
        properties.setProperty("yearIsDateType", "true");

        if (config.get("jdbc_url_params") != null && !config.get("jdbc_url_params").asText().isEmpty()) {
            Arrays.stream(config.get("jdbc_url_params").asText().split("&"))
                    .filter(item -> item.contains("="))
                    .forEach(item -> {
                        String[] params = item.split("=");
                        properties.setProperty(params[0], params[1]);
                    });
        }
        return new JdbcInfo(jdbcUrl, properties);
    }

    @Override
    public List<DataSourceQueryApi.Item> query(JsonNode config, String type, String sql, DataSourceQueryApi.QueryContext context) throws Exception {
        JdbcInfo jdbcInfo = toDatabaseConfig(config, type);
        if (type.equals(SourceTypeEnum.clickhouse.getType())) {
            Class.forName(CLICKHOUSE_DRIVER);
        } else {
            Class.forName(MYSQL_DRIVER);
        }

        try (Connection connection = DriverManager.getConnection(jdbcInfo.getJdbcUrl(), jdbcInfo.getProperties());
             Statement statement = connection.createStatement()) {
            
            return parseResultSet(statement.executeQuery(sql), context);
        } catch (SQLException e) {
            log.error("exec metrics query error, sql:" + sql + " msg:" + e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<DataSourceQueryApi.Item> parseResultSet(ResultSet resultSet, DataSourceQueryApi.QueryContext context) throws SQLException {
        List<DataSourceQueryApi.Item> itemList = new ArrayList<>();
        while (resultSet.next()) {
            DataSourceQueryApi.Item.Builder itemBuilder = DataSourceQueryApi.Item.newBuilder();
            if (CollectionUtils.isEmpty(context.getRuleKeyListList())) {
                itemBuilder.putColumnMap("defaultValue", resultSet.getString(NumberUtils.INTEGER_ONE));
            } else {
                for (String columnName : context.getRuleKeyListList()) {
                    itemBuilder.putColumnMap(columnName, resultSet.getString(columnName));
                }
            }
            itemList.add(itemBuilder.build());
        }
        return itemList;
    }
}
