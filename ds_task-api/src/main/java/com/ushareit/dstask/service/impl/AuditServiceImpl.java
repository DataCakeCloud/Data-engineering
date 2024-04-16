package com.ushareit.dstask.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.Module;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.ApiGatewayMapper;
import com.ushareit.dstask.mapper.SysDictMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AuditService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import com.ushareit.dstask.web.utils.DateUtil;
import com.ushareit.dstask.web.utils.TimestampUtil;
import com.ushareit.dstask.web.utils.UrlUtil;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.model.TableSource;
import io.lakecat.catalog.common.model.TableUsageProfile;
import io.lakecat.catalog.common.plugin.request.GetUsageProfileDetailsRequest;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuditServiceImpl extends AbstractBaseServiceImpl<ApiGateway> implements AuditService {

    @Resource
    private TimestampUtil timestampUtil;

    @Resource
    private ApiGatewayMapper apiGatewayMapper;

    @Resource
    private SysDictMapper sysDictMapper;

    @Autowired
    private Lakecatutil lakecatutil;

    @Override
    public CrudMapper<ApiGateway> getBaseMapper() {
        return (CrudMapper<ApiGateway>) apiGatewayMapper;
    }

    @Override
    public List<Map<String, String>> uriMap(String uri, CurrentUser currentUser) {
        log.info(String.format("%s query uri mapping table", currentUser.getUserName()));
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        List<SysDict> sysDicts = sysDictMapper.selectCodeValue(uri);
        return sysDicts.stream()
                .map(sysDict -> {
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("code", "/" + sysDict.getSource() + sysDict.getCode());
                    resultMap.put("value", sysDict.getValue());
                    return resultMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> moduleEvent(String source, String name, CurrentUser currentUser) {
        log.info(String.format("%s query module event map", currentUser.getUserName()));
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        List<SysDict> sysDicts = sysDictMapper.selectSource(source, name);
        return sysDicts.stream()
                .map(sysDict -> {
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("code", "/" + sysDict.getSource() + sysDict.getCode());
                    resultMap.put("value", sysDict.getValue());
                    return resultMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo<ApiGateway> auditFunction(Integer pageNum, Integer pageSize, ApiGateway apiGateway, CurrentUser currentUser) {
        log.info(String.format("%s query auditFunction", currentUser.getUserName()));
        PageHelper.startPage(pageNum, pageSize);
        if (apiGateway.getStartTime() != null && apiGateway.getStartTime() != 0) {
            apiGateway.setStartTimeFormat(timestampUtil.formatTimestamp(apiGateway.getStartTime()));
        }
        if (apiGateway.getEndTime() != null && apiGateway.getEndTime() != 0) {
            apiGateway.setEndTimeFormat(timestampUtil.formatTimestamp(apiGateway.getEndTime()));
        }
        List<ApiGateway> auditLog = apiGatewayMapper.getAuditLog(apiGateway);

        Module module = new Module();
        List<Map<String, String>> maps = moduleEvent(apiGateway.getSource(), "", currentUser);
        Map<String, String> uriNameMap = new HashMap<>();
        for (Map<String, String> map: maps) {
            uriNameMap.put(map.get("code"), map.get("value"));
        }

        List<ApiGateway> collect = auditLog.stream()
                .map(info -> {
                    info.setSource(module.getValue(info.getSource()));
                    String uri = info.getUri().replace(CommonConstant.URI_PREFIX, "");
                    String newUri = UrlUtil.assembleUri(uri);
                    String eventName = uriNameMap.containsKey(uri) ? uriNameMap.get(uri) : newUri;
                    info.setUri(newUri);
                    info.setEventName(eventName);
                    info.setRequestTime(DateUtil.add8Hour(info.getRequestTime()));
                    info.setResponseTime(DateUtil.add8Hour(info.getResponseTime()));
                    String params = info.getParams();
                    if (params.startsWith("{")) {
                        info.setParam(JSONObject.parseObject(params));
                    } else if (params.startsWith("\"{")) {
                        params = params.substring(1, params.length()-1);
                        params = params.replace("\\\"", "\"");
                        info.setParam(JSONObject.parseObject(params, Map.class));
                    } else {
                        Map<String, Object> param = new HashMap<>();
                        param.put("param", params);
                        info.setParam(param);
                    }
                    return info;
                })
                .collect(Collectors.toList());
        return new PageInfo<>(collect);
    }

    @Override
    public PagedList<TableUsageProfile> auditData(DataAudit audit, CurrentUser currentUser) {
        log.info(String.format("%s query auditData", currentUser.getUserName()));
        if (StringUtils.isEmpty(audit.getDatabase())) {
            throw new ServiceException(BaseResponseCodeEnum.DATABASE_NO_EXIST);
        }
        LakeCatClient lakeCatClient = lakecatutil.getClient();
        String projectId = lakeCatClient.getProjectId();
        TableSource tableSource = new TableSource();
//        projectId = "shareit";
        tableSource.setProjectId(projectId);
//        catalogName = "shareit_ue1";
        tableSource.setCatalogName(audit.getCatalogName());
        tableSource.setDatabaseName(audit.getDatabase());
        if (StringUtils.isNotEmpty(audit.getTable())) {
            tableSource.setTableName(audit.getTable());
        }
        GetUsageProfileDetailsRequest getUsageProfileDetailsRequest = new GetUsageProfileDetailsRequest(projectId, tableSource);
        getUsageProfileDetailsRequest.setOperations(audit.getOperations());
        getUsageProfileDetailsRequest.setRowCount(CommonConstant.CATALOG_ROW);
        if (StringUtils.isNotEmpty(audit.getNextMarker())) {
            getUsageProfileDetailsRequest.setPageToken(audit.getNextMarker());
        }
        if (audit.getStartTime() != null && audit.getStartTime() != 0) {
            getUsageProfileDetailsRequest.setStartTimestamp(audit.getStartTime());
        }
        if (audit.getEndTime() != null && audit.getEndTime() != 0) {
            getUsageProfileDetailsRequest.setEndTimestamp(audit.getEndTime());
        }
        if (StringUtils.isNotEmpty(audit.getUserId())) {
            getUsageProfileDetailsRequest.setUserId(audit.getUserId());
        }

        PagedList<TableUsageProfile> usageProfileDetails = lakeCatClient.getUsageProfileDetails(getUsageProfileDetailsRequest);
        List<TableUsageProfile> tableUsageProfiles = Arrays.asList(usageProfileDetails.getObjects());
        List<TableUsageProfile> modifiedList = tableUsageProfiles.stream()
                .map(profile -> {
                    TableSource table = profile.getTable();
                    String databaseName = table.getDatabaseName();
                    String tableName = table.getTableName();
                    if (databaseName != null && tableName != null) {
                        table.setTableName(databaseName + "." + tableName);
                    } else {
                        table.setTableName(databaseName);
                    }
                    List<String> opTypes = getOpTypes(profile.getOpTypes(), profile.getOriginOpTypes());
                    profile.setOpTypes(opTypes);
                    return profile;
                })
                .collect(Collectors.toList());
        usageProfileDetails.setObjectList(modifiedList);
        return usageProfileDetails;
    }

    private List<String> getOpTypes(List<String> opTypes, List<String> originOpTypes) {
        ArrayList<String> ops = new ArrayList<>();
        for (String op: opTypes) {
            if (op != null) {
                ops.add(op);
            }
        }
        for (String op: originOpTypes) {
            if (op != null) {
                ops.add(op);
            }
        }
        return ops;
    }
}
