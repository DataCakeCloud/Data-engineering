package com.ushareit.dstask.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.bean.Catalog;
import com.ushareit.dstask.common.handle.ActorQueryHandle;
import com.ushareit.dstask.common.handle.ActorSavePrivilegeHandle;
import com.ushareit.dstask.common.vo.ActorUserGroupVo;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.*;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.service.ThreadService;
import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;
import com.ushareit.dstask.third.airbyte.json.JsonSchemaValidator;
import com.ushareit.dstask.third.airbyte.json.JsonValidationException;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.IdUtils;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.plugin.request.AuthenticationRequest;
import io.lakecat.catalog.common.plugin.request.input.AuthorizationInput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Slf4j
@Service
public class ActorServiceImpl extends AbstractBaseServiceImpl<Actor> implements ActorService, CommandLineRunner {

    private final JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
    @Autowired
    private AccessGroupMapper accessGroupMapper;
    @Autowired
    private ActorMapper actorMapper;
    @Autowired
    private ActorDefinitionMapper actorDefinitionMapper;

    @Resource
    private ActorShareMapper actorShareMapper;

    @Autowired
    private AccessGroupService accessGroupService;

    @Override
    public CrudMapper<Actor> getBaseMapper() {
        return actorMapper;
    }

    @Autowired
    private AccessUserMapper accessUserMapper;

    @Autowired
    private LakeService lakeService;

    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private ThreadService threadService;

    @Resource
    public TaskMapper taskMapper;


