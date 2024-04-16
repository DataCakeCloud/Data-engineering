package com.ushareit.dstask.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.utils.ObjectId;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItUtil {

    private static ItUtil itUtil;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessGroupService accessGroupService;


    @PostConstruct
    public void init() {
        itUtil = this;
        itUtil.accessUserService = this.accessUserService;
        itUtil.accessGroupService = this.accessGroupService;
    }

    public static final String getId() {
        return ObjectId.get().toHexString();
    }

    private static String[] chars = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    /**
     * 生成 8 位不重复 (数量较少时保证) 的 ID, 目前用于标识广告位
     * <p>
     * 由数字 0-9, 小写字母 26 个, 大写字母 26 个随机构成
     *
     * @return 8 位 tag ID
     */
    public static String getLenthId(int len) {
        StringBuilder shortBuffer = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < len; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }


    /**
     * 获取用户信息
     *
     * @param name
     * @return
     */
    public List<UserBase> getUsersInfo(String name) {
        log.info("getUsersInfo name is :" + name);
        Map<String, String> params = new HashMap();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            return getUserInfoByName(Collections.singletonList(name));
        }

        params.put("name", name);
        String itPersonnelInfo = getItPersonnelInfo("user info", DsTaskConstant.IT_GET_USER_URL,
                params, null, DsTaskConstant.GET, BaseResponseCodeEnum.IT_GET_USER_FAIL);
        return JSONObject.parseArray(itPersonnelInfo, UserBase.class);
    }

    /**
     * 获取部门列表信息
     *
     * @return
     */
    public List<DeptInfo> getDepartmentsList() {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            AccessGroup build = AccessGroup.builder().tenantId(tenantId).type(0).build();
            build.setDeleteStatus(0);
            List<AccessGroup> accessGroupList = itUtil.accessGroupService.listByExample(build);
            return accessGroupList.stream().map(DeptInfo::conversion).collect(Collectors.toList());
        }

        String itPersonnelInfo = getItPersonnelInfo("department list", DsTaskConstant.IT_GET_DEPARTMENT_LIST_URL,
                new HashMap(), null, DsTaskConstant.GET, BaseResponseCodeEnum.IT_GET_DEPARTMENT_LIST_FAIL);
        return JSONObject.parseArray(itPersonnelInfo, DeptInfo.class);
    }

    /**
     * 获取员工部门信息
     *
     * @param shareitId
     * @return
     */
    public List<DeptInfo> getDeptInfo(String shareitId) {
        Map<String, String> params = new HashMap();
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            List<DeptInfo> res = new ArrayList<>();
            List<AccessUser> accessUsers = itUtil.accessUserService.selectByNames(Collections.singletonList(shareitId), tenantId);
            List<Integer> ids = accessUsers.stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<AccessGroup> accessGroupList = itUtil.accessGroupService.selectByUserIds(ids);

            Map<Integer, List<AccessGroup>> userGroupMap = accessGroupList.stream().collect(Collectors.groupingBy(AccessGroup::getUserId));

            List<Integer> groupIds = accessGroupList.stream().map(AccessGroup::getParentId).collect(Collectors.toList());
            Map<Integer, List<AccessGroup>> groupMap = itUtil.accessGroupService.listByIds(groupIds)
                    .stream().collect(Collectors.groupingBy(AccessGroup::getId));
            for (AccessUser accessUser : accessUsers) {
                AccessGroup accessGroup = userGroupMap.get(accessUser.getId()).stream().findFirst().get();
                AccessGroup group = groupMap.get(accessGroup.getParentId()).stream().findFirst().get();
                res.add(DeptInfo.conversion(group));
            }

            return res;
        }
        params.put("ShareId", shareitId);
        String itPersonnelInfo = getItPersonnelInfo("dept info", DsTaskConstant.IT_GET_DEPT_INFO_URL,
                params, null, DsTaskConstant.POST, BaseResponseCodeEnum.IT_GET_DEPT_INFO_FAIL);
        return JSONObject.parseArray(itPersonnelInfo, DeptInfo.class);
    }

    /**
     * 获取下属信息
     *
     * @param shareitId
     * @return
     */
    public List<UserBase> getSubordinate(String shareitId) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            AccessUser accessUser = itUtil.accessUserService.selectByNames(Collections.singletonList(shareitId), tenantId)
                    .stream().findFirst().orElse(null);

            AccessGroup accessGroup = itUtil.accessGroupService.selectByUserIds(Collections.singletonList(accessUser.getId()))
                    .stream().findFirst().orElse(null);
            if (accessGroup == null) {
                return new ArrayList<>();
            }

            return itUtil.accessGroupService.
                    seletByParentIds(Collections.singletonList(accessGroup.getParentId()), 1)
                    .stream().map(data -> UserBase.builder().name(data.getName()).shareId(data.getEName()).build())
                    .collect(Collectors.toList());
        }
        Map<String, String> ps = Maps.newHashMap();
        ps.put("shareId", shareitId);
        ps.put("fetchChild", "true");
        String itPersonnelInfo = getItPersonnelInfo("subordinate info", DsTaskConstant.IT_GET_SUBORDINATE_INFO_URL,
                ps, null, DsTaskConstant.POST, BaseResponseCodeEnum.IT_GET_SUBORDINATE_INFO_FAIL);
        return JSONObject.parseArray(itPersonnelInfo, UserBase.class);
    }

    /**
     * 获取用户信息
     *
     * @param shareIds
     * @return
     */
    public List<UserBase> batchGetUsersInfo(List<String> shareIds) {
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if (isPrivate) {
            return getUserInfoByName(shareIds);
        }
        JSONArray jsonArray = (JSONArray) JSONArray.toJSON(shareIds);
        String itPersonnelInfo = getItPersonnelInfo("batch user info", DsTaskConstant.IT_BATCH_GET_USER_URL,
                new HashMap<>(), jsonArray.toJSONString(), DsTaskConstant.POST, BaseResponseCodeEnum.IT_BATCH_GET_USER_INFO_FAIL);
        return JSONObject.parseArray(itPersonnelInfo, UserBase.class);
    }


    public static String getItPersonnelInfo(String name,
                                            String url,
                                            Map<String, String> params,
                                            String json,
                                            String responseType,
                                            BaseResponseCodeEnum erorMessage) {
        String token = getToken();
        Map<String, String> headrs = new HashMap(1);
        headrs.put("Authorization", "Bearer " + token);
        BaseResponse resp = null;
        switch (responseType) {
            case "get":
                resp = HttpUtil.get(MessageFormat.format(url, getHost()), params, headrs);
                break;
            case "post":
                if (StringUtils.isEmpty(json)) {
                    json = new org.json.JSONObject(params).toString();
                }
                resp = HttpUtil.postWithJson(MessageFormat.format(url, getHost()), json, headrs);
        }
        if (resp == null || resp.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject respJson = resp.get();
        if (respJson.getInteger("code") != 0) {
            log.warn("get {} failed ,resp:{}", name, JSONObject.toJSONString(respJson));
            throw new ServiceException(erorMessage);
        }
        return respJson.getString("data");
    }


    private static String getToken() {
        long nowTs = System.currentTimeMillis() / 1000;
        String itSyskey = DsTaskConstant.IT_SYSKEY;
        String itSysecret = DsTaskConstant.IT_SYSSECRET;
        String env = InfTraceContextHolder.get().getEnv();
        if (env.contains(DsTaskConstant.DEV) || env.contains(DsTaskConstant.TEST)) {
            itSyskey = DsTaskConstant.IT_SYSKEY_TEST;
            itSysecret = DsTaskConstant.IT_SYSSECRET_TEST;
        }


        // secret =  md5(timestamp+syssecret)
        String secret = DigestUtils.md5DigestAsHex((itSysecret + nowTs).getBytes());

        Map<String, String> params = new HashMap(3);
        params.put("sysKey", itSyskey);
        params.put("timestamp", Long.toString(nowTs));
        params.put("secret", secret);

        BaseResponse resp = HttpUtil.get(MessageFormat.format(DsTaskConstant.IT_GET_TOKEN_URL, getHost()), params);
        if (resp == null || resp.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }

        JSONObject respJson = resp.get();
        if (respJson.getInteger("code") != 0) {
            System.out.println(respJson.getInteger("code"));
            log.warn("get ItToken failed ,resp:" + JSONObject.toJSONString(respJson));
            throw new ServiceException(BaseResponseCodeEnum.IT_GET_TOKEN_FAIL);
        }

        log.info("getToken:" + respJson.getString("token"));
        return respJson.getString("token");
    }


    private static String getHost() {
        String env = InfTraceContextHolder.get().getEnv();
        if (env.contains(DsTaskConstant.DEV) || env.contains(DsTaskConstant.TEST)) {
            return "https://emds-test.ushareit.me";
        }

        return "https://emds.ushareit.me";
    }

    public List<UserBase> getUserInfoByName(List<String> shareIds) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        log.warn("getUserInfoByName tenantId is :" + tenantId);
        List<UserBase> res = new ArrayList<>();
        List<AccessUser> accessUsers = itUtil.accessUserService.selectByNames(shareIds, tenantId);
        List<Integer> ids = accessUsers.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<AccessGroup> accessGroupList = itUtil.accessGroupService.selectByUserIds(ids);

        Map<Integer, List<AccessGroup>> userGroupMap = accessGroupList.stream().collect(Collectors.groupingBy(AccessGroup::getUserId));

        List<Integer> groupIds = accessGroupList.stream().map(AccessGroup::getParentId).collect(Collectors.toList());
        Map<Integer, List<AccessGroup>> groupMap = itUtil.accessGroupService.listByIds(groupIds)
                .stream().collect(Collectors.groupingBy(AccessGroup::getId));

        if (userGroupMap == null) {
            return new ArrayList<>();
        }

        for (AccessUser accessUser : accessUsers) {
            UserBase conversion = UserBase.conversion(accessUser);
            List<AccessGroup> resUser = userGroupMap.get(accessUser.getId());
            if (resUser == null || resUser.isEmpty()) {
                continue;
            }
            AccessGroup accessGroup = resUser.stream().findFirst().get();
            conversion.setDepartment(groupMap.get(accessGroup.getParentId()).stream().findFirst().get().getName())
                    .setDeptFullPath(groupMap.get(accessGroup.getParentId()).stream().findFirst().get().getName());
            res.add(conversion);
        }
        return res;
    }


}