package com.ushareit.dstask.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.common.vo.ninebot.CreateUserGroupRequestVo;
import com.ushareit.dstask.common.vo.ninebot.CreateUserGroupResponseVo;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.*;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.LakeService;
import com.ushareit.dstask.service.UserGroupService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户组
 */
@Slf4j
@Service
public class UserGroupServiceImpl extends AbstractBaseServiceImpl<UserGroup> implements UserGroupService {

    @Resource
    private UserGroupRelationMapper userGroupRelationMapper;

    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private AccessGroupMapper accessGroupMapper;

    @Resource
    private AccessUserRoleMapper accessUserRoleMapper;

    @Resource
    private FileManagerMapper fileManagerMapper;

    @Autowired
    private LakeService lakeService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${createGroup.appId}")
    private String appId;

    @Value("${createGroup.appSecret}")
    private String appSecret;

    @Value("${createGroup.url}")
    private String url;



    public String createSign(SortedMap<String, String> params, String appSecret){
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> es =  params.entrySet();
        for (Map.Entry<String, String> entry : es) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"appId".equals(k) && !"appSecret".equals(k)) {
                sb.append(k).append("=").append(v).append("&");
            }
        }
        sb.append("appSecret=").append(appSecret);
        return DigestUtils.md5Hex(sb.toString()).toUpperCase();
    }

    public void addUserGroup(UserGroup userGroup) {
        List<UserGroup> userGroupList = userGroupMapper.selectUserGroupByName(userGroup.getName());
        if (CollectionUtils.isNotEmpty(userGroupList)) {
            throw new ServiceException(BaseResponseCodeEnum.USERGROUP_EXIST, BaseResponseCodeEnum.USERGROUP_EXIST.getMessage());
        }
        userGroup.setUuid(IdUtils.getLenthIdForUserGroup());
        userGroup.setCreateBy(InfTraceContextHolder.get().getUserName());
        userGroup.setUpdateBy(userGroup.getCreateBy());

        userGroup.setCreateTime(new Timestamp(System.currentTimeMillis()));
        userGroup.setUpdateBy(userGroup.getCreateBy());
        userGroup.setDeleteStatus(DeleteEntity.NOT_DELETE);

        CreateUserGroupRequestVo createUserGroupRequestVo=CreateUserGroupRequestVo.builder().createTime(userGroup.getCreateTime().getTime()).creator(userGroup.getCreateBy()).userGroupName(userGroup.getName())
                .defaultDatabase(userGroup.getDefaultHiveDbName()).build();

        SortedMap<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("userGroupName", createUserGroupRequestVo.getUserGroupName());
        sortedMap.put("creator", createUserGroupRequestVo.getCreator());
        sortedMap.put("defaultDatabase", createUserGroupRequestVo.getDefaultDatabase());
        sortedMap.put("appId", appId);
        sortedMap.put("ts", String.valueOf(System.currentTimeMillis()));


        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("sign", createSign(sortedMap, appSecret));
        map.add("appId", appId);
        map.add("creator", createUserGroupRequestVo.getCreator());
        map.add("userGroupName", createUserGroupRequestVo.getUserGroupName());
        map.add("defaultDatabase", createUserGroupRequestVo.getDefaultDatabase());
        map.add("ts", sortedMap.get("ts"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        if(StringUtils.isNotEmpty(url) && !url.equals("null") ){
            ResponseEntity<CreateUserGroupResponseVo> responseVoResponseEntity = restTemplate.postForEntity(url, new HttpEntity<>(map,headers),CreateUserGroupResponseVo.class);
            log.info("create group result:" + responseVoResponseEntity.toString());
            if (responseVoResponseEntity == null || !"0".equals(responseVoResponseEntity.getBody().getStatus())){
                throw new ServiceException(BaseResponseCodeEnum.USERGROUP_API_FAIL, BaseResponseCodeEnum.USERGROUP_API_FAIL.getMessage());
            }
        }
        userGroupMapper.insert(userGroup);
        lakeService.createRole(userGroup.getUuid());
        // 初始化各个模块的根文件夹
        List<FileManager> fms = new ArrayList<>();
        for (FileManager.Module module : FileManager.Module.values()) {
            FileManager fm = new FileManager();
            String rootName = "全部文件";
            switch (module) {
                case TASK:
                    rootName = "任务开发";
                    break;
                case ARTIFACT:
                    rootName = "全部资源";
                    break;
                default:
            }
            fm.setModule(module.name())
                    .setUserGroup(userGroup.getUuid())
                    .setParentId(0)
                    .setFileNums(0)
                    .setName(rootName)
                    .setIsFolder(true);
            fms.add(fm);
        }
        fileManagerMapper.insertList(fms);
    }

    public void editUserGroup(UserGroup userGroup) {
        List<UserGroup> userGroupList = userGroupMapper.selectUserGroupByNameAndIdNot(userGroup.getName(), userGroup.getId());
        if (CollectionUtils.isNotEmpty(userGroupList)) {
            throw new ServiceException(BaseResponseCodeEnum.USERGROUP_EXIST, BaseResponseCodeEnum.USERGROUP_EXIST.getMessage());
        }
        UserGroup old = userGroupMapper.selectByPrimaryKey(userGroup.getId());
        BeanUtils.copyProperties(userGroup, old, "createTime", "createBy","id","deleteStatus","uuid");
        old.setUpdateBy(InfTraceContextHolder.get().getUserName());
        old.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        userGroupMapper.updateByPrimaryKey(old);
    }

    public void deleteUserGroup(Integer id){
        UserGroup userGroup=userGroupMapper.selectByPrimaryKey(id);
        userGroup.setDeleteStatus(DeleteEntity.DELETE);
        userGroupMapper.updateByPrimaryKey(userGroup);
        lakeService.dropRole(userGroup.getUuid());
    }

    public UserGroup selectUserGroupById(Integer id){
        UserGroup userGroup = userGroupMapper.selectUserGroupById(id);
        return userGroup;
    }
    public UserGroup selectUserGroupByUuid(String uuid){
        UserGroup userGroup = userGroupMapper.selectUserGroupByUuid(uuid);
        return userGroup;
    }


    public void addUser(UserGroupRelation userGroupRelation) {
        List<UserGroupRelation> userGroupRelations = userGroupRelationMapper.selectByUserIdAndGroupId(userGroupRelation);
        if (CollectionUtils.isEmpty(userGroupRelations)){
            userGroupRelation.setCreateTime(new Timestamp(System.currentTimeMillis()));
            userGroupRelationMapper.insert(userGroupRelation);
            UserGroup userGroup=userGroupMapper.selectByPrimaryKey(userGroupRelation.getUserGroupId());
            lakeService.addUserToRole(userGroupRelation.getUserName(),userGroup.getUuid());
        }
    }

    public void removeUser(UserGroupRelation userGroupRelation) {
        UserGroupRelation old=userGroupRelationMapper.selectByPrimaryKey(userGroupRelation.getId());
        userGroupRelationMapper.deleteByPrimaryKey(userGroupRelation.getId());
        UserGroup userGroup=userGroupMapper.selectByPrimaryKey(old.getUserGroupId());
        lakeService.removeUser(old.getUserName(),userGroup.getUuid());
    }

    /**
     * 获取所有机构
     * @return
     */
    @Override
    public List<UserGroupVo> selectAllUserGroup() {
        List<UserGroupVo> userGroupVoList=Lists.newArrayList();
        Example example=new Example(UserGroup.class);
        Example.Criteria criteria=example.and();
        criteria.andEqualTo("deleteStatus",DeleteEntity.NOT_DELETE);
        List<UserGroup> userGroupList=userGroupMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userGroupList)){
            List<UserGroupRelation> userGroupRelations=userGroupRelationMapper.selectAll();
            for (UserGroup userGroup:userGroupList){
                UserGroupVo userGroupVo=new UserGroupVo();
                userGroupVoList.add(userGroupVo);
                BeanUtils.copyProperties(userGroup,userGroupVo);
                if (CollectionUtils.isNotEmpty(userGroupRelations)){
                    for (UserGroupRelation userGroupRelation:userGroupRelations){
                        if (userGroupRelation.getUserGroupId().intValue()==userGroup.getId().intValue()){
                            userGroupVo.getUserGroupRelationList().add(userGroupRelation);
                        }
                    }
                }
            }
        }
        return userGroupVoList;
    }

    /**
     * 获取登录人的组织机构
     * @return
     */
    @Override
    public List<UserGroupVo> selectLoginUserGroup() {
        List<UserGroupVo> userGroupVos=Lists.newArrayList();
        CurrentUser currentUser=InfTraceContextHolder.get().getUserInfo();
        List<UserGroupVo> userGroupVoList=selectAllUserGroup();
        if (CollectionUtils.isNotEmpty(userGroupVoList)){
            for (UserGroupVo userGroupVo:userGroupVoList){
                if (CollectionUtils.isNotEmpty(userGroupVo.getUserGroupRelationList())){
                    for (UserGroupRelation userGroupRelation:userGroupVo.getUserGroupRelationList()){
                        if (userGroupRelation.getUserId().intValue()==currentUser.getId()){
                            userGroupVos.add(userGroupVo);
                            break;
                        }
                    }
                }
            }
        }
        return userGroupVos;
    }

    @Override
    public PageInfo listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        if (!InfTraceContextHolder.get().getUserInfo().isAdmin()&&pageSize!=10000){
            paramMap.put("user_id",InfTraceContextHolder.get().getUserId().intValue()+"");
        }
        List<UserGroupVo> userGroupVoList=Lists.newArrayList();
        PageInfo pageInfo = super.listByPage(pageNum, pageSize, paramMap);
        List<UserGroupRelation> userGroupRelations=userGroupRelationMapper.selectAll();
        if (pageInfo!=null&&CollectionUtils.isNotEmpty(pageInfo.getList())){
            Map<Integer,AccessGroup> map=accessGroup();
            pageInfo.getList().forEach(userGroup -> {
                UserGroupVo userGroupVo=new UserGroupVo();
                BeanUtils.copyProperties(userGroup,userGroupVo);
                wrapUserGroupName(map,userGroupVo);
                if (CollectionUtils.isNotEmpty(userGroupRelations)){
                    for (UserGroupRelation userGroupRelation:userGroupRelations){
                        if (userGroupRelation.getUserGroupId().intValue()==userGroupVo.getId()){
                            userGroupVo.getUserGroupRelationList().add(userGroupRelation);
                        }
                    }
                }
                userGroupVoList.add(userGroupVo);
            });
        }
        pageInfo.setList(userGroupVoList);
        return pageInfo;
    }

    @Override
    public CrudMapper<UserGroup> getBaseMapper() {
        return userGroupMapper;
    }

    private Map<Integer,AccessGroup> accessGroup(){
        Map<Integer,AccessGroup> map= Maps.newHashMap();
        List<AccessGroup> accessGroups=accessGroupMapper.selectAll();
        if (CollectionUtils.isNotEmpty(accessGroups)){
            map=accessGroups.stream().filter(accessGroup -> accessGroup.getType()==0&&accessGroup.getDeleteStatus()==DeleteEntity.NOT_DELETE)
                    .collect(Collectors.toMap(AccessGroup::getId,accessGroup -> accessGroup));
        }
        return map;
    }

    private void wrapUserGroupName(Map<Integer,AccessGroup> map,UserGroupVo userGroup){
        if (map!=null&&map.size()>0&&userGroup.getParentId()!=null&&userGroup.getParentId()>0){
            List<String> userGroups= Lists.newArrayList();
            List<Integer> orgId= Lists.newArrayList();
            Integer parentId=userGroup.getParentId();
            while (parentId!=null){
                AccessGroup accessGroup=map.get(parentId);
                if (accessGroup!=null){
                    userGroups.add(accessGroup.getName());
                    orgId.add(accessGroup.getId());
                    parentId=accessGroup.getParentId();
                }else {
                    break;
                }
            }
            userGroup.setOrg(userGroups);
            userGroup.setOrgId(orgId);
        }
    }


    @Resource
    private AccessUserMapper accessUserMapper;

    @PostConstruct
    public void init(){
        try {
            InfTraceContextHolder.get().setTenantName(BaseConstant.defaultTenantName);
            List<UserGroup> userGroupList=userGroupMapper.selectAll();
            if (CollectionUtils.isEmpty(userGroupList)){
                UserGroup userGroup=new UserGroup();
                userGroup.setName(BaseConstant.defaultTenantName);
                userGroup.setDefaultHiveDbName("test");
                userGroup.setDescription("init");
                addUserGroup(userGroup);
                List<AccessUser> accessUserList=accessUserMapper.selectAll();
                if (CollectionUtils.isNotEmpty(accessUserList)){
                    userGroupList=userGroupMapper.selectAll();
                    for (UserGroup u:userGroupList){
                        for (AccessUser accessUser:accessUserList){
                            UserGroupRelation userGroupRelation=new UserGroupRelation();
                            userGroupRelation.setOwner(1);
                            userGroupRelation.setUserGroupId(u.getId());
                            userGroupRelation.setUserId(accessUser.getId());
                            userGroupRelation.setUserName(accessUser.getName());
                            addUser(userGroupRelation);
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("",e);
        }

    }

}