    private String shareitQe = "{\"ch_aws-ds-test\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"caowei\",\"dongzhe1\",\"chenchen\",\"wangsh\",\"wanghy1\",\"mingqs\",\"luocheng\",\"xiaoning\",\"yangmy\",\"qiqi\",\"yuesj\",\"tangyh\",\"chenlei\",\"lids\",\"fengtt\",\"wangll\",\"wangyn1\",\"xuecj\",\"zhangxl\",\"wangzg\",\"kangkang\",\"nanchao\",\"liuyulong\",\"zhaoqingfa\",\"wangtaiyang\",\"chucf\",\"yangping\",\"renhantao\",\"niuqingwen\",\"niuqianxiong\",\"peizx\",\"zhousq\",\"huanglu\",\"yanhui\",\"xiln\",\"fangjj1\",\"fengxiao\",\"linyang\",\"chenfeihang\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"mysql_query-editor\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"zhangtao\",\"tangyh\",\"wangll\",\"xuecj\",\"licg\",\"wangsy1\",\"fengxiao\",\"xiari\",\"wuyan\",\"linyang\",\"chenfeihang\",\"sunlongjiang\",\"renjianxu\",\"sangxg\",\"tangjk\",\"shilidong\",\"tianxu\",\"zhangkangwei\",\"zhangfeng\",\"dongcx\",\"huyx\",\"hongyg\",\"hanzenggui\",\"zhangshaoquan\",\"zhengxiao\",\"wangxin\",\"liweimin\"],\"mysql_shareit-mam\":[\"zhangpeng1\",\"wangyanli\",\"wangll\",\"xuecj\",\"nanchao\",\"fengxiao\",\"wangyiren\",\"chudebiao\",\"wangxc\",\"wangxw\",\"yangw\",\"xiari\",\"wuyan\",\"linyang\",\"chenfeihang\",\"sunlongjiang\",\"shilidong\",\"tianxu\",\"dongcx\",\"huyx\",\"zhangshaoquan\",\"wangxin\"],\"tidb_bigdata-metrics\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"tangyh\",\"wangll\",\"xuecj\",\"zhaoqingfa\",\"wangsy1\",\"liangyz\",\"fengxiao\",\"wuxuanxu\",\"zhaoxiaoqi\",\"renjianxu\",\"tangjk\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"mysql_olap-gateway\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"zhangtao\",\"wangll\",\"xuecj\",\"licg\",\"fengxiao\",\"xiari\",\"linyang\",\"chenfeihang\",\"sunlongjiang\",\"renjianxu\",\"tangjk\",\"shilidong\",\"tianxu\",\"dongcx\",\"huyx\",\"hongyg\",\"hanzenggui\",\"caihuan\",\"zhangshaoquan\",\"zhengxiao\",\"wangxin\"],\"mysql_gocs\":[\"zhangpeng1\",\"tangyh\",\"wangjy1\",\"wangll\",\"wangzg\",\"zhaoqingfa\",\"yuanhb\",\"yangping\",\"liangyz\",\"xiari\",\"sunlongjiang\",\"huyx\",\"hanzenggui\",\"caihuan\",\"xiangshuai\",\"wangzj2\",\"zhangshaoquan\",\"wangxin\"],\"mysql_query-gateway\":[\"licg\",\"linyang\",\"renjianxu\",\"zhangshaoquan\",\"songmingyuan\"],\"mysql_huawei\":[\"zhangpeng1\",\"tangyh\",\"zhaoqingfa\",\"liangyz\",\"tangjk\",\"hanzenggui\",\"zhangshaoquan\",\"wangxin\"],\"ch_apm\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"wangsh\",\"zangwt\",\"mingqs\",\"zhangtao\",\"luowj\",\"hurh\",\"renhx\",\"yuesj\",\"guohy\",\"tangyh\",\"chenlei\",\"zhanglq\",\"wangll\",\"xuecj\",\"zhangyt\",\"xuzz\",\"changjun\",\"liuyulong\",\"fengpc\",\"licg\",\"fenggy\",\"xuwl\",\"peizx\",\"fengxiao\",\"linyang\",\"chenfeihang\",\"guanweilin\",\"sangxg\",\"tangjk\",\"shilidong\",\"tianxu\",\"liuzhao\",\"zhangshaoquan\",\"wangxin\",\"zhouhancheng\"],\"mysql_databend-test\":[\"zhangpeng1\",\"zhuzhe\",\"wangll\",\"wangsy1\",\"xiari\",\"tangjk\",\"tianxu\",\"huyx\",\"zhangshaoquan\",\"zhengxiao\",\"wangxin\"],\"mysql_cost-center\":[\"zhangpeng1\",\"zhuzhe\",\"wangll\",\"zhaoqingfa\",\"liangyz\",\"xiari\",\"sunlongjiang\",\"tangjk\",\"shilidong\",\"zhangkangwei\",\"zhangshaoquan\"],\"mysql_ds-task\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"zhangtao\",\"wangll\",\"xuecj\",\"zhaoqingfa\",\"licg\",\"wangsy1\",\"ext.zhangfeng\",\"liangyz\",\"fengxiao\",\"xiari\",\"wuyan\",\"linyang\",\"zhaoxiaoqi\",\"chenfeihang\",\"sunlongjiang\",\"renjianxu\",\"xuebotao\",\"wuhaorui\",\"tangjk\",\"shilidong\",\"tianxu\",\"luhongyu\",\"zhangfeng\",\"dongcx\",\"huyx\",\"hongyg\",\"hanzenggui\",\"zhangshaoquan\",\"zhengxiao\",\"wangxin\"],\"mysql_query-bi\":[\"wangll\",\"xiari\",\"linyang\",\"renjianxu\",\"tangjk\",\"luhongyu\"],\"ch_hw-sbos\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"wangsh\",\"zhangrg\",\"hurh\",\"tangyh\",\"wangll\",\"xuecj\",\"kuangcz\",\"yanglichuan\",\"peizx\",\"fengxiao\",\"linyang\",\"chenfeihang\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"ch_rcs-prod\":[\"zhangpeng1\",\"wangsh\",\"zhangrg\",\"hurh\",\"wangll\",\"fengxiao\",\"linyang\",\"chenfeihang\",\"zhouyayong\",\"lvtao\",\"yuyankang\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"mysql_sla-service\":[\"zhangpeng1\",\"wangll\",\"fengxiao\",\"xiari\",\"zhouyayong\",\"lvtao\",\"yuyankang\",\"wukai\",\"shilidong\",\"tianxu\",\"huyx\",\"zhangshaoquan\",\"wangxin\"],\"mysql_ds-pipeline\":[\"zhangpeng1\",\"zhuzhe\",\"zhaoqingfa\",\"licg\",\"xiari\",\"xuebotao\",\"wuhaorui\",\"shilidong\",\"luhongyu\",\"zhangfeng\",\"dongcx\",\"huyx\",\"hanzenggui\",\"zhangshaoquan\",\"wangxin\"],\"mysql_ds-pipeline-readonly\":[\"zhuzhe\",\"wangll\",\"licg\",\"xiari\",\"renjianxu\",\"xuebotao\",\"wuhaorui\",\"tianxu\",\"luhongyu\",\"zhangfeng\",\"zhangshaoquan\",\"songmingyuan\"],\"mysql_sharesql\":[\"zhangpeng1\",\"wangyanli\",\"tangyh\",\"wangll\",\"wangsy1\",\"xiari\",\"shilidong\",\"zhangkangwei\",\"huyx\",\"zhangshaoquan\",\"wangxin\"],\"mysql_ds_task_readonly\":[\"zhuzhe\",\"wangll\",\"licg\",\"liangyz\",\"xiari\",\"renjianxu\",\"xuebotao\",\"wuhaorui\",\"tianxu\",\"luhongyu\",\"zhangfeng\",\"liweimin\"],\"mysql_lakecat-web\":[\"zhangpeng1\",\"tangyh\",\"wangll\",\"zhaoqingfa\",\"wangsy1\",\"xiari\",\"linyang\",\"sunlongjiang\",\"huyx\",\"hongyg\",\"hanzenggui\",\"zhangshaoquan\",\"wangxin\"],\"mysql_cloud-test\":[\"zhangpeng1\",\"guohy\",\"wangll\",\"xiari\",\"shilidong\",\"tianxu\",\"huyx\",\"liuzhao\",\"zhangshaoquan\",\"wangxin\"],\"mysql_datastudio-pipeline\":[\"zhangpeng1\",\"tangjk\",\"luhongyu\",\"zhangfeng\",\"zhangfeng\",\"zhangshaoquan\"],\"mysql_ds_task\":[\"wuhaorui\"],\"mysql_api-gateway\":[\"zhangpeng1\",\"zhuzhe\",\"wangll\",\"licg\",\"fengxiao\",\"xiari\",\"linyang\",\"sunlongjiang\",\"xuebotao\",\"wuhaorui\",\"tangjk\",\"shilidong\",\"tianxu\",\"luhongyu\",\"zhangfeng\",\"hanzenggui\",\"zhangshaoquan\",\"yangyong\"],\"mysql_gov-payment\":[\"zhangpeng1\",\"wangll\",\"zhaoqingfa\",\"xiari\",\"linyang\",\"sunlongjiang\",\"tangjk\",\"hanzenggui\"],\"ch_hermes-aws-ue1\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"wangsh\",\"tangyh\",\"wangjy1\",\"wangll\",\"xuecj\",\"wangzg\",\"yuanhb\",\"yangping\",\"peizx\",\"fengxiao\",\"chentongwei\",\"linyang\",\"chenfeihang\",\"tangjk\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"mysql_trino-history\":[\"zhangpeng1\",\"tangyh\",\"wangll\",\"xiari\",\"tangjk\",\"shilidong\",\"zhangkangwei\",\"hongyg\",\"zhangshaoquan\",\"liweimin\"],\"mysql_gov-shareit\":[\"zhangpeng1\",\"hurh\",\"wangll\",\"zhaoqingfa\",\"xiari\",\"linyang\",\"sunlongjiang\",\"tangjk\",\"hanzenggui\"],\"mysql_test-datastudio\":[\"zhangfeng\"],\"mysql_tableau-business\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"zhangtao\",\"hurh\",\"tangyh\",\"wangjy1\",\"wangll\",\"xuecj\",\"wangzg\",\"zhaoqingfa\",\"licg\",\"peizx\",\"huanglu\",\"wangsy1\",\"ext.zhangfeng\",\"liangyz\",\"fengxiao\",\"xiari\",\"wuxuanxu\",\"linyang\",\"zhaopan\",\"zhaoxiaoqi\",\"chenfeihang\",\"wanglw\",\"qijuhong\",\"sunlongjiang\",\"renjianxu\",\"sangxg\",\"xuebotao\",\"tangjk\",\"shilidong\",\"tianxu\",\"lianghb\",\"zhangfeng\",\"zhanshulin\",\"dongcx\",\"huyx\",\"hanzenggui\",\"zhangshaoquan\",\"zhengxiao\",\"wangxin\",\"xiaobanglong\",\"liulu\"],\"ch_aws-do-dwd-ch\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"caowei\",\"dongzhe1\",\"chenchen\",\"wangsh\",\"wanghy1\",\"mingqs\",\"luocheng\",\"xiaoning\",\"yangmy\",\"qiqi\",\"yuesj\",\"tangyh\",\"chenlei\",\"lids\",\"fengtt\",\"wangll\",\"wangyn1\",\"xuecj\",\"zhangxl\",\"wangzg\",\"kangkang\",\"nanchao\",\"liuyulong\",\"zhaoqingfa\",\"wangtaiyang\",\"chucf\",\"yangping\",\"renhantao\",\"niuqingwen\",\"niuqianxiong\",\"peizx\",\"zhousq\",\"huanglu\",\"yanhui\",\"xiln\",\"fangjj1\",\"fengxiao\",\"zhoushiqi\",\"hanxiaoqing\",\"linyang\",\"chenfeihang\",\"wuhaorui\",\"shilidong\",\"tianxu\",\"luhongyu\",\"zhangfeng\",\"zhangshaoquan\",\"wangxin\"],\"ch_fintech\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"wangsh\",\"tangyh\",\"wangll\",\"xuecj\",\"kangkang\",\"nanchao\",\"niuqianxiong\",\"peizx\",\"fengxiao\",\"linyang\",\"chenfeihang\",\"sangxg\",\"shilidong\",\"tianxu\",\"zhangshaoquan\",\"wangxin\"],\"mysql_gov-center\":[\"zhangpeng1\",\"wangll\",\"liangyz\",\"xiari\",\"tangjk\",\"zhangshaoquan\",\"liulu\"],\"tidb_apm\":[\"zhangpeng1\",\"zhuzhe\",\"wangyanli\",\"wangqq\",\"zangwt\",\"zhangtao\",\"luowj\",\"renhx\",\"yuesj\",\"guohy\",\"tangyh\",\"chenlei\",\"zhanglq\",\"wangll\",\"xuecj\",\"zhangyt\",\"xuzz\",\"changjun\",\"liuyulong\",\"fengpc\",\"licg\",\"fenggy\",\"xuwl\",\"peizx\",\"fengxiao\",\"linyang\",\"tangjk\",\"shilidong\",\"tianxu\",\"liuzhao\",\"zhangshaoquan\",\"wangxin\"],\"ch_aws-analyst-ch\":[\"zhangpeng1\",\"wangsh\",\"wangll\",\"tangjk\",\"zhangshaoquan\"],\"mysql_query-editor-ro\":[\"wangll\",\"licg\",\"xiari\",\"linyang\",\"shilidong\",\"zhangshaoquan\"]}";
    private String paymentQe = "{\"mysql_ds-pipeline-readonly\":[\"xiari\"],\"mysql_query-editor\":[\"xiari\"],\"mysql_sharesql\":[\"xiari\"],\"mysql_ds_task_readonly\":[\"xiari\"],\"mysql_lakecat-web\":[\"xiari\"],\"mysql_shareit-mam\":[\"xiari\"],\"mysql_cloud-test\":[\"xiari\"],\"mysql_olap-gateway\":[\"caihuan\",\"xiari\"],\"mysql_gov-payment\":[\"xiari\"],\"mysql_api-gateway\":[\"xiari\"],\"mysql_gocs\":[\"xiari\"],\"mysql_databend-test\":[\"xiari\"],\"mysql_trino-history\":[\"xiari\"],\"mysql_cost-center\":[\"xiari\"],\"mysql_gov-shareit\":[\"xiari\"],\"mysql_ds-task\":[\"xiari\"],\"mysql_tableau-business\":[\"xiari\"],\"mysql_gov-center\":[\"xiari\"],\"mysql_sla-service\":[\"xiari\"],\"mysql_query-editor-ro\":[\"tangjk\",\"caihuan\",\"xiari\"]}";

