package com.ushareit.dstask.common.handle;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.vo.ActorUserGroupVo;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.RolePrivilege;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ActorSavePrivilegeHandle extends AbstractCoundownRunnable{

    private Actor actor;
    private UserGroup userGroup;
    private LakeService lakeService;
    private ActorUserGroupVo actorUserGroupVo;
    private String tenantName;

    public ActorSavePrivilegeHandle(Actor actor,UserGroup userGroup,LakeService lakeService,ActorUserGroupVo actorUserGroupVo,String tenantName){
        this.actor=actor;
        this.userGroup=userGroup;
        this.lakeService=lakeService;
        this.actorUserGroupVo=actorUserGroupVo;
        this.tenantName=tenantName;
    }

    @Override
    public void uCountDownLatch(CountDownLatch countDownLatch) {
        super.countDownLatch=countDownLatch;
    }

    @Override
    public void run() {
        try {
            InfTraceContextHolder.get().setTenantName(tenantName);
            Role role=lakeService.getRole(userGroup.getUuid());
            if (role!=null){
                RolePrivilege rolePrivileges[]=role.getRolePrivileges();
                for (RolePrivilege rolePrivilege:rolePrivileges){
                    if (rolePrivilege.getName().equals(actor.getUuid())){
                        lakeService.deletePrivilegeForRole(actor.getUuid(),userGroup.getUuid(), ObjectType.CATALOG.name(),getOperation(rolePrivilege.getPrivilege()));
                    }
                }
            }
            boolean find=false;
            UserGroupVo u=null;
            if (CollectionUtils.isNotEmpty(actorUserGroupVo.getUserGroupVoList())){
                for (UserGroupVo userGroupVo: actorUserGroupVo.getUserGroupVoList()){
                    if (userGroup.getId().intValue()==userGroupVo.getId().intValue()){
                        find=true;
                        u=userGroupVo;
                        break;
                    }
                }
            }
            if (find){
                if (CollectionUtils.isNotEmpty(u.getActorPrivileges())){
                    for (String s:u.getActorPrivileges()){
                        lakeService.grantPrivilegeToRole(actor.getUuid(),userGroup.getUuid(),ObjectType.CATALOG.name(),getOperation(s));
                    }
                }
            }
        }catch (Exception e){

        }finally {
            countDownLatch.countDown();
        }

    }
    private Operation getOperation(String name){
        for (Operation operation:Operation.values()){
            if (operation.getPrintName().equals(name)){
                return operation;
            }
        }
        return null;
    }
}
