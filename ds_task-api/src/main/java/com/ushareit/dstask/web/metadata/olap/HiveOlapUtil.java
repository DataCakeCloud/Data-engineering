package com.ushareit.dstask.web.metadata.olap;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Account;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HiveOlapUtil {

    /**
     * 库表发现：使用引擎presto ,ue1 sg2 查2次取并集  sg1查一次
     * scheam获取: 使用spark
     */
    public static String PRESTO_AWS = "presto_aws";
    public static String PRESTO_AWS_SG = "presto_aws_sg";
    public static String PRESTO_HUAWEI = "presto_huawei";
    public static String BDP_GROUP = "BDP";

    public static String HIVE = "hive";
    public static String ICEBERG = "iceberg";
    public static String USER_NAME = "username";
    public static String PASSWORD = "password";
    public static String URL = "url";
    public static String PROVIDER = "provider";
    public static String PROVIDER_AWS_SG = "aws_sg";
    public static String HIVE_DB = "hive_db";

    public static String HUAIWEI = "huawei";
    public static String AWS = "aws";

    public static String GET_DATABASES = "GET_DATABASES";
    public static String GET_TABLES = "GET_TABLES";
    public static String GET_COLUMN = "GET_COLUMN";
    public static String GET_CATLOG = "GET_CATLOG";

    public static String HIVE_SHOW_DATABASES_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\nshow schemas from %s";
    public static String HIVE_SHOW_DATABASES_AWS_SG_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-engine=ares_ap1\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\n show schemas";

    public static String HIVE_SHOW_TABLES_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\nshow tables from %s.%s";
    public static String HIVE_SHOW_TABLES_AWS_SG_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-engine=ares_ap1\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\n show tables from %s";

    public static String HIVE_SHOW_COLUMNS_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\n describe %s.%s";
    public static String HIVE_SHOW_COLUMNS_AWS_SG_SQL = "--conf bdp-query-user=%s\n--conf bdp-query-engine=ares_ap1\n--conf bdp-query-tenancy=%s\n--conf bdp-query-tenantid=%s\n describe %s.%s";

    private HiveMetaData hiveMetaData;

    private String sql = "";

//    @Cacheable(cacheNames = {"metadata"}, key = "#engine+'-'+#name")
    @Cacheable(cacheNames = {"metadata"}, key = "#engine+'-'+#tenantName")
    public List<Table> getMetaDatabase(HiveMetaData hiveMetaData, String engine, String name, String tenantName) {

        Integer tenantId = InfTraceContextHolder.get().getTenantId();

        log.info("getMetaDatabase metaDataParam is :" + hiveMetaData.toString());
        log.info("getMetaDatabase engine is :" + engine);
        log.info("getMetaDatabase name is :" + name);
        this.hiveMetaData = hiveMetaData;
        //, String catalog
        String hiveDataBaseSql = "";
        List<Table> hiveDatabasesList;
        List<Table> icebergDatabaseList = new ArrayList<>();
        //美东和华为 调2次  hive 和iceberge不同
        if (engine.equals(PRESTO_AWS) || engine.equals(PRESTO_HUAWEI)) {
            hiveDataBaseSql = String.format(HIVE_SHOW_DATABASES_SQL, name, tenantName, tenantId, HIVE);
            String icebergDataBaseSql = String.format(HIVE_SHOW_DATABASES_SQL, name, tenantName, tenantId, ICEBERG);
            icebergDatabaseList = getResults(engine, icebergDataBaseSql, GET_DATABASES);
        }
        if (engine.equals(PRESTO_AWS_SG)) {
            hiveDataBaseSql = String.format(HIVE_SHOW_DATABASES_AWS_SG_SQL, name, tenantName, tenantId);
        }
        hiveDatabasesList = getResults(engine, hiveDataBaseSql, GET_DATABASES);
        hiveDatabasesList.addAll(icebergDatabaseList);
        Set<Table> personSet = new TreeSet<>(Comparator.comparing(Table::getName));

        if (!hiveDatabasesList.isEmpty()) {
            personSet.addAll(hiveDatabasesList);
        }
        return new ArrayList<>(personSet);
    }


//    @Cacheable(cacheNames = {"metadata"}, key = "#engine +'-'+#database+'-'+#name")
//    @Cacheable(cacheNames = {"metadata"}, key = "#engine +'-'+#database + '-' +#tenantName")
    public List<Table> getMetaTable(HiveMetaData hiveMetaData, String engine, String name, String database, String tenantName) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();

        this.hiveMetaData = hiveMetaData;
        List<Table> icebergDatabaseList = new ArrayList<>();
        if (engine.equals(PRESTO_AWS) || engine.equals(PRESTO_HUAWEI)) {
            sql = String.format(HIVE_SHOW_TABLES_SQL, name, tenantName, tenantId, HIVE, database);
            String icebergTableSql = String.format(HIVE_SHOW_TABLES_SQL, name, tenantName, tenantId, ICEBERG, database);
            icebergDatabaseList = getResults(engine, icebergTableSql, GET_TABLES);
        } else if (engine.equals(PRESTO_AWS_SG)) {
            sql = String.format(HIVE_SHOW_TABLES_AWS_SG_SQL, name, tenantName, tenantId, database);
        }
        List<Table> results = getResults(engine, sql, GET_TABLES);
        results.addAll(icebergDatabaseList);

        Set<Table> personSet = new TreeSet<>(Comparator.comparing(Table::getName));

        if (!results.isEmpty()) {
            personSet.addAll(results);
        }
        return new ArrayList<>(personSet);
    }