    @Override
    public Object save(Actor actor) {
        actor.setUuid(IdUtils.getLenthIdForActor());
        actor.setCreateUserGroupUuid(InfTraceContextHolder.get().getUuid());
        validateSchema(actor.getActorDefinitionId(), Jsons.deserialize(actor.getConfiguration()));
        Example example = new Example(Actor.class);
        example.or()
                .andEqualTo("name", actor.getName())
                .andEqualTo("actorType", actor.getActorType())
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        if (checkExist(null, actor.getName(), actor.getActorType())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源名字已存在");
        }
        lakeService.addActor(actor);
        lakeService.grantPrivilegeToRole(actor.getUuid(),InfTraceContextHolder.get().getUuid(),ObjectType.CATALOG.name(),Operation.DROP_CATALOG);
        lakeService.grantPrivilegeToRole(actor.getUuid(),InfTraceContextHolder.get().getUuid(),ObjectType.CATALOG.name(),Operation.ALTER_CATALOG);
        lakeService.grantPrivilegeToRole(actor.getUuid(),InfTraceContextHolder.get().getUuid(),ObjectType.CATALOG.name(),Operation.DESC_CATALOG);
        Object o = super.save(actor);
        return o;
    }

    @Override
    public Actor getById(Object id) {
        Actor actor = getBaseMapper().selectByPrimaryKey(id);
        String uuid = InfTraceContextHolder.get().getUuid();
        Boolean admin = InfTraceContextHolder.get().getAdmin();
        if (actor.getId() != 2) {
            if (!admin && StringUtils.isNotEmpty(actor.getCreateUserGroupUuid()) && !actor.getCreateUserGroupUuid().equals(uuid)
                    && !uuid.equals("INNER_SCHEDULE")) {
                throw new ServiceException(BaseResponseCodeEnum.SELECT_FAIL);
            }
        }
        return actor;
    }


