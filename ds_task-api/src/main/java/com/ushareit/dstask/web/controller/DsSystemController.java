package com.ushareit.dstask.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.UserGroupInfoVo;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.filter.AuthIdentityFilter;
import com.ushareit.dstask.mapper.AccessMenuMapper;
import com.ushareit.dstask.mapper.AccessRoleMapper;
import com.ushareit.dstask.mapper.AccessUserMapper;
import com.ushareit.dstask.mapper.UserGroupRelationMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/15
 */
@Slf4j
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/ds")
public class DsSystemController {
    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Resource
    private AccessRoleService accessRoleService;

    @Resource
    private AccessRoleMenuService accessRoleMenuService;

    @Resource
    private AccessMenuService accessMenuService;

    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private AccessGroupService accessGroupService;

    @Resource
    private AccessMenuMapper accessMenuMapper;

    @Resource
    private AccessRoleMapper accessRoleMapper;

    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    @Autowired
    private UserGroupService userGroupService;

    @Resource
    private AccessUserMapper accessUserMapper;

    @Value("${group.dbc}")
    private String dbcUserGroup;

    @Value("${group.admin}")
    private String adminGroup;

    @ApiOperation(value = "是否切换成新权限接口")
    @GetMapping("/switch/newaccess")
    public BaseResponse switchNew() {
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, DsTaskConstant.SWITCH_TO_NEW_ACCESS);
    }

    @ApiOperation(value = "校验用户对当前接口是否有权限")
    @GetMapping("/check/newaccess")
    public Boolean check(HttpServletRequest request, HttpServletResponse response,String requestPath) {
        CurrentUser currentUser = (CurrentUser)request.getAttribute(CommonConstant.CURRENT_LOGIN_USER);

        // 用户是否冻结或不存在
        // TODO 不同租户用户重名，有问题，下期修改

        AccessUser accessUser = accessUserService.selectByNameTenant(currentUser.getTenantId(), currentUser.getUserId());

        List<AccessMenu> accessMenus = accessMenuService.selectMenusByUserId(accessUser.getId());

        // 用户请求接口与其对应权限接口比对
        return accessMenus.stream().anyMatch(menu -> requestPath != null && requestPath.matches(menu.getUrl()));
    }

    @ApiOperation(value = "获取用户信息接口")
    @GetMapping("/expand/remote")
    public Map<String, Object> expand(HttpServletRequest request) {
        String authentication = InfTraceContextHolder.get().getAuthentication();
        log.debug("authentication is " + authentication);

        CurrentUser currentUser = AuthIdentityFilter.getCurrentUser(authentication);
        if (currentUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "当前用户不存在");
        }

        String groupId = request.getHeader("groupId");
        log.info("current groupId is :" + groupId);

        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        String accessUserName = currentUser.getUserId();
        log.debug("currentUser is " + currentUser);

        AccessUser accessUserBuilder = AccessUser.builder().tenantId(currentUser.getTenantId()).name(accessUserName).build();
        accessUserBuilder.setDeleteStatus(0);
        accessUserBuilder.setFreezeStatus(0);
        AccessUser accessUser = accessUserService.selectOne(accessUserBuilder);
        if (accessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "当前用户已删除或已冻结");
        }

        List<AccessGroup> accessGroupList = accessGroupService.getParentGroupList(InfTraceContextHolder.get().getTenantId(), accessUserName);
        List<Integer> collect = accessGroupList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        String groupIds = StringUtils.join(collect, ",");
        AccessTenant accessTenant = accessTenantService.checkExist(accessUser.getTenantId());
        JSONObject config = JSON.parseObject(accessTenant.getConfig());

        //改造这个关联查询