//    @Cacheable(cacheNames = {"metadata"}, key = "#engine +'-'+#database+'-'+#table+'-'+#name")
    @Cacheable(cacheNames = {"metadata"}, key = "#engine +'-'+#database+'-'+#tenantName+'-'+#table")
    public List<Table> getMetaColumn(HiveMetaData hiveMetaData, String engine, String name, String database, String table, String tenantName) {

        Integer tenantId = InfTraceContextHolder.get().getTenantId();

        this.hiveMetaData = hiveMetaData;
        if (engine.equals(PRESTO_AWS_SG)) {
            sql = String.format(HIVE_SHOW_COLUMNS_AWS_SG_SQL, name, tenantName, tenantId, database, table);
        } else {
            sql = String.format(HIVE_SHOW_COLUMNS_SQL, name, tenantName, tenantId, database, table);
        }
        return getResults(engine, sql, GET_COLUMN);
    }


    public List<Table> getResults(String engine, String sql, String type) {
        log.info("getResults sql is :" + sql);
        JSONObject connectInfo = getUsernameAndPassword(hiveMetaData.account, BDP_GROUP, engine, hiveMetaData.awsSGUrl, hiveMetaData.awsUrl, hiveMetaData.huaweiUrl);
        String username = connectInfo.getString(USER_NAME);
        String password = connectInfo.getString(PASSWORD);
        String url = connectInfo.getString(URL);
        String provider = connectInfo.getString(PROVIDER);
        if (StringUtils.isNotEmpty(type) && type.equals(GET_COLUMN)) {
            if (engine.equals(PRESTO_AWS_SG)) {
                provider = PROVIDER_AWS_SG;
            }
        }
        return getQueryResults(engine, username, password, url, sql, provider, type);
    }


    public static JSONObject getUsernameAndPassword(List<Account> account,
                                                    String group, String engine,
                                                    String awsSGUrl, String awsUrl, String huaweiUrl) {
        JSONObject connectInfo = new JSONObject();
        String username = "";
        String password = "";
        String url = "";
        String provider = AWS;

        for (Account value : account) {
            if (value.getUserGroup().equals(group)) {
                username = value.getUsername();
                password = value.getPassword();
                break;
            }
        }
        if (engine.equals(PRESTO_AWS_SG)) {
            url = awsSGUrl;
        } else if (engine.equals(PRESTO_HUAWEI)) {
            url = huaweiUrl;
            provider = HUAIWEI;
        } else {
            url = awsUrl;
        }

        connectInfo.put(USER_NAME, username);
        connectInfo.put(PASSWORD, password);
        connectInfo.put(URL, url);
        connectInfo.put(PROVIDER, provider);

        return connectInfo;
    }


    public List<Table> getQueryResults(String engine, String username, String password,
                                       String url, String sql, String provider, String type) {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        log.info("getResults sql is :" + sql);
        log.info("getResults username is :" + username);
        log.info("getResults password is :" + password);
        log.info("getResults url is :" + url);
        log.info("getResults provider is :" + provider);
        log.info("getResults type is :" + type);
        if (!provider.equals(HUAIWEI)) {
            properties.setProperty(PASSWORD, password);
            properties.setProperty("SSL", "true");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        ArrayList<Table> tables = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url, properties);
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            log.info("getResults rs is :" + rs);
            int colNum = rs.getMetaData().getColumnCount();
            Table tableColum = new Table();
            tableColum.setColumns(new ArrayList<>());
            Set<String> partitionKey = new HashSet<>();
            Boolean partitionKeyFlag = false;
            while (rs.next()) {
                if (colNum == 1) {
                    Table table = new Table();
                    if (!rs.getString(1).equals("system") && !rs.getString(1).equals("tpcds")) {
                        table.setName(rs.getString(1));
                        table.setSourceType(HIVE_DB);
                        table.setTypeName(HIVE_DB);
                        tables.add(table);
                    }
                } else if (colNum > 1) {
                    String columnName = rs.getString(1);
                    String columnType = rs.getString(2);
                    String comment = rs.getString(3);
                    Column column = new Column();

                    if (StringUtils.isEmpty(columnName) || columnName.startsWith("#")) {
                        partitionKeyFlag = true;
                        continue;
                    }
                    if (partitionKeyFlag) {
                        if (StringUtils.isNotEmpty(rs.getString(1))
                                && rs.getString(1).trim().startsWith("Part")) {
                            partitionKey.add(rs.getString(2).trim());
                        } else {
                            partitionKey.add(rs.getString(1).trim());
                        }
                        continue;
                    }
                    column.setName(columnName.trim());
                    column.setType(columnType.trim());
                    if (StringUtils.isNotEmpty(comment)) {
                        comment = comment.trim();
                    }
                    column.setComment(comment);

                    List<Column> sourceColumn = tableColum.getColumns();
                    sourceColumn.add(column);
                    tableColum.setColumns(sourceColumn);

                }

            }
            if (StringUtils.isNotEmpty(type) && type.equals(GET_COLUMN)) {
                if (!partitionKey.isEmpty()) {
                    List<Column> columns = tableColum.getColumns();
                    tableColum.setColumns(columns.stream().
                            filter(data -> !partitionKey.contains(data.getName())).
                            collect(Collectors.toList()));
                }
                tables.add(tableColum);
            }
        } catch (Exception e) {
//            throw new RuntimeException(CommonUtil.printStackTraceToString(e));
            try {
                throw e;
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                    connection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return tables;
    }


}