    @Override
    public void update(Actor actor) {
        Actor originActor = getBaseMapper().selectByPrimaryKey(actor.getId());
        String uuid = InfTraceContextHolder.get().getUuid();
        Boolean admin = InfTraceContextHolder.get().getAdmin();
        if (!admin && StringUtils.isNotEmpty(originActor.getCreateUserGroupUuid()) && !originActor.getCreateUserGroupUuid().equals(uuid)) {
            throw new ServiceException(BaseResponseCodeEnum.UPDATE_FAIL);
        }

        if (actor.getConfiguration() != null) {
            validateSchema(originActor.getActorDefinitionId(), Jsons.deserialize(actor.getConfiguration()));
        }

        if (StringUtils.isNotBlank(actor.getName()) && checkExist(actor.getId(), actor.getName(), originActor.getActorType())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源名字已存在");
        }
        super.update(actor);
    }

    @Override
    public boolean checkExist(Integer actorId, String name, String actorType) {
        Example example = new Example(Actor.class);
        Example.Criteria criteria = example.or()
                .andEqualTo("name", name)
                .andEqualTo("actorType", actorType)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        if (actorId != null) {
            criteria.andNotEqualTo("id", actorId);
        }

        return CollectionUtils.isNotEmpty(getBaseMapper().selectByExample(example));
    }