//        List<AccessRole> accessRoles = accessRoleService.selectByUserId(accessUser.getId());
        List<Integer> integers = accessUserRoleService.selectByUserId(accessUser.getId());
        List<AccessRole> accessRoles = accessRoleService.listByIds(integers);

        String roles = accessRoles.stream().map(AccessRole::getName).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
        if (StringUtils.isEmpty(roles) || roles.equals(BaseConstant.MINOR_ROLE)) {
            AccessRole accessRole = accessRoleMapper.selectByName(BaseConstant.COMMON_ROLE);
            if (accessRole == null) {
                throw new ServiceException(BaseResponseCodeEnum.ROLE_NOT_FOUND);
            }
            accessUserRoleService.addRole(accessUser.getId(), accessRole.getId().toString());
            roles = BaseConstant.COMMON_ROLE;
        }
        Boolean admin = false;
        Boolean supperAdmin = false;
        /*if (roles.contains("admin")) {
            admin = true;
            if (dataCakeSourceConfig.getSuperTenant().equals(accessTenant.getName())) {
                supperAdmin = true;
            }
        }*/
//        List<Integer> adminMenuIds=accessMenuMapper.existAdminAndSupperAdminRole(accessUser.getId());
//        if (CollectionUtils.isNotEmpty(adminMenuIds)){
//            if (adminMenuIds.contains(BaseConstant.ADMINMENUID)){
//                admin=true;
//            }
//            if (adminMenuIds.contains(BaseConstant.SUPPERADMINMENUID)){
//                supperAdmin=true;
//            }
//        }


