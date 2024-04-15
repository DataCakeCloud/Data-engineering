package com.ushareit.dstask.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.ArtifactNameVO;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.ArtifactMapper;
import com.ushareit.dstask.mapper.FileManagerMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessGroupService;
import com.ushareit.dstask.service.AccessUserService;
import com.ushareit.dstask.service.ArtifactService;
import com.ushareit.dstask.service.ArtifactVersionService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class ArtifactServiceImpl extends AbstractBaseServiceImpl<Artifact> implements ArtifactService {

    @Resource
    private ArtifactMapper artifactMapper;

    @Override
    public CrudMapper<Artifact> getBaseMapper() {
        return artifactMapper;
    }

    @Resource
    private ArtifactVersionService artifactVersionService;

    @Resource
    private UserGroupServiceImpl userGroupServiceImpl;

    @Resource
    private FileManagerServiceImpl fileManagerServiceImpl;

    @Resource
    private FileManagerMapper fileManagerMapper;

    @Resource
    private AccessUserService accessUserService;

    @Resource
    private AccessGroupService accessGroupService;

    @Resource
    private CloudFactory cloudFactory;

    @Override
    public Object save(Artifact artifact) {
        //1.参数预校验
        preCheckCommon(artifact);
        //校验 工件模式
        if (DsTaskConstant.ARTIFACT_MODE_UPLOAD.equals(artifact.getModeCode())) {
            if (artifact.getJarFile() == null) {
                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_MODE_NOT_MATCH);
            }
            String filename = artifact.getJarFile().getOriginalFilename();
            String prefix = filename.substring(filename.lastIndexOf(".") + 1);
//            if (!artifact.getTypeCode().equalsIgnoreCase(prefix)) {
//                throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_TYPE_NOT_MATCH);
//            }
        }
        String[] regionArray = artifact.getRegionList().split(",");
        for (String region : regionArray) {
            artifact.setRegion(region);
            //2.设置回显文件名
            if (DsTaskConstant.ARTIFACT_MODE_UPLOAD.equals(artifact.getModeCode())) {
                artifact.setContent(uploadByRegion(artifact));
                artifact.setFileSize(artifact.getJarFile().getSize());
                artifact.setFileName(artifact.getJarFile().getOriginalFilename());
            } else {
                artifact.setFileSize(Long.valueOf(artifact.getContent().length()));
            }
            //3、保存工件
            if (region.equals(regionArray[0])) {
                super.save(artifact);
                if (artifact.getFolderId() != null) {
                    // 工件归属文件夹，添加到文件管理表中
                    FileManager fm = new FileManager();
                    fm.setName(artifact.getName())
                            .setModule(FileManager.Module.ARTIFACT.name())
                            .setParentId(artifact.getFolderId())
                            .setUserGroup(artifact.getUserGroup())
                            .setIsFolder(false)
                            .setEntityId(artifact.getId());
                    fileManagerServiceImpl.add(fm);
                }
            }
            //4.保存工具版本
            int currentMaxVersion = artifactVersionService.getMaxVersionById(artifact.getId());
            int version = currentMaxVersion == 0 ? 1 : ++currentMaxVersion;
            ArtifactVersion artifactVersion = new ArtifactVersion(artifact)
                    .setRegion(region).setArtifactId(artifact.getId()).setVersion(version);
            artifactVersionService.save(artifactVersion);
        }
        return artifact;
    }

    @Override
    public void update(Artifact artifactFromWeb) {
        //1.参数预校验
        preCheckCommon(artifactFromWeb);
        Artifact byName = super.getByName(artifactFromWeb.getName());
        String userName = InfTraceContextHolder.get().getUserName();
        String createBy = byName.getCreateBy();
        List<String> sameGroupUser = getSameGroupUser(byName.getCreateBy());
        if (!accessUserService.isRootUser(userName) && !userName.equals(createBy) && artifactFromWeb.getIsPublic().equals(1)
                && (sameGroupUser.isEmpty() || !sameGroupUser.contains(userName))) {
            throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_NOT_ACCESS);
        }

        String[] regionArray = artifactFromWeb.getRegionList().split(",");
        for (String region : regionArray) {
            artifactFromWeb.setRegion(region);
            //2.设置回显文件名
            if (DsTaskConstant.ARTIFACT_MODE_UPLOAD.equals(artifactFromWeb.getModeCode())) {
                if (artifactFromWeb.getJarFile() == null) {
//                    Artifact artifactFromDb = super.getById(artifactFromWeb.getId());
//                    artifactFromWeb.setContent(artifactFromDb.getContent());
//                    artifactFromWeb.setFileSize(artifactFromDb.getFileSize());
//                    artifactFromWeb.setFileName(artifactFromDb.getFileName());
                    super.update(artifactFromWeb);
                    return;
//                    throw new ServiceException(BaseResponseCodeEnum.FILE_IS_NOT_NULL);
                } else {
//                String url = ObsClientUtil.upload(artifactFromWeb.getJarFile());
                    artifactFromWeb.setContent(uploadByRegion(artifactFromWeb));
                    artifactFromWeb.setFileSize(artifactFromWeb.getJarFile().getSize());
                    artifactFromWeb.setFileName(artifactFromWeb.getJarFile().getOriginalFilename());
                }
            } else {
                artifactFromWeb.setFileSize(Long.valueOf(artifactFromWeb.getContent().length()));
            }
            //3、保存工件
            if (region.equals(regionArray[regionArray.length - 1])) {
                super.update(artifactFromWeb);
                // 文件管理相关的修改
                if (artifactFromWeb.getFolderId() != null){
                    FileManager fm = fileManagerMapper.selectByEntityId(artifactFromWeb.getId(), FileManager.Module.ARTIFACT.name());
                    if(!fm.getParentId().equals(artifactFromWeb.getFolderId())){
                        // 如果工件归属的文件夹发生更改，需要更新文件管理表中的数据
                        fileManagerServiceImpl.move(fm.getId(),fm.getParentId(),artifactFromWeb.getFolderId());
                    }
                    // 工件名称现在不支持修改，所以不用处理名称变更的情况
                }
            }
            //4.保存工具版本
            int currentMaxVersion = artifactVersionService.getMaxVersionById(artifactFromWeb.getId());
            ArtifactVersion artifactVersion = new ArtifactVersion(artifactFromWeb).setRegion(region)
                    .setArtifactId(artifactFromWeb.getId()).setVersion(++currentMaxVersion);
            artifactVersion.setCreateBy(artifactFromWeb.getUpdateBy());
            artifactVersion.setCreateTime(artifactFromWeb.getUpdateTime());
            artifactVersionService.save(artifactVersion);
        }
    }

    private String uploadByRegion(Artifact artifact) {
        String region = artifact.getRegion();
        if (StringUtils.isEmpty(region)) {
            throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_REGION_IS_NULL);
        }
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(region);
        return cloudClientUtil.upload(artifact.getJarFile(), region);
    }


    private void preCheckCommon(Artifact artifact) {
        //1.校验 名称
        if (!match(artifact.getName(), DsTaskConstant.ARTIFACT_NAME_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.NAME_NOT_MATCH);
        }

        //2.Name不重复校验
        super.checkOnUpdate(super.getByName(artifact.getName()), artifact);
    }

    @Override
    public void delete(Object id) {
        Artifact artifact = checkExist(id);
        artifact.setDeleteStatus(1);
        String userName = InfTraceContextHolder.get().getUserName();
        if (!accessUserService.isRootUser(userName) && !userName.equals(artifact.getCreateBy())) {
            throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_NOT_ACCESS);
        }
        super.update(artifact);
        // 删除文件夹中的工件
        fileManagerServiceImpl.deleteByEntityId(Integer.parseInt((String) id),FileManager.Module.ARTIFACT.name());
    }

    private Artifact checkExist(Object id) {
        Artifact artifact = super.getById(id);
        if (artifact == null || artifact.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        return artifact;
    }

    @Override
    public List<Artifact> listByExample(Artifact artifact) {
        artifact.setDeleteStatus(0);
        String userName = InfTraceContextHolder.get().getUserName();
        Boolean rootUser = accessUserService.isRootUser(userName);
        Set<Integer> collect = artifactVersionService.selectByRegion(artifact.getRegion())
                .stream().map(ArtifactVersion::getArtifactId).collect(Collectors.toSet());

        List<Artifact> selectList = getBaseMapper().select(artifact)
                .stream().filter(data -> collect.contains(data.getId())).collect(Collectors.toList());

        if (!rootUser && !selectList.isEmpty()) {
            selectList = selectList.stream().filter(data -> {
                if (data.getCreateBy().equals(userName) || data.getIsPublic() == 0) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }
        return selectList;
    }



    /**
     * @param id 主键
     * @return
     */
    @Override
    public Artifact getById(Object id) {
        Artifact artifact = super.getById(id);
        List<ArtifactVersion> artifactVersions = artifactVersionService.selectByArtifactid(Integer.parseInt((String) id));
        Set<String> regionSet = new HashSet<>();
        for (ArtifactVersion artifactVersion : artifactVersions) {
            regionSet.add(artifactVersion.getRegion());
        }
        artifact.setRegionList(StringUtils.join(regionSet, ","));
        // 回显工件的归属文件夹id
        FileManager fm = fileManagerMapper.selectByEntityId(artifact.getId(), FileManager.Module.ARTIFACT.name());
        if (fm != null){
            artifact.setFolderId(fm.getParentId());
        }
        return artifact;
    }

    @Override
    public List<ArtifactNameVO> searchByName(String name) {
        Example example = new Example(Artifact.class);
        if (StringUtils.isNotBlank(name)) {
            example.or()
                    .andLike("name", "%" + name + "%");
        }

        example.setOrderByClause("update_time desc");
        return listByExample(example).stream()
                .map(ArtifactNameVO::new)
                .sorted(Comparator.comparing(ArtifactNameVO::getName))
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo<Artifact> page(Integer pageNum, Integer pageSize, Map<String, String> paramMap) {
        //先判断用户是否是root用户
        String userName = InfTraceContextHolder.get().getUserName();
        //Boolean rootUser = accessUserService.isRootUser(userName);
        Integer folderId = CommonUtil.getIdFromMap(paramMap, "folderId");
        List<String> sameGroupUser = new ArrayList<>();
/*        if (!rootUser) {
            sameGroupUser = getSameGroupUser(userName);
            sameGroupUser.add(userName);
            paramMap.put("owner", userName);
            paramMap.put("isPublic", "0");
            paramMap.put("nonRoot", "1");
        }*/
        List<Integer> idList=null;
        Page<Artifact> tPageInfo = artifactMapper.listByMap(sameGroupUser, paramMap);
        List<Artifact> artifacts=tPageInfo.getResult();
        // 如果查询的是文件夹下的工件
        if (folderId != null) {
            String fileName = "";
            if (paramMap.containsKey("fileName") && StringUtils.isNotBlank(paramMap.get("fileName"))) {
                fileName = paramMap.get("fileName");
            }
            idList = fileManagerServiceImpl.getEntityIdContainedInFolder(folderId, fileName);
            // 这里以前的思路是先取所有工件，然后再筛选需要的工件id，继续沿用了。
            if(idList.size() == 0){
                artifacts=null;
            }else{
                if (CollectionUtils.isNotEmpty(artifacts)){
                    List<Artifact> as= Lists.newArrayList();
                    for (Artifact artifact:artifacts){
                        if (idList.contains(artifact.getId())){
                            as.add(artifact);
                        }
                    }
                    artifacts=as;
                }
            }
        }


        PageInfo<Artifact> pageInfo = getPageInfo(pageNum, pageSize, artifacts);
        List<Artifact> pageList = pageInfo.getList();
        List<Integer> artifactIds = pageList.stream().map(Artifact::getId).collect(Collectors.toList());
        Map<Integer, List<ArtifactVersion>> collect = artifactVersionService.listByArtifactIds(artifactIds)
                .stream().collect(Collectors.groupingBy(ArtifactVersion::getArtifactId));

        Map<Integer, String> artifactRegionMap = new HashMap<>();
        for (Map.Entry<Integer, List<ArtifactVersion>> entry : collect.entrySet()) {
            List<ArtifactVersion> value = entry.getValue();
            String regionList = StringUtils.join(value.stream()
                    .map(ArtifactVersion::getRegion).distinct().collect(Collectors.toList()), ",");
            artifactRegionMap.put(entry.getKey(), regionList);
        }
        List<Artifact> resList = pageList.stream()
                .peek(data->{
                    data.setRegionList(artifactRegionMap.get(data.getId()));
                    UserGroup userGroup = userGroupServiceImpl.selectUserGroupByUuid(data.getUserGroup());
                    if(userGroup!=null){
                        data.setUserGroup(userGroup.getName());
                    }
                })
                .collect(Collectors.toList());
        if(folderId != null){
            // 如果查询的是文件夹下的工件，则需要按照文件夹的规则排序展示结果
            resList.sort(Comparator.comparing(Artifact::getName));
        }

        pageInfo.setList(resList);
        return pageInfo;
    }

    /**
     * 获取与自己同组人
     */
    public List<String> getSameGroupUser(String name) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        AccessUser build = AccessUser.builder().tenantId(tenantId).name(name).build();
        build.setDeleteStatus(DeleteEntity.NOT_DELETE);
        AccessUser accessUser = accessUserService.selectOne(build);
        if (accessUser == null) {
            return new ArrayList<>();
        }

        List<AccessGroup> accessGroupList = accessGroupService.selectByUserIds(Collections.singletonList(accessUser.getId()));
        List<Integer> ids = accessGroupList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        //同组用户名称
        return accessGroupService.seletByParentIds(ids, 1).stream()
                .map(AccessGroup::getName).collect(Collectors.toList());
    }
}