    @Override
    public List<Actor> selectActorByDatabase(String database) {
        List<Actor> actors = actorMapper.selectActorByDatabase(database);
        return actors;
    }

    @Override
    public void addActorShare(ActorShare actorShare) {
        Actor actor = actorMapper.selectByPrimaryKey(actorShare.getActorId());
        Example example = new Example(ActorShare.class);
        Example.Criteria criteria = example.and();
        criteria.andEqualTo("actorId", actorShare.getActorId());
        List<ActorShare> actorShares = actorShareMapper.selectByExample(example);
        if (StringUtils.isNoneBlank(actorShare.getShareId())) {
            for (String shareId : actorShare.getShareId().split(",")) {
                ActorShare a = new ActorShare();
                BeanUtils.copyProperties(actorShare, a);
                a.setShareId(shareId);
                boolean exists = false;
                if (CollectionUtils.isNotEmpty(actorShares)) {
                    for (ActorShare ac : actorShares) {
                        if (ac.getShareId().equals(shareId) && ac.getType() == actorShare.getType()) {
                            exists = true;
                        }
                    }
                }
                if (exists) {
                    continue;
                }
                if (actorShare.getType() == 2) {
                    AccessGroup accessGroup = accessGroupMapper.selectByPrimaryKey(a.getShareId());
                    if (accessGroup != null) {
                        a.setName(accessGroup.getName());
                    }
                }
                actorShareMapper.insert(a);
                addSourceDesc(actor, actorShare, shareId);
            }
        }
    }

