package com.ushareit.dstask.third.lakecat.impl;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.configuration.LakeCatConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.third.lakecat.LakeCatService;
import com.ushareit.dstask.third.lakecat.vo.LakeCatResponse;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/2/14
 */
@Slf4j
@Service
public class LakeCatServiceImpl implements LakeCatService {
    @Autowired
    private LakeService lakeService;

    private static final String CREATE_TENANT_URI = "/v1/%s/tenants";
    @Resource
    private LakeCatConfig lakeCatConfig;
    @Resource
    private RestTemplate restTemplate;

    @Value("${lakecat-url.host}")
    private String lakeCatHost;

    @Override
    public void createTenant(String tenantName) {
        String url = lakeCatConfig.getLakeCatHost() + String.format(CREATE_TENANT_URI, tenantName);
        log.info("create tenant for lakeCat url is {}", url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", lakeCatConfig.getToken());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<LakeCatResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                LakeCatResponse.class);
        log.info("response info is {}", response);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getStatusCode().getReasonPhrase());
        }

        if (response.getBody() == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "调用lakeCat接口返回空");
        }

        log.info("response body is {}", response.getBody());
        if (response.getBody().getCode() != 200 && response.getBody().getCode() != 403) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), response.getBody().getMessage());
        }
    }


    private String assemblyParams(String user, String region, String db, String table, String privilege) {
        Map<String, String> map = new HashMap<>();
        map.put("catalogName", region);
        map.put("operation", privilege);
        map.put("projectId", "");
        map.put("region", region);
        map.put("userId", user);

        if (privilege.equals(CommonConstant.CREATE_TABLE)) {
            map.put("qualifiedName", getCataLog(region) + region + "." + db);
        } else {
            map.put("qualifiedName", getCataLog(region) + region + "." + db + "." + table);
        }
        return JSONObject.toJSONString(map);
    }

    public String getCataLog(String region) {
        if (DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
            CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
            return cloudResource.getProvider() + "_" + cloudResource.getRegion();
        }
        return CommonConstant.CATALOG;
    }

    private ResponseEntity<LakeCatResponse> createRequest(String url, String requestBody) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String authentication = InfTraceContextHolder.get().getAuthentication();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CommonConstant.AUTHENTICATION_HEADER, authentication);
        httpHeaders.add(CommonConstant.CURRENT_LOGIN_USER, JSONObject.toJSONString(userInfo));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(requestBody, httpHeaders);
        return restTemplate.exchange(url, HttpMethod.POST, entity, LakeCatResponse.class);
    }

    public void checkTablePri(String user, String region, String db, String table, String privilege) {
        log.info("hive applyuser-->{}-region->{}-db->{}-table->{}-prvilege->{}",user,region,db,table,privilege);
        boolean auth=lakeService.doAuthByGroup(InfTraceContextHolder.get().getUuid(),region,db,table,privilege);
        if (!auth) {
            switch (privilege) {
                case CommonConstant.SELECT_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_READ_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_READ_NO_PRIVILEGE.getMessage() + String.format(":%s.%s", db, table));
                case CommonConstant.INSERT_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_WRITE_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_WRITE_NO_PRIVILEGE.getMessage() + String.format(":%s.%s", db, table));
                case CommonConstant.CREATE_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.getMessage() + String.format(":%s", db));
            }
        }
       /* String url = lakeCatHost + "/metadata/auth/doAuth";
        String requestBody = assemblyParams(user, region, db, table, privilege);
        ResponseEntity<LakeCatResponse> response = createRequest(url, requestBody);

        if (response.getStatusCodeValue() != 200 || response.getBody().getCode() != 0 || response.getBody().getData().equals("false")) {
            switch (privilege) {
                case CommonConstant.SELECT_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_READ_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_READ_NO_PRIVILEGE.getMessage() + String.format(":%s.%s", db, table));
                case CommonConstant.INSERT_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_WRITE_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_WRITE_NO_PRIVILEGE.getMessage() + String.format(":%s.%s", db, table));
                case CommonConstant.CREATE_TABLE:
                    throw new ServiceException(BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.name(), BaseResponseCodeEnum.HIVE_CREATE_NO_PRIVILEGE.getMessage() + String.format(":%s", db));
            }
        }*/
    }

}
