package com.ushareit.dstask.web.metadata.airbyte;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorCatalogService;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.connector.MySQLConnector;
import com.ushareit.dstask.third.airbyte.connector.vo.ColumnInfo;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.EncryptUtil;
import org.apache.catalina.Host;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JdbcMetaData extends AirByteMetaData {

    private static JdbcMetaData jdbcMetaData;

    @Resource
    public ActorService actorService;

    @Resource
    public ActorCatalogService actorCatalogService;

    @Resource
    public ActorDefinitionService actorDefinitionService;

    @Resource
    public MySQLConnector mySQLConnector;

    public String MYSQL_URL = "jdbc:mysql://%s:%s/%s";

    public String CLICKHOUSE_URL = "jdbc:clickhouse://%s:%s/%s";

    @PostConstruct
    public void init() {
        jdbcMetaData = this;
        jdbcMetaData.actorDefinitionService = this.actorDefinitionService;
        jdbcMetaData.actorService = this.actorService;
        jdbcMetaData.actorCatalogService = this.actorCatalogService;
        jdbcMetaData.mySQLConnector = this.mySQLConnector;
    }

    public JdbcMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    public String URL = "url";
    public String USER_NAME = "username";
    public String PASSWORD = "password";
    public String RDBM_TYPE = "rdbms_type";
    public String DB_NAME = "db_name";

    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        List<Actor> actorByType;
        if (StringUtils.isEmpty(metaDataParam.getActorId())) {
            actorByType = jdbcMetaData.actorDefinitionService.getActorByTypeAndRegion(metaDataParam.getType(), metaDataParam.getRegion());
            if (actorByType.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            //拿这actor 和 table 查表
            Actor byId = jdbcMetaData.actorService.getById(metaDataParam.getActorId());
//            List<ActorCatalog> actorCatalogs = mysqlMetaData.actorCatalogService.selectByActorId(Integer.valueOf(metaDataParam.getActorId()));
//            if (actorCatalogs.isEmpty()) {
//                return new ArrayList<>();
//            }
//            ActorCatalog actorCatalog = actorCatalogs.stream().findFirst().get();
//            MysqlStreams mysqlStreams = JSON.parseObject(actorCatalog.getCatalog(), MysqlStreams.class);
//            Map<String, List<MysqlStreams.Table>> tableMaps = mysqlStreams.getStreams().stream().collect(Collectors.
//                    groupingBy(MysqlStreams.Table::getName));
            if (StringUtils.isEmpty(metaDataParam.getDb()) && StringUtils.isEmpty(metaDataParam.getTable())) {
                return getDataBase(byId);
            }
            if (StringUtils.isNotEmpty(metaDataParam.getTable())) {
                return getTableSchema(metaDataParam, byId);
            }
            return getTableList(metaDataParam, byId);
        }
        return getSourceList(metaDataParam, actorByType);
    }

    @Override
    public List<Map<String, String>> getTableSample(MetaDataParam metaDataParam) {
        if (StringUtils.isEmpty(metaDataParam.getDb()) || StringUtils.isEmpty(metaDataParam.getTable())) {
            return new ArrayList<>();
        }
        Actor byId = jdbcMetaData.actorService.getById(metaDataParam.getActorId());
        return getTableSample(metaDataParam, byId);
    }

    /**
     * 获取表信息
     *
     * @param metaDataParam
     * @param byId
     * @return
     */
    public List<Table> getTableSchema(MetaDataParam metaDataParam, Actor byId) {
        String HOST_URL;
        HOST_URL = MYSQL_URL;
        DbConfig dbConfig = JSON.parseObject(byId.getConfiguration(), DbConfig.class);
        String db = dbConfig.getDatabase();
        String url = "";
        if (metaDataParam != null && StringUtils.isNotEmpty(metaDataParam.getType())) {
            if (metaDataParam.getType().equals(SourceTypeEnum.clickhouse.getType())) {
                HOST_URL = CLICKHOUSE_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType())) {
                url = String.format(DsTaskConstant.SQLSERVER_URL, dbConfig.getHost(), dbConfig.getPort(), db);
            } else if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                db = dbConfig.getSchemas().get(0);
                url = String.format(DsTaskConstant.ORACLE_URL, dbConfig.getHost(), dbConfig.getPort(), dbConfig.getSid());
            }
        }

        Map<String, String> parameters = new HashMap<>();

        if (!metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType()) &&
                !metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
            String urlParams = "";
            if (StringUtils.isNotEmpty(dbConfig.getJdbc_url_params())) {
                urlParams = "?" + dbConfig.getJdbc_url_params();
            }
            if (metaDataParam.getType().equals(SourceTypeEnum.doris.getType())) {
                url = String.format(HOST_URL, dbConfig.getHost(), dbConfig.getQueryPort(), db) + urlParams;
            } else {
                url = String.format(HOST_URL, dbConfig.getHost(), dbConfig.getPort(), db) + urlParams;
            }
        }

        parameters.put(URL, url);
        parameters.put(USER_NAME, dbConfig.getUsername());
        parameters.put(PASSWORD, getEncryptPassword(dbConfig.getPassword()));
        parameters.put(RDBM_TYPE, metaDataParam.getType());
        parameters.put(DB_NAME, db);
        List<Table> resultList = new ArrayList<>();
        Table resTable = new Table();
        List<Column> columns = new ArrayList<>();