//        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
//
//
//        List<String> dbcGroupList = null;
//        if (StringUtils.isNoneBlank(dbcUserGroup)) {
//            dbcGroupList = Arrays.asList(dbcUserGroup.split(","));
//        }
//        for (UserGroupInfoVo userGroupInfoVo:userGroupInfo){
//            if (dbcGroupList != null && dbcGroupList.contains(userGroupInfoVo.getName())){
//                userGroupInfoVo.setDbc(true);
//            }
//            List<String> ownerList = userGroupRelationMapper.selectOwner(userGroupInfoVo.getId());
//            userGroupInfoVo.setOwnerList(ownerList);
//        }
//


        Map<String, Object> map = new HashMap<>();
        map.put("id", accessUser.getId());
        map.put("userName", accessUser.getName());
        map.put("userId", accessUser.getName());
        map.put("group", currentUser.getGroup());
        map.put("email", accessUser.getEmail());
        map.put("org", currentUser.getGroupName());
        map.put("tenantId", accessTenant.getId());
        map.put("tenantName", accessTenant.getName());
        map.put("isSupperAdmin", supperAdmin);
        map.put("roles", roles);
        map.put("groupIds", groupIds);

        if (config != null && !config.isEmpty() && config.getString("login_mode").equals(DsTaskConstant.LOGIN_MODE)) {
            map.put("phone", accessUser.getPhone());
            map.put("weChatId", accessUser.getWeChatId());
            map.put("alarmChannel", JSON.parseObject(config.getString("alarm_channel"), String[].class));
            List<UserGroupInfoVo> userGroupInfo = userGroupRelationMapper.selectGroupByUseId(accessUser.getId());


            //控制admin
            boolean flag = true;
            if (StringUtils.isNotEmpty(groupId)) {
                UserGroup byId = userGroupService.getById(groupId);
                if (byId != null && byId.getDeleteStatus() == 0 && byId.getName().equals(adminGroup)) {
                    admin = true;
                    flag = false;
                }
            }

            /*if (flag) {
                if (!userGroupInfo.isEmpty()) {
                    String name = userGroupInfo.stream().findFirst().orElse(null).getName();
                    log.info("db first group is :" + name);
                    if (name.equals(adminGroup)) {
                        admin = true;
                    }
                }
            }*/

            if (CollectionUtils.isEmpty(userGroupInfo)) {
                map.put("admins", accessUserMapper.selectAdmin());
            } else {
                List<String> dbcGroupList = null;
                if (StringUtils.isNoneBlank(dbcUserGroup)) {
                    dbcGroupList = Arrays.asList(dbcUserGroup.split(","));
                }
                for (UserGroupInfoVo userGroupInfoVo:userGroupInfo){
                    if (dbcGroupList != null && dbcGroupList.contains(userGroupInfoVo.getName())){
                        userGroupInfoVo.setDbc(true);
                    }
                    List<String> ownerList = userGroupRelationMapper.selectOwner(userGroupInfoVo.getId());
                    userGroupInfoVo.setOwnerList(ownerList);
                }
            }

            map.put("userGroup", userGroupInfo);
        }

        map.put("isAdmin", admin);

        Map<String, Object> result = new HashMap<>();
        result.put("data", map);
        result.put("success", true);

        return result;
    }

    @ApiOperation(value = "获取用户所有权限菜单接口")
    @GetMapping("/menu/remote")
    public Map<String, Object> menu() {
        Map<String, Object> result = new HashMap<>(2);
        result.put("success", true);

        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        String userId = InfTraceContextHolder.get().getUserName();

        if (StringUtils.isEmpty(userId)) {
            result.put("data", "");
            return result;
        }

        InfTraceContextHolder.get().setTenantName(tenantName);
        InfTraceContextHolder.get().setTenantId(tenantId);

//        AccessUser accessUserBuilder = AccessUser.builder().tenantId(tenantId).name(userId).build();
//        accessUserBuilder.setDeleteStatus(0);
//        accessUserBuilder.setFreezeStatus(0);
//        AccessUser accessUser = accessUserService.selectOne(accessUserBuilder);
//
//        // 用户是否冻结或不存在
//        if (accessUser == null) {
//            result.put("data", "");
//            return result;
//        }
//
//        // 租户是否存在或冻结
//        AccessTenant accessTenant = accessTenantService.getById(accessUser.getTenantId());
//        if (accessTenant == null || accessTenant.getFreezeStatus() == 1 || accessTenant.getDeleteStatus() == 1) {
//            result.put("data", "");
//            return result;
//        }
//        //增加没有创建云资源不给其他页面权限设置
//        log.info("env is " + InfTraceContextHolder.get().getEnv());
//        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
//        if (isPrivate) {
//            String isHasCloudResource = DataCakeConfigUtil.getCloudResourcesService().getIsHasCloudResource(accessTenant.getId());
//            if (isHasCloudResource.equals("1")) {
//                List<String> urls = new ArrayList<>();
//                urls.add("/config/cloud");
//                urls.add("/config/import");
//                urls.add("/config");
//                result.put("data", accessMenuService.selectByUrl(urls));
//                return result;
//            }
//        }
//
//
//        // id表示ds用户id 通过用户id查找对应的角色集合
//        List<Integer> rids = accessUserRoleService.selectByUserId(accessUser.getId());
//        List<AccessRole> list = accessRoleService.listByIds(rids.stream());
//        List<AccessRole> accessRoles = list.stream().filter(role -> role.getDeleteStatus() == 0).collect(Collectors.toList());
//
//        // 用户所对应的角色是否存在
//        if (accessRoles.size() == 0) {
//            result.put("data", "");
//            return result;
//        }
//
        // 所有角色对应菜单的去重并集
//        List<Integer> menuIds = accessRoles.stream().map(role -> {
//            List<AccessRoleMenu> accessRoleMenus = accessRoleMenuService.selectByRoleId(role.getId());
//            List<Integer> ids = accessRoleMenus.stream().map(accessRoleMenu -> accessRoleMenu.getMenuId()).collect(Collectors.toList());
//            return ids;
//        }).flatMap(Collection::stream).collect(Collectors.toSet()).stream().collect(Collectors.toList());
//
//        List<AccessMenu> accessMenus = accessMenuService.listByIds(menuIds.stream()).stream().filter(menu -> menu.getDeleteStatus() == 0).collect(Collectors.toList());

        //查询是否当前用户组owner
        String user_group = InfTraceContextHolder.get().getUuid();
        UserGroup egBuild = UserGroup.builder().uuid(user_group).build();
        UserGroup userGroup= userGroupService.selectOne(egBuild);
        List<String> ownerList = userGroupRelationMapper.selectOwner(userGroup.getId());
        String owner = "";
        if (ownerList!=null && ownerList.size()>0){
            owner = ownerList.get(0);
        }

        String name = "common";
        Boolean admin = InfTraceContextHolder.get().getAdmin();
        if (admin) {
            name = "admin";
        } else if (userId.equals(owner)) {
            name = "teamowner";
        }

        AccessRole build = AccessRole.builder().name(name).build();
        build.setDeleteStatus(DeleteEntity.NOT_DELETE);
        AccessRole accessRole = accessRoleService.selectOne(build);
        List<AccessRoleMenu> accessRoleMenus = accessRoleMenuService.selectByRoleId(accessRole.getId());
        List<Integer> ids = accessRoleMenus.stream().map(accessRoleMenu -> accessRoleMenu.getMenuId()).collect(Collectors.toList());
        List<AccessMenu> accessMenus = accessMenuService.listByIds(ids.stream()).stream().filter(menu -> menu.getDeleteStatus() == 0).collect(Collectors.toList());
        result.put("data", accessMenus);
        log.info("userId:" + userId + " userGroup:"+ userGroup +" AccessRole:" + name);
        return result;
    }

    @ApiOperation(value = "OA工单数据回调")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shareid", value = "ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "systemCode", value = "系统编号", dataType = "String"),
            @ApiImplicitParam(name = "group", value = "业务组", dataType = "String")
    })
    @PostMapping("/userinfo/hook")
    public BaseResponse userInfoHook(@RequestParam("shareid") String sharedId,
                                     @RequestParam("systemCode") String systemCode,
                                     @RequestParam("group") String group,
                                     @RequestParam(value = "org[]") String[] org) {
        log.info("sharedId=" + sharedId + " ,systemCode=" + systemCode + "  ,group=" + group + " ,org=" + org);
        final String SHAREIT_CODE = "shareit";
        AccessUser accessUserFromDb = accessUserService.selectOne(new AccessUser().setName(sharedId));

        AccessTenant accessTenant = accessTenantService.getByName(SHAREIT_CODE);
        if (accessTenant == null || accessTenant.getFreezeStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "当前租户不存在");
        }
        Integer commonRoleId = accessRoleService.getCommonRoleId(accessTenant.getId()).getId();

        if (accessUserFromDb == null) {
            AccessUser accessUser = new AccessUser().setName(sharedId)
                    .setTenantName(accessTenant.getName())
                    .setEmail(sharedId + "@ushareit.com")
                    .setUserRoleIds(commonRoleId.toString())
                    .setTenantId(accessTenant.getId())
                    .setCompanyId(SHAREIT_CODE)
                    .setTenancyCode(group)
                    .setOrg(Arrays.asList(org).stream().collect(Collectors.joining(",")));
            accessUser.setCreateBy(InfTraceContextHolder.get().getUserName());
            accessUser.setUpdateBy(InfTraceContextHolder.get().getUserName());
            accessUserService.save(accessUser);

//            AccessUserRole accessUserRole = new AccessUserRole()
//                    .setUserId(accessUser.getId())
//                    .setRoleId(commonRoleId);
//            accessUserRole.setCreateBy(InfTraceContextHolder.get().getUserName());
//            accessUserRole.setUpdateBy(InfTraceContextHolder.get().getUserName());
//            accessUserRoleService.save(accessUserRole);
        } else {
            accessUserFromDb.setCompanyId(SHAREIT_CODE)
                    .setTenancyCode(group)
                    .setOrg(Arrays.asList(org).stream().collect(Collectors.joining(",")));

            accessUserService.update(accessUserFromDb);

            List<Integer> accessUserRoleFromDb = accessUserRoleService.selectByUserId(accessUserFromDb.getId());
            if (accessUserRoleFromDb == null) {
                AccessUserRole accessUserRole = new AccessUserRole()
                        .setUserId(accessUserFromDb.getId())
                        .setRoleId(commonRoleId);
                accessUserRole.setCreateBy(InfTraceContextHolder.get().getUserName());
                accessUserRole.setUpdateBy(InfTraceContextHolder.get().getUserName());
                accessUserRoleService.save(accessUserRole);
            }
        }
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

}