 /*   @Override
    public boolean doAuthEdit(Integer id) {
        Actor originActor = getBaseMapper().selectByPrimaryKey(id);
        return lakeService.doAuth(Operation.ALTER_CATALOG, ObjectType.CATALOG.name(), originActor.getUuid());
    }*/
    @Override
    public Map<Integer,Map<String,Boolean>> doAuthEdit(String id) {
        Map<Integer,Map<String,Boolean>> totalMap=Maps.newHashMap();
        if (StringUtils.isEmpty(id)) {
            return totalMap;
        }
        Role role=lakeService.getRole(InfTraceContextHolder.get().getUuid());
        RolePrivilege[] rolePrivileges=role.getRolePrivileges();
        //Set<String> set=Sets.newSet();
        Map<String,List<String>> mapPrivilege=Maps.newHashMap();
        if (rolePrivileges!=null&&rolePrivileges.length>0){
            for (RolePrivilege rolePrivilege:rolePrivileges){
                if (ObjectType.CATALOG.name().equalsIgnoreCase(rolePrivilege.getGrantedOn())&&rolePrivilege.getPrivilege().indexOf(ObjectType.CATALOG.name())>-1){
                    if (mapPrivilege.containsKey(rolePrivilege.getName())){
                        mapPrivilege.get(rolePrivilege.getName()).add(rolePrivilege.getPrivilege());
                    }else {
                        mapPrivilege.put(rolePrivilege.getName(),Lists.newArrayList(rolePrivilege.getPrivilege()));
                    }
                    //set.add(rolePrivilege.getName());
                }
            }
        }
        List<Actor> actorList=getBaseMapper().selectByIds(id);
        if (CollectionUtils.isNotEmpty(actorList)){
            for (Actor originActor:actorList){
                Map<String,Boolean> map=Maps.newHashMap();
                if (StringUtils.isNoneBlank(originActor.getUuid())&&mapPrivilege.containsKey(originActor.getUuid())){
                  if (mapPrivilege.get(originActor.getUuid()).contains(Operation.ALTER_CATALOG.getPrintName())){
                      map.put("edit",true);
                  }else {
                      map.put("edit",false);
                  }
                }
                if (StringUtils.isNoneBlank(originActor.getUuid())&&mapPrivilege.containsKey(originActor.getUuid())){
                    if (mapPrivilege.get(originActor.getUuid()).contains(Operation.DROP_CATALOG.getPrintName())){
                        map.put("delete",true);
                    }else {
                        map.put("delete",false);
                    }
                }
                totalMap.put(originActor.getId(),map);
            }
        }
        return totalMap;
    }


