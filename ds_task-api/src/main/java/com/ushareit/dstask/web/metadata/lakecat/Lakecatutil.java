package com.ushareit.dstask.web.metadata.lakecat;


import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.UrlUtil;
import io.lakecat.catalog.client.CatalogUserInformation;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.LakeCatConf;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.plugin.request.AuthenticationRequest;
import io.lakecat.catalog.common.plugin.request.GetTableRequest;
import io.lakecat.catalog.common.plugin.request.ListDatabasesRequest;
import io.lakecat.catalog.common.plugin.request.ListTablesRequest;
import io.lakecat.catalog.common.plugin.request.input.AuthorizationInput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * author:xuebotao
 * date:2023-03-20
 */
@Component
@Slf4j
public class Lakecatutil {

    @Value("${gateway.url}")
    public String gatewayUrl;

    @Value("${lakecat.server.url}")
    public String url;

    @Value("${lakecat.server.port}")
    public String port;

    @Value("${lakecat.server.user}")
    public String user;

    @Value("${lakecat.server.password}")
    public String password;

    @Resource
    public CloudResourcesService cloudResourcesService;


    public static String HIVE_DB = "hive_db";

    public String getCatalogName(String tenantName, String region) {

        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        return cloudResource.getProvider() + "_" + cloudResource.getRegion();

    }

//    @Cacheable(cacheNames = {"metadataV2"}, key = "#region+'-'+#tenantName")
    public List<Table> getMetaDatabase(String tenantName, String region) {

        List<Table> tableList = new ArrayList<>();
        LakeCatClient lakeCatClient = getClient();

        ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId(tenantName);
        listDatabasesRequest.setCatalogName(getCatalogName(tenantName, region));
        PagedList<Database> databasePagedList;

        try {
            log.info("listDatabasesRequest is :" + listDatabasesRequest);
            databasePagedList = lakeCatClient.listDatabases(listDatabasesRequest);
        } catch (Exception exception) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }

        List<String> databaseList = Arrays.stream(databasePagedList.getObjects()).map(Database::getDatabaseName).sorted().collect(Collectors.toList());
        List<Object> collect = databaseList.stream().map(data -> {
            Table table = new Table();
            table.setName(data);
            table.setSourceType(HIVE_DB);
            table.setTypeName(HIVE_DB);
            tableList.add(table);
            return data;
        }).collect(Collectors.toList());
        return tableList;
    }


    //@Cacheable(cacheNames = {"metadataV2"}, key = "#region +'-'+#db + '-' +#tenantName")
    public List<Table> getMetaTable(String tenantName, String region, String db) {
        List<Table> tableList = new ArrayList<>();
        LakeCatClient lakeCatClient = getClient();

        ListTablesRequest listTablesRequest = new ListTablesRequest();
        listTablesRequest.setDatabaseName(db);
        listTablesRequest.setMaxResults(10000);
        listTablesRequest.setProjectId(tenantName);

        listTablesRequest.setCatalogName(getCatalogName(tenantName, region));
        PagedList<String> tablePagedList;
        try {
            tablePagedList = lakeCatClient.listTableNames(listTablesRequest);
        } catch (Exception exception) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }
        List<String> databaseList = Arrays.stream(tablePagedList.getObjects())
                .sorted().collect(Collectors.toList());

        List<Object> collect = databaseList.stream().map(data -> {
            Table table = new Table();
            table.setName(data);
            table.setSourceType(HIVE_DB);
            table.setTypeName(HIVE_DB);
            tableList.add(table);
            return data;
        }).collect(Collectors.toList());
        return tableList;
    }


