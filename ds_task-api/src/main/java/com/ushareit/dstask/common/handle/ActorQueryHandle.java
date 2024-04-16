package com.ushareit.dstask.common.handle;

import com.google.api.client.util.Lists;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.RolePrivilege;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
@Slf4j
public class ActorQueryHandle extends AbstractCoundownRunnable{
    private Actor actor;
    private UserGroup userGroup;
    private LakeService lakeService;
    private List<UserGroupVo> userGroupVoList;
    private String tenantName;

    public ActorQueryHandle(Actor actor,UserGroup userGroup,LakeService lakeService,List<UserGroupVo> userGroupVoList,String tenantName){
        this.actor=actor;
        this.userGroup=userGroup;
        this.lakeService=lakeService;
        this.userGroupVoList=userGroupVoList;
        this.tenantName=tenantName;
    }

    @Override
    public void uCountDownLatch(CountDownLatch countDownLatch) {
        super.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            InfTraceContextHolder.get().setTenantName(tenantName);
            Role role=lakeService.getRole(userGroup.getUuid());
            if (role!=null){
                RolePrivilege rolePrivileges[]=role.getRolePrivileges();
                if (rolePrivileges!=null&&rolePrivileges.length>0){
                    boolean find=false;
                    List<String> privileges= Lists.newArrayList();
                    for (RolePrivilege rolePrivilege:rolePrivileges){
                        if (rolePrivilege.getName().equals(actor.getUuid())){
                            find=true;
                            privileges.add(rolePrivilege.getPrivilege());
                        }
                    }
                    if (find){
                        UserGroupVo userGroupVo=new UserGroupVo();
                        BeanUtils.copyProperties(userGroup,userGroupVo);
                        userGroupVo.setActorPrivileges(privileges);
                        userGroupVoList.add(userGroupVo);
                    }
                }
            }
        }catch (Exception e){
            log.error("",e);
        }finally {
            countDownLatch.countDown();
        }

    }
}