    private void addSourceDesc(Actor actor, ActorShare actorShare, String shareId) {
        List<String> users = Lists.newArrayList();
        if (actorShare.getType() == 2) {
            Set<String> userIds = accessGroupService.listUserIdsByGroupIds(Lists.newArrayList(Integer.valueOf(shareId)));
            if (CollectionUtils.isNotEmpty(userIds)) {
                users = Lists.newArrayList(userIds);
            }
        } else {
            users.add(shareId);
        }
        if (CollectionUtils.isNotEmpty(users)) {
            for (String s : users) {
                String roleName = LakeServiceImpl.SINGLE + s;
                if (!lakeService.existRole(roleName)) {
                    lakeService.createRole(roleName);
                    lakeService.addUserToRole(s, roleName);
                }
                lakeService.grantPrivilegeToRole(actor.getUuid(), roleName, ObjectType.CATALOG.name(), Operation.DESC_CATALOG);
            }
        }

    }

    @Override
    public void deleteActorShare(Integer id) {
        ActorShare actorShare = actorShareMapper.selectByPrimaryKey(id);
        Actor actor = actorMapper.selectByPrimaryKey(actorShare.getActorId());
        deleteSourceDesc(actor, actorShare, actorShare.getShareId());
        actorShareMapper.deleteByPrimaryKey(id);
    }

    private void deleteSourceDesc(Actor actor, ActorShare actorShare, String shareId) {
        List<String> users = Lists.newArrayList();
        if (actorShare.getType() == 2) {
            Set<String> userIds = accessGroupService.listUserIdsByGroupIds(Lists.newArrayList(Integer.valueOf(shareId)));
            if (CollectionUtils.isNotEmpty(userIds)) {
                users = Lists.newArrayList(userIds);
            }
        } else {
            users.add(shareId);
        }
        if (CollectionUtils.isNotEmpty(users)) {
            for (String s : users) {
                lakeService.deletePrivilegeForRole(actor.getUuid(), LakeServiceImpl.SINGLE + s, ObjectType.CATALOG.name(), Operation.DESC_CATALOG);
            }
        }

    }


    @Override
    public List<ActorShare> listActorShare(Integer actorId) {
        Example example = new Example(ActorShare.class);
        Example.Criteria criteria = example.and();
        criteria.andEqualTo("actorId", actorId);
        List<ActorShare> actorShares = actorShareMapper.selectByExample(example);
        return actorShares;
    }

    @Override
    public List<String> selectActorIdByShareId() {
        Role role=lakeService.getRole(InfTraceContextHolder.get().getUuid());
        RolePrivilege[] rolePrivileges=role.getRolePrivileges();
        Set<String> set=Sets.newSet();
        if (rolePrivileges!=null&&rolePrivileges.length>0){
            for (RolePrivilege rolePrivilege:rolePrivileges){
                if (ObjectType.CATALOG.name().equalsIgnoreCase(rolePrivilege.getGrantedOn())&&rolePrivilege.getPrivilege().indexOf(ObjectType.CATALOG.name())>-1){
                    set.add(rolePrivilege.getName());
                }
            }
        }
        return Lists.newArrayList(set);
    }

    @Override
    public void delete(Object id) {
        String sourceId = id.toString();
        Integer destinationId = (Integer) id;
        List<Task> tasks = taskMapper.selectTaskBySourceId(sourceId, destinationId);
        if (tasks != null && tasks.size() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.CANNOT_DELETE_DATA_SCHEMA);
        }
        Actor toUpdateParam = Actor.builder().build();