//    @Cacheable(cacheNames = {"metadataV2"}, key = "#region +'-'+#db+'-'+#tenantName+'-'+#table +'-'+#userId+'-'+#judgeTable")
    public List<Table> getMetaColumn(String tenantName, String region, String db, String table, String userId,Boolean judgeTable) {

        List<Table> tableList = new ArrayList<>();
        io.lakecat.catalog.common.model.Table tableRes = null;
        Table tab = new Table();
        if (judgeTable) {
            boolean auth = doAuth(region, db, table, userId);
            if (!auth) {
                throw new ServiceException(BaseResponseCodeEnum.NOT_OPERATION_TABLE_PERMISSION);
            }
        }

        try {
            LakeCatClient lakeCatClient = getClient();
            GetTableRequest getTableRequest = new GetTableRequest(tenantName, getCatalogName(tenantName, region), db, table);
            tableRes = lakeCatClient.getTable(getTableRequest);
        } catch (Exception exception) {
            if (exception.getMessage().contains(table + " not found")) {
                tab.setIsTalbeExist(false);
                tableList.add(tab);
                return tableList;
            }
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }
        String description = tableRes.getDescription();
        String securityLevel = tableRes.getStorageDescriptor().getParameters().get("SECURITY_LEVEL");
        tab.setDescription(description);
        tab.setSecurityLevel(securityLevel);
        List<Column> fields = tableRes.getFields();
        String location = tableRes.getStorageDescriptor().getLocation();
        List<com.ushareit.dstask.web.ddl.metadata.Column> sourceColumn = new ArrayList<>();

        for (Column column : fields) {
            com.ushareit.dstask.web.ddl.metadata.Column dsColumn = new com.ushareit.dstask.web.ddl.metadata.Column();
            dsColumn.setName(column.getColumnName().trim())
                    .setType(column.getColType().trim().toLowerCase())
                    .setComment(column.getComment().trim());
            sourceColumn.add(dsColumn);
        }
        if (sourceColumn.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<Column> partitionColumns = tableRes.getPartitionKeys();
        List<com.ushareit.dstask.web.ddl.metadata.Column> partColumns = new ArrayList<>();
        if(partitionColumns!=null && !partitionColumns.isEmpty()){
            partColumns= partitionColumns.stream().map(data->{
                com.ushareit.dstask.web.ddl.metadata.Column column = new com.ushareit.dstask.web.ddl.metadata.Column();
                column.setName(data.getColumnName())
                        .setType(data.getColType())
                        .setComment(data.getComment());
                return column;
            }).collect(Collectors.toList());
        }
        tab.setPartitionKeys(partColumns);
        tab.setColumns(sourceColumn);
        tab.setLocation(location);
        tableList.add(tab);

        List<String> partitionKeys = partitionColumns.stream().map(Column::getColumnName)
                .collect(Collectors.toList());

        if (!partitionKeys.isEmpty()) {
            List<com.ushareit.dstask.web.ddl.metadata.Column> columns = tab.getColumns();
            tab.setColumns(columns.stream().
                    filter(data -> !partitionKeys.contains(data.getName())).
                    collect(Collectors.toList()));
        }

        return tableList;
    }

    public String getTableDelmiter(String tenantName, String region, String db, String table){
        io.lakecat.catalog.common.model.Table tableDetail;
        String delimiter;
        try {
            LakeCatClient lakeCatClient = getClient();
            GetTableRequest getTableRequest = new GetTableRequest(tenantName, getCatalogName(tenantName, region), db, table);
            tableDetail = lakeCatClient.getTable(getTableRequest);
            Map<String, String> parameters = tableDetail.getStorageDescriptor().getSerdeInfo().getParameters();
            if (parameters.containsKey("field.delim")){
                if(parameters.get("field.delim").contains("2")){
                    delimiter="\2";
                }else{
                    delimiter="\t";
                }

            }else{
                delimiter = "\t";
            }
        } catch (Exception exception) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }
        return delimiter;
    }

    public Map<String, String> getTableParameters(String tenantName, String region, String db, String table){
        io.lakecat.catalog.common.model.Table tableDetail;
        try {
            LakeCatClient lakeCatClient = getClient();
            GetTableRequest getTableRequest = new GetTableRequest(tenantName, getCatalogName(tenantName, region), db, table);
            tableDetail = lakeCatClient.getTable(getTableRequest);
        } catch (Exception exception) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }
        return tableDetail.getParameters();

    }


    public String getFileFormat(String tenantName, String region, String db, String table){
        io.lakecat.catalog.common.model.Table tableDetail;
        try {
            LakeCatClient lakeCatClient = getClient();
            GetTableRequest getTableRequest = new GetTableRequest(tenantName, getCatalogName(tenantName, region), db, table);
            tableDetail = lakeCatClient.getTable(getTableRequest);
        } catch (Exception exception) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, exception);
        }
        return tableDetail.getStorageDescriptor().getInputFormat();

    }
    public LakeCatClient getClient() {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        tenantName=StringUtils.isBlank(tenantName)? BaseConstant.defaultTenantName :tenantName;
//        try {
        LakeCatClient lakeCatClient = LakeCatClient.getInstance(getConfiguration(), true);
        lakeCatClient.getContext().setProjectId(tenantName);
        return lakeCatClient;
//        } catch (Exception e) {
//            throw new RuntimeException("get lakecat client fail" + e.getMessage());
//        }
    }


    public Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.set(LakeCatConf.CATALOG_HOST, url);
        conf.setInt(LakeCatConf.CATALOG_PORT, Integer.parseInt(port));
        conf.set(CatalogUserInformation.LAKECAT_USER_NAME, user);
        conf.set(CatalogUserInformation.LAKECAT_USER_PASSWORD, password);
        return conf;
    }


    /**
     * 查询表结构前 先查询是否对表有权限
     */
    public boolean doAuth(String region, String db, String table, String userId) {
        AuthenticationReq authenticationReq = new AuthenticationReq();

        String tenantName = InfTraceContextHolder.get().getTenantName();
        authenticationReq.setUserId(userId)
                .setProjectId(tenantName)
                .setOperation("DESC_TABLE")
                .setCatalogName(getCatalogName(tenantName, region))
                .setQualifiedName(authenticationReq.getCatalogName() + "." + db + "." + table)
                .setRegion(region);

        LakeCatClient lakeCatClient = getClient();
        List<AuthorizationInput> list = new ArrayList<>();
        AuthorizationInput authorizationInput = new AuthorizationInput();

        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);

        list.add(buildAuthorizationInput(authenticationReq));
        AuthorizationResponse authenticate = null;
        try {
            authenticate = lakeCatClient.authenticate(new AuthenticationRequest(authenticationReq.getProjectId(), false, list));
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECAT_METADATA_GET_FAILURE, e);
        }
        return authenticate.getAllowed();
    }


    private AuthorizationInput buildAuthorizationInput(AuthenticationReq authenticationReq) {
        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(Operation.valueOf(authenticationReq.getOperation()));
        User user = new User();
        user.setUserId(authenticationReq.getUserId());
        authorizationInput.setUser(user);
        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(authenticationReq.getProjectId());
        try {
            catalogObject.setCatalogName(authenticationReq.getCatalogName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String qualifiedName = authenticationReq.getQualifiedName();
        String[] split = qualifiedName.split("\\.");
        catalogObject.setDatabaseName(split[1]);
        catalogObject.setObjectName(split[2]);
        authorizationInput.setCatalogInnerObject(catalogObject);
        log.debug("authorizationInput: {}", JSONObject.toJSONString(authorizationInput));
        return authorizationInput;
    }


}