//        MysqlStreams.Table table = tableMaps.get(metaDataParam.getTable()).stream().findFirst().get();
//        MysqlStreams.Table.JsonSchema jsonSchema = table.getJsonSchema();
//        Map<String, Map<String, String>> properties = jsonSchema.getProperties();
//        for (Map.Entry<String, Map<String, String>> entry : properties.entrySet()) {
//            Column column = new Column();
//            column.setName(entry.getKey());
//            column.setComment("");
//            column.setType(MysqlStreams.transformDataType(entry.getValue().get("type")));
//            column.setData_type(MysqlStreams.transformDataType(entry.getValue().get("type")));
//            columns.add(column);
//        }
        try {
//            Map<String, ColumnInfo> columnMap = mysqlMetaData.mySQLConnector.getColumnMap(byId.getId(), metaDataParam.getTable());
            JsonNode jsonNode = Jsons.deserialize(byId.getConfiguration());
            Map<String, ColumnInfo> columnMap = jdbcMetaData.mySQLConnector.getColumnMap(jsonNode, metaDataParam.getTable(), metaDataParam.getType());
            for (Map.Entry<String, ColumnInfo> entry : columnMap.entrySet()) {
                Column column = new Column();
                column.setName(entry.getKey());
                column.setComment(entry.getValue().getComment());
                column.setSize(entry.getValue().getLength());
                column.setType(entry.getValue().getType().toLowerCase());
                column.setData_type(entry.getValue().getType().toLowerCase());
                column.setIsPK(entry.getValue().getIsPK());
                columns.add(column);
            }

        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.META_DATA_SCHEMA_GET_FAILURE);
        }
        resTable.setParameters(parameters);
        resTable.setColumns(columns);
        resTable.setName(metaDataParam.getTable());
        resTable.setTypeName(SourceTypeEnum.SourceTypeMap.get(metaDataParam.getType()).name());
        resultList.add(resTable);
        return resultList;
    }

    /**
     * 获取表集合
     *
     * @return
     */
    public List<Table> getTableList(MetaDataParam metaDataParam, Actor byId) {
//        return mysqlStreams.getStreams().stream().map(data -> {
//            Table table = new Table();
//            table.setName(data.getName());
//            table.setTypeName(SourceTypeEnum.rdbms_table.type);
//            return table;
//        }).collect(Collectors.toList());
        JsonNode jsonNode = Jsons.deserialize(byId.getConfiguration());
        List<String> tableList;
        try {
            tableList = jdbcMetaData.mySQLConnector.getTableList(jsonNode, metaDataParam.getType());
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.META_DATA_SCHEMA_GET_FAILURE);
        }
        if (tableList.isEmpty()) {
            return new ArrayList<>();
        }
        return tableList.stream().map(data -> {
            Table table = new Table();
            table.setName(data);
            table.setTypeName(metaDataParam.getType());
            return table;
        }).collect(Collectors.toList());
    }

    public List<Map<String, String>> getTableSample(MetaDataParam metaDataParam, Actor byId) {
        JsonNode jsonNode = Jsons.deserialize(byId.getConfiguration());
        List<Map<String, String>> tableList;
        try {
            tableList = jdbcMetaData.mySQLConnector.getTableSample(jsonNode, metaDataParam.getTable(), metaDataParam.getType());
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.META_DATA_SCHEMA_GET_FAILURE);
        }

        return tableList;
    }

    /**
     * 获取数据库集合
     *
     * @param actor
     * @return
     */
    public List<Table> getDataBase(Actor actor) {
        DbConfig dbConfig = JSON.parseObject(actor.getConfiguration(), DbConfig.class);
        List<Table> res = new ArrayList<>();
        Table table = new Table();
        table.setName(dbConfig.getDatabase());
        res.add(table);
        return res;
    }

    /**
     * 获取source
     *
     * @param actorByType
     * @return
     */
    public List<Table> getSourceList(MetaDataParam metaDataParam, List<Actor> actorByType) {
        String HOST_URL = MYSQL_URL;
        if (metaDataParam != null && StringUtils.isNotEmpty(metaDataParam.getType())) {
            if (metaDataParam.getType().equals(SourceTypeEnum.clickhouse.getType())) {
                HOST_URL = CLICKHOUSE_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType())) {
                HOST_URL = DsTaskConstant.SQLSERVER_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                HOST_URL = DsTaskConstant.ORACLE_URL;
            }
        }

        String finalHOST_URL = HOST_URL;
        return actorByType.stream().map(data -> {
            Table table = new Table();
            table.setName(data.getName());
            table.setActorId(data.getId().toString());
            DbConfig dbConfig = JSON.parseObject(data.getConfiguration(), DbConfig.class);

            String db = "";
            if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                db = dbConfig.getSchemas().get(0);
            } else {
                db = dbConfig.getDatabase();
            }

            table.setDb(db);
            table.setSourceType(metaDataParam.getType());
            Map<String, String> parameters = new HashMap<>();
            String url = "";
            if (metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType())) {
                url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getPort(), db);
            } else if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getPort(), dbConfig.getSid());
            } else {
                String urlParams = "";
                if (StringUtils.isNotEmpty(dbConfig.getJdbc_url_params())) {
                    urlParams = "?" + dbConfig.getJdbc_url_params();
                }
                if (metaDataParam.getType().equals(SourceTypeEnum.doris.getType())) {
                    url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getQueryPort(), urlParams);
                } else {
                    url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getPort(), urlParams);
                }

            }

            parameters.put(URL, url);
            parameters.put(USER_NAME, dbConfig.getUsername());
            parameters.put(PASSWORD, getEncryptPassword(dbConfig.getPassword()));
            parameters.put(RDBM_TYPE, metaDataParam.getType());
            parameters.put(DB_NAME, db);
            table.setDbName(db);
            table.setUsername(dbConfig.getUsername());
            table.setPassword(getEncryptPassword(dbConfig.getPassword()));
            table.setUrl(url);

            table.setParameters(parameters);
            return table;
        }).collect(Collectors.toList());
    }

    public String getEncryptPassword(String password) {
        String encryptPassword;
        try {
            encryptPassword = EncryptUtil.encrypt(password, DsTaskConstant.METADATA_PASSWDKEY);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.META_DATA_PASSWORD_ENCRYPT_FAILURE);
        }
        return encryptPassword;
    }

    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        List<Table> search = search(metaDataParam);
        return search.stream().findFirst().get();
    }
}