        toUpdateParam.setId((Integer) id);
        toUpdateParam.setDeleteStatus(DeleteEntity.DELETE);

        actorMapper.updateByPrimaryKeySelective(toUpdateParam);
    }

    @Override
    public List<Actor> selectByActorDefinitionIds(List<Integer> ids, String region) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        Example example = new Example(Actor.class);
        Example.Criteria criteria = example.or();
//        criteria.andEqualTo("region", region);
        criteria.andIn("actorDefinitionId", ids);
        criteria.andEqualTo("deleteStatus", 0);

        Boolean admin = InfTraceContextHolder.get().getAdmin();
        String groupUid = InfTraceContextHolder.get().getUuid();
        if (!admin) {
            criteria.andEqualTo("createUserGroupUuid", groupUid);
        }
        return getBaseMapper().selectByExample(example);
    }

    private void validateSchema(Integer actorDefinitionId, JsonNode config) {
        try {
            ActorDefinition actorDefinition = actorDefinitionMapper.selectByPrimaryKey(actorDefinitionId);
            if (actorDefinition == null) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据定义不存在");
            }

            ConnectorSpecification spec = Jsons.deserialize(actorDefinition.getSpec(), ConnectorSpecification.class);
            schemaValidator.ensure(spec.getConnectionSpecification(), config);
        } catch (JsonValidationException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "配置信息校验失败");
        }
    }


    public List<UserGroupVo> listUserGroupPrivilege(Integer id) {
        List<UserGroupVo> userGroupVoList=Lists.newArrayList();
        Actor actor=actorMapper.selectByPrimaryKey(id);
        Example example=new Example(UserGroup.class);
        Example.Criteria criteria=example.and();
        criteria.andEqualTo("deleteStatus",DeleteEntity.NOT_DELETE);
        List<UserGroup> userGroupList=userGroupMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userGroupList)){
            List<ActorQueryHandle> actorQueryHandleList=Collections.synchronizedList(Lists.newArrayList());
            for (UserGroup userGroup:userGroupList){
                ActorQueryHandle actorQueryHandle=new ActorQueryHandle(actor,userGroup,lakeService,userGroupVoList,InfTraceContextHolder.get().getTenantName());
                actorQueryHandleList.add(actorQueryHandle);
            }
            threadService.multi(actorQueryHandleList);
        }
        return userGroupVoList;
    }

    public void saveActorUserGroupPrivileges(ActorUserGroupVo actorUserGroupVo){
        Actor actor=actorMapper.selectByPrimaryKey(actorUserGroupVo.getId());
        Example example=new Example(UserGroup.class);
        Example.Criteria criteria=example.and();
        criteria.andEqualTo("deleteStatus",DeleteEntity.NOT_DELETE);
        List<UserGroup> userGroupList=userGroupMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userGroupList)){
            List<ActorSavePrivilegeHandle> actorSavePrivilegeHandles=Collections.synchronizedList(Lists.newArrayList());
            for (UserGroup userGroup:userGroupList){
                ActorSavePrivilegeHandle actorQueryHandle=new ActorSavePrivilegeHandle(actor,userGroup,lakeService,actorUserGroupVo,InfTraceContextHolder.get().getTenantName());
                actorSavePrivilegeHandles.add(actorQueryHandle);
            }
            threadService.multi(actorSavePrivilegeHandles);
        }
    }

    @Override
    public void run(String... args) throws Exception {
//        init();
    }

    private Operation getOperation(String name){
        for (Operation operation:Operation.values()){
            if (operation.getPrintName().equals(name)){
                return operation;
            }
        }
        return null;
    }

    public List<Actor> selectByConfigInfo(String where){
        return actorMapper.selectActorByWhere(where);
    }

}
