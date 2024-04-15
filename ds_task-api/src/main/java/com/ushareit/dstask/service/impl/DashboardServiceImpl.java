package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.DashboardBase;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.DashboardService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl extends AbstractBaseServiceImpl<DashboardBase> implements DashboardService {
    @Override
    public CrudMapper<DashboardBase> getBaseMapper() {
        return null;
    }

    @Override
    public Map<String, Object> getDashboardUrl(DashboardBase dashboard) {
        Map<String, Object> data = new HashMap<>();

        String env = InfTraceContextHolder.get().getEnv();
        String biUrl;
        if (env.equals(DsTaskConstant.DEV) || env.equals(DsTaskConstant.TEST)) {
            biUrl = DsTaskConstant.BI_URL_TEST;
        } else {
            biUrl = DsTaskConstant.BI_URL;
        }

        Map<String, String> params = new HashMap(1);
        params.put("viewid", dashboard.getViewId().toString());
        Map<String, String> headers = new HashMap(1);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
        BaseResponse resp = HttpUtil.get(biUrl + "/view/get_view_url", params, headers);
        if (resp == null || resp.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject respJson = resp.get();
        if (respJson.getInteger("result_code") == 405) {
            log.warn("get dashboard url failed ,resp:" + JSONObject.toJSONString(respJson));
            data.put("permission", false);
            return data;
        }

        if (respJson.getInteger("result_code") != 200) {
            log.warn("get dashboard url failed ,resp:" + JSONObject.toJSONString(respJson));
            throw new ServiceException(BaseResponseCodeEnum.BI_GET_DASHBOARD_FAIL);
        }

        log.info("get dashboard url:" + respJson.getString("iframe_url"));
        StringBuilder dashboardUrl = new StringBuilder(respJson.getString("iframe_url"));
        if (dashboard.getParams() != null && dashboard.getParams().size() > 0) {
            dashboard.getParams().forEach(
                    param -> {
                        dashboardUrl.append("&" + param.getKey() + "=" + param.getValue());
                    }
            );
        }
        data.put("permission", true);
        data.put("url", dashboardUrl.toString());
        return data;
    }

    @Override
    public void addPermission(){
        Map<String, Object> data = new HashMap<>();

        String env = InfTraceContextHolder.get().getEnv();
        String biUrl;
        String permissionDir;
        if (env.equals(DsTaskConstant.DEV) || env.equals(DsTaskConstant.TEST)) {
            biUrl = DsTaskConstant.BI_URL_TEST;
            permissionDir = DsTaskConstant.BI_DS_DASHBOARD_PERMISSION_TEST;
        } else {
            biUrl = DsTaskConstant.BI_URL;
            permissionDir = DsTaskConstant.BI_DS_DASHBOARD_PERMISSION;
        }

        Map<String, String> params = new HashMap(4);
        String[] dirs = permissionDir.split(",");
        params.put("dir_level1",dirs[0]);
        params.put("dir_level2",dirs[1]);
        params.put("dir_level3",dirs[2]);
        params.put("reason","ds看板权限申请");

        Map<String, String> headers = new HashMap(1);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
        BaseResponse resp = HttpUtil.doPost(biUrl + "/oa_approve/oa_zeus_form", params, headers);
        if (resp == null || resp.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject respJson = resp.get();

        if (respJson.getInteger("result_code") != 200) {
            log.warn("get dashboard url failed ,resp:" + JSONObject.toJSONString(respJson));
            throw new ServiceException(BaseResponseCodeEnum.BI_ADD_PERMISSION_FAIL);
        }
    }
}
