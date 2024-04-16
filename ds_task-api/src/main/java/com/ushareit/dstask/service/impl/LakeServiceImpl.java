package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.UserGroupMapper;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.plugin.request.*;
import io.lakecat.catalog.common.plugin.request.input.AuthorizationInput;
import io.lakecat.catalog.common.plugin.request.input.CatalogInput;
import io.lakecat.catalog.common.plugin.request.input.ColumnChangeInput;
import io.lakecat.catalog.common.plugin.request.input.RoleInput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class LakeServiceImpl implements LakeService {

    public static final String SINGLE = "privilege_single_user_";
    public static final String SYSTEM = "SYSTEM";
    @Autowired
    private Lakecatutil lakecatutil;
    @Resource
    private UserGroupMapper userGroupMapper;

    /**
     * uuid如果为空 则取currentuser中的uuid
     * Operation.CREATE_TABLE是创建表
     * @param region
     * @param dbName
     * @param operation
     * @return
     */
    public boolean allowForDb(String region,String dbName,Operation operation,String uuid){
        GetRoleRequest getRoleRequest=new GetRoleRequest();
        if (StringUtils.isBlank(uuid)){
            uuid=InfTraceContextHolder.get().getUuid();
        }
        getRoleRequest.setRoleName(uuid);
        LakeCatClient lakeCatClient=lakecatutil.getClient();
        getRoleRequest.setProjectId(lakeCatClient.getProjectId());
        Role role=lakeCatClient.getRole(getRoleRequest);
        RolePrivilege[] rolePrivileges=role.getRolePrivileges();
        boolean allow=false;
        if (rolePrivileges!=null&&rolePrivileges.length>0){
            for (RolePrivilege rolePrivilege:rolePrivileges){
                if (rolePrivilege.getPrivilege().equalsIgnoreCase(operation.getPrintName())&&rolePrivilege.getName().equals(region+"."+dbName)){
                    allow=true;
                }
            }
        }
        return allow;
    }

    public void deletePrivilegeForRole(String objectName, String roleName, String objectType, Operation operation) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            //lakeCatClient.revokePrivilegeFromRole();
            AlterRoleRequest request = new AlterRoleRequest();
            request.setProjectId(lakeCatClient.getProjectId());
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectType(objectType);
            roleInput.setObjectName(objectName);
            roleInput.setOperation(operation);
            roleInput.setRoleName(roleName);
            request.setInput(roleInput);
            lakeCatClient.revokePrivilegeFromRole(request);
        } catch (Exception e) {
            log.error("", e);
        }

    }

    @Override
    public Role getRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            GetRoleRequest getRoleRequest=new GetRoleRequest();
            getRoleRequest.setProjectId(lakeCatClient.getProjectId());
            getRoleRequest.setRoleName(roleName);
            return lakeCatClient.getRole(getRoleRequest);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public void grantPrivilegeToRole(String objectName, String roleName, String objectType, Operation operation) {
        try {
            String comment="operUser="+InfTraceContextHolder.get().getUserName()+",applyUser="+InfTraceContextHolder.get().getUserName();
            try {
                String userGroupName=userGroupMapper.selectUserGroupByUuid(roleName).getName();
                if (StringUtils.isNoneBlank(userGroupName)){
                    comment=comment+",userGroup="+userGroupName;
                }
            }catch (Exception e){
                log.error("",e);
            }
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            //lakeCatClient.revokePrivilegeFromRole();
            AlterRoleRequest request = new AlterRoleRequest();
            request.setProjectId(lakeCatClient.getProjectId());
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectType(objectType);
            roleInput.setObjectName(objectName);
            roleInput.setOperation(operation);
            roleInput.setRoleName(roleName);
            roleInput.setComment(comment);
            request.setInput(roleInput);
            lakeCatClient.grantPrivilegeToRole(request);
        } catch (Exception e) {
            log.error("", e);
        }

    }

    public void addUserToRole(String user, String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            AlterRoleRequest request = new AlterRoleRequest();
            RoleInput roleInput = new RoleInput();
            roleInput.setUserId(new String[]{user});
            roleInput.setRoleName(roleName);
            request.setInput(roleInput);
            request.setProjectId(lakeCatClient.getProjectId());
            request.setRoleName(roleName);
            lakeCatClient.grantRoleToUser(request);
        } catch (Exception e) {
            log.error("");
        }
    }

    @Override
    public void removeUser(String user, String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            AlterRoleRequest request = new AlterRoleRequest();
            request.setProjectId(lakeCatClient.getProjectId());
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectName("");
            roleInput.setObjectType("");
            roleInput.setOperation(Operation.ALTER_ROLE);
            roleInput.setOwnerUser("");
            roleInput.setRoleName(roleName);
            roleInput.setUserId(new String[]{user});
            request.setInput(roleInput);
            lakeCatClient.revokeRoleFromUser(request);
        } catch (Exception e) {
            log.error("");
        }
    }

    @Override
    public void dropRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            DropRoleRequest request = new DropRoleRequest();
            request.setProjectId(lakeCatClient.getProjectId());
            request.setRoleName(roleName);
            lakeCatClient.dropRole(request);
        } catch (Exception e) {
            log.error("");
        }
    }

    public void createRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            CreateRoleRequest requestCreate = new CreateRoleRequest();
            requestCreate.setProjectId(lakeCatClient.getProjectId());
            RoleInput roleInputCreate = new RoleInput();
            roleInputCreate.setOwnerUser(SYSTEM);
            roleInputCreate.setRoleName(roleName);
            requestCreate.setInput(roleInputCreate);
            lakeCatClient.createRole(requestCreate);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public boolean existRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            GetRoleRequest request = new GetRoleRequest();
            request.setRoleName(roleName);
            request.setProjectId(lakeCatClient.getProjectId());
            Role role = lakeCatClient.getRole(request);
            if (role != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public void addActor(Actor actor) {
        try {
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            CreateCatalogRequest createCatalogRequest = new CreateCatalogRequest();
            createCatalogRequest.setProjectId(lakeCatClient.getProjectId());
            CatalogInput catalogInput = new CatalogInput();
            catalogInput.setCatalogName(actor.getUuid());
            catalogInput.setDescription(actor.getRegion());
            catalogInput.setOwnerType("USER");
            catalogInput.setOwner(actor.getCreateBy());
            createCatalogRequest.setInput(catalogInput);
            lakeCatClient.createCatalog(createCatalogRequest);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.LAKECLIENT_ADD_SOURCE.name(), "lake增加数据源失败");
        }
    }

    public boolean doAuth(Operation operation, String catalog, String objectName) {
        LakeCatClient lakeCatClient = lakecatutil.getClient();
        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(operation);
        User user = new User();
        user.setUserId(InfTraceContextHolder.get().getUserName());
        authorizationInput.setUser(user);

        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(lakeCatClient.getProjectId());
        catalogObject.setCatalogName(catalog);
        catalogObject.setObjectName(objectName);
        authorizationInput.setCatalogInnerObject(catalogObject);
        AuthorizationResponse authenticate = lakeCatClient.authenticate(new AuthenticationRequest(lakeCatClient.getProjectId(), false, com.google.common.collect.Lists.newArrayList(authorizationInput)));
        return authenticate.getAllowed();
    }

    public void alterColumn(){
        LakeCatClient lakeCatClient = lakecatutil.getClient();
        AlterColumnRequest alterColumnRequest=new AlterColumnRequest();
        alterColumnRequest.setCatalogName(ObjectType.TABLE.name());
        alterColumnRequest.setDatabaseName("");
        alterColumnRequest.setTableName("0");
        alterColumnRequest.setProjectId("");
        ColumnChangeInput columnChangeInput=new ColumnChangeInput();
        columnChangeInput.setChangeType(Operation.CHANGE_COLUMN);
        Column column=new Column();
        //column.set
       // columnChangeInput.setChangeColumnMap();
        //columnChangeInput.setColumnList();

        alterColumnRequest.setInput(columnChangeInput);
        lakeCatClient.alterColumn(alterColumnRequest);
    }

    public boolean doAuthByGroup(String roleName,String region,String dbName,String tableName,String privilege){
        Role role=getRole(roleName);
        if (role.getRolePrivileges()!=null&&role.getRolePrivileges().length>0){
            for (RolePrivilege rolePrivilege:role.getRolePrivileges()){
                if (CommonConstant.CREATE_TABLE.equals(privilege)){
                    if ("DATABASE".equals(rolePrivilege.getGrantedOn())){
                        if (rolePrivilege.getName().equalsIgnoreCase(region+"."+dbName)&&Operation.CREATE_TABLE.getPrintName().equals(rolePrivilege.getPrivilege())){
                            return true;
                        }
                    }
                }else {
                    if ("TABLE".equals(rolePrivilege.getGrantedOn())){
                        if (rolePrivilege.getName().equalsIgnoreCase(region+"."+dbName+"."+"*")){
                            return true;
                        }
                        if (rolePrivilege.getName().equalsIgnoreCase(region+"."+dbName+"."+tableName)){
                            if (CommonConstant.INSERT_TABLE.equals(privilege)){
                                if (Operation.INSERT_TABLE.getPrintName().equals(rolePrivilege.getPrivilege())){
                                    return true;
                                }
                            }else if (CommonConstant.SELECT_TABLE.equalsIgnoreCase(privilege)){
                                if (Operation.SELECT_TABLE.getPrintName().equals(rolePrivilege.getPrivilege())){
                                    return true;
                                }
                            }
                        }
                    }

                }
            }

        }
        return false;
    }
}
