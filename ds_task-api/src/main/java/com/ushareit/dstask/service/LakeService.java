package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Actor;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.Role;

public interface LakeService {
    boolean doAuthByGroup(String roleName,String region,String dbName,String tableName,String privilege);
    void grantPrivilegeToRole(String objectName, String roleName, String objectType, Operation operation);
    void createRole(String roleName);
    void addUserToRole(String user,String roleName);
    void removeUser(String user,String roleName);
    void dropRole(String roleName);
    boolean existRole(String roleName);
    void addActor(Actor actor);
    boolean doAuth(Operation operation, String catalog, String objectName);
    void deletePrivilegeForRole(String objectName,String roleName,String objectType,Operation operation);
    Role getRole(String roleName);
    boolean allowForDb(String region,String dbName,Operation operation,String uuid);
}
