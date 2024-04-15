package com.ushareit.dstask.web.metadata.airbyte;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.OwnerApp;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.service.OwnerAppService;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.service.impl.UserGroupServiceImpl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.EncryptUtil;
import com.ushareit.engine.param.ActorProvider;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.seatunnel.util.DataSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SeatunnelMetaData extends AirByteMetaData {

    private static SeatunnelMetaData seatunnelMetaData;

    @Resource
    public ActorService actorService;
    @Resource
    public TaskServiceImpl taskService;
    @Resource
    public UserGroupServiceImpl userGroupService;

    @Resource
    public OwnerAppService ownerAppService;

    @Resource
    public ActorDefinitionService actorDefinitionService;

    @PostConstruct
    public void init() {
        seatunnelMetaData = this;
        seatunnelMetaData.actorDefinitionService = this.actorDefinitionService;
        seatunnelMetaData.actorService = this.actorService;
        seatunnelMetaData.userGroupService = this.userGroupService;
        seatunnelMetaData.taskService = this.taskService;
    }

    public SeatunnelMetaData(MetaDataParam metaDataParam) {
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
            actorByType = seatunnelMetaData.actorDefinitionService.getActorByTypeAndRegion(metaDataParam.getType(), metaDataParam.getRegion());
            if (actorByType.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            //拿这actor 和 table 查表
            Actor byId = seatunnelMetaData.actorService.getById(metaDataParam.getActorId());

            if (StringUtils.isEmpty(metaDataParam.getDb()) && StringUtils.isEmpty(metaDataParam.getTable()) && StringUtils.isEmpty(metaDataParam.getSourceParam())) {
                return getDataBase(byId);
            }
            if (StringUtils.isNotEmpty(metaDataParam.getTable()) || StringUtils.isNotEmpty(metaDataParam.getSourceParam())) {
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
        Actor byId = seatunnelMetaData.actorService.getById(metaDataParam.getActorId());
        //String sourceName, String runtimeConfig, String type, String sourceConfigStr
        Catalog catalog = new Catalog();
        com.ushareit.engine.param.Table ta = new com.ushareit.engine.param.Table();
        if (metaDataParam.getType().equals("oracle")) {
            ta.setSourceTable(String.format("%s.%s", metaDataParam.getDb(), metaDataParam.getTable()));
        } else if (metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
            String schema = JSON.parseObject(byId.getConfiguration()).getJSONArray("schemas").getString(0);
            ta.setSourceTable(schema + "." + metaDataParam.getTable());
        }  else {
            ta.setSourceTable(metaDataParam.getTable());
        }

        List<com.ushareit.engine.param.Table> tables = new ArrayList<>();
        tables.add(ta);
        catalog.setTables(tables);

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setSourceParam(metaDataParam.getSourceParam());
        runtimeConfig.setExecuteMode("local")
                .setCatalog(catalog);
        return DataSourceUtil.getSample(byId.getName(),
                JSON.toJSONString(runtimeConfig), metaDataParam.getType(), byId.getConfiguration());
    }

    public Boolean checkConnection(MetaDataParam metaDataParam) {
        String name;
        String configuration;
        if (metaDataParam.getSourceDefinitionId() != null) {
            name = metaDataParam.getName();
            configuration = String.valueOf(metaDataParam.getConnectionConfiguration());
        } else {
            Actor byId = seatunnelMetaData.actorService.getById(metaDataParam.getActorId());
            name = byId.getName();
            configuration = byId.getConfiguration();
        }

        Catalog catalog = new Catalog();
        com.ushareit.engine.param.Table table = new com.ushareit.engine.param.Table();
        if (metaDataParam.getType().equals("oracle")) {
            table.setSourceTable("dual");
        } else if (metaDataParam.getType().equals("s3")) {
            ActorProvider actorProvider = JSON.parseObject(JSON.parseObject(configuration).getString("provider"), ActorProvider.class);
            table.setSourceTable(actorProvider.getBucket());
        } else {
            table.setSourceTable("empty");
        }

        List<com.ushareit.engine.param.Table> tables = new ArrayList<>();
        tables.add(table);
        catalog.setTables(tables);

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setExecuteMode("local").setCatalog(catalog);
        return DataSourceUtil.checkConnection(name,
                JSON.toJSONString(runtimeConfig), metaDataParam.getType(), configuration);
    }

    public Boolean createTable(MetaDataParam metaDataParam) {
        Actor byId = seatunnelMetaData.actorService.getById(metaDataParam.getActorId());

        Catalog catalog = new Catalog();
        com.ushareit.engine.param.Table table = new com.ushareit.engine.param.Table();

        List<com.ushareit.engine.param.Table> tables = new ArrayList<>();
        tables.add(table);
        catalog.setTables(tables);

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setExecuteMode("local").setCatalog(catalog);
        runtimeConfig.setCreateTableSql(metaDataParam.getCreateTableSql());
        return DataSourceUtil.createTable(byId.getName(),
                JSON.toJSONString(runtimeConfig), metaDataParam.getType(), byId.getConfiguration());
    }

    /**
     * 获取表信息
     */
    public List<Table> getTableSchema(MetaDataParam metaDataParam, Actor byId) {
        String HOST_URL;
        HOST_URL = DsTaskConstant.MYSQL_URL;
        DbConfig dbConfig = JSON.parseObject(byId.getConfiguration(), DbConfig.class);
        String db = dbConfig.getDatabase();
        String url = "";
        if (metaDataParam != null && StringUtils.isNotEmpty(metaDataParam.getType())) {
            if (metaDataParam.getType().equals(SourceTypeEnum.clickhouse.getType())) {
                HOST_URL = DsTaskConstant.CLICKHOUSE_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType())) {
                url = String.format(DsTaskConstant.SQLSERVER_URL, dbConfig.getHost(), dbConfig.getPort(), db);
            } else if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                db = dbConfig.getSchemas().get(0);
                url = String.format(DsTaskConstant.ORACLE_URL, dbConfig.getHost(), dbConfig.getPort(), dbConfig.getSid());
            } else if (metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
                url = String.format(DsTaskConstant.POSTGRES_URL, dbConfig.getHost(), dbConfig.getHost(), db, StringUtils.isNotEmpty(dbConfig.getJdbc_url_params()) ? dbConfig.getJdbc_url_params() : "");
            }
        }

        Map<String, String> parameters = new HashMap<>();

        if (!metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType()) &&
                !metaDataParam.getType().equals(SourceTypeEnum.oracle.getType()) &&
                !metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
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

        if(StringUtils.isNotBlank(db)){
            parameters.put(URL, url);
            parameters.put(USER_NAME, dbConfig.getUsername());
            parameters.put(PASSWORD, getEncryptPassword(dbConfig.getPassword()));
            parameters.put(RDBM_TYPE, metaDataParam.getType());
            parameters.put(DB_NAME, db);
        }
        List<Table> resultList = new ArrayList<>();
        Table resTable = new Table();
        List<Column> columns = new ArrayList<>();

        try {
            //String sourceName, String runtimeConfig, String type, String sourceConfigStr
            Catalog catalog = new Catalog();
            com.ushareit.engine.param.Table ta = new com.ushareit.engine.param.Table();
            if (metaDataParam.getType().equals("oracle") || metaDataParam.getType().equals((SourceTypeEnum.hana.getType()))) {
                ta.setSourceTable(String.format("%s.%s", metaDataParam.getDb(), metaDataParam.getTable()));
            } else if (metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
                ta.setSourceTable(dbConfig.getSchemas().get(0) + "." + metaDataParam.getTable());
            } else {
                ta.setSourceTable(metaDataParam.getTable());
            }

            List<com.ushareit.engine.param.Table> tables = new ArrayList<>();
            tables.add(ta);
            catalog.setTables(tables);

            RuntimeConfig runtimeConfig = new RuntimeConfig();
            if(StringUtils.isNotBlank(metaDataParam.getSourceParam())){
                String sourceParam = seatunnelMetaData.taskService.renderContent(metaDataParam.getSourceParam(), "");
                runtimeConfig.setSourceParam(sourceParam);
            }
            runtimeConfig.setExecuteMode("local")
                    .setCatalog(catalog);
            List<Map<String, String>> schema = DataSourceUtil.getSchema(byId.getName(),
                    JSON.toJSONString(runtimeConfig), metaDataParam.getType(), byId.getConfiguration());

            for (Map<String, String> entry : schema) {
                Column column = new Column();
                column.setName(entry.get("name"));
                column.setComment(entry.get("comment"));
                column.setType(entry.get("type").toLowerCase());
                column.setData_type(entry.get("type").toLowerCase());
                if(entry.get("isPk")!=null){
                    column.setIsPK(Boolean.parseBoolean(entry.get("isPk")));
                }
                columns.add(column);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
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
        //String sourceName, String runtimeConfig, String type, String sourceConfigStr
        Catalog catalog = new Catalog();
        com.ushareit.engine.param.Table ta = new com.ushareit.engine.param.Table();
        if (metaDataParam.getType().equals("oracle")) {
            ta.setSourceTable(metaDataParam.getDb() + ".*");
        } else if (metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
            String schema = JSON.parseObject(byId.getConfiguration()).getJSONArray("schemas").getString(0);
            ta.setSourceTable(schema);
        } else {
            ta.setSourceTable("empty");
        }

        List<com.ushareit.engine.param.Table> tables = new ArrayList<>();
        tables.add(ta);
        catalog.setTables(tables);

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setExecuteMode("local")
                .setCatalog(catalog);
        List<Map<String, Object>> tableList = DataSourceUtil.getTables(byId.getName(),
                JSON.toJSONString(runtimeConfig), metaDataParam.getType(), byId.getConfiguration());
        if (tableList.isEmpty()) {
            return new ArrayList<>();
        }

        return tableList.stream().map(data -> {
            Table table = new Table();
            table.setName(data.get("name").toString());
            table.setComment(data.get("comment").toString());
            table.setTypeName(metaDataParam.getType());
            return table;
        }).collect(Collectors.toList());
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
        String HOST_URL = DsTaskConstant.MYSQL_URL;
        if (metaDataParam != null && StringUtils.isNotEmpty(metaDataParam.getType())) {
            if (metaDataParam.getType().equals(SourceTypeEnum.clickhouse.getType())) {
                HOST_URL = DsTaskConstant.CLICKHOUSE_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.sql_server.getType())) {
                HOST_URL = DsTaskConstant.SQLSERVER_URL;
            } else if (metaDataParam.getType().equals(SourceTypeEnum.oracle.getType())) {
                HOST_URL = DsTaskConstant.ORACLE_URL;
            } else if ((metaDataParam.getType().equals(SourceTypeEnum.postgres.getType()))) {
                HOST_URL = DsTaskConstant.POSTGRES_URL;
            }
        }

        String finalHOST_URL = HOST_URL;
        return actorByType.stream().filter(data->{
            UserGroup userGroup = seatunnelMetaData.userGroupService.selectUserGroupByUuid(data.getCreateUserGroupUuid());
            if (userGroup==null){
                return false;
            }
            if(userGroup.getDeleteStatus() == 0) {
                return true;
            }else{
                return false;
            }
        }).map(data -> {
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
                    url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getQueryPort(), db) + urlParams;
                } else if (metaDataParam.getType().equals(SourceTypeEnum.postgres.getType())) {
                    url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getPort(), db, urlParams);
                } else {
                    url = String.format(finalHOST_URL, dbConfig.getHost(), dbConfig.getPort(), db) + urlParams;
                }
            }
            // 修改用户组名称为归属应用名称
            OwnerApp ownerApp = seatunnelMetaData.ownerAppService.getById(data.getOwnerAppId());
            table.setUserGroupName(null == ownerApp? null : ownerApp.getName());
//            UserGroup userGroup = seatunnelMetaData.userGroupService.selectUserGroupById(Integer.parseInt(data.getGroups()));
//            table.setUserGroupName(userGroup.getName());
            if(StringUtils.isNotBlank(db)){
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
            }
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
