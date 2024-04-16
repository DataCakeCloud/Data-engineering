package com.ushareit.dstask.service.impl;

import cn.hutool.core.net.URLEncodeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.Artifact;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsIndicatorsEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.ArtifactMapper;
import com.ushareit.dstask.mapper.ArtifactVersionMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.service.ArtifactVersionService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class ArtifactVersionServiceImpl extends AbstractBaseServiceImpl<ArtifactVersion> implements ArtifactVersionService {

    @Resource
    private ArtifactVersionMapper artifactVersionMapper;

    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private ArtifactMapper artifactMapper;

    @Resource
    private CloudFactory cloudFactory;


    @Override
    public CrudMapper<ArtifactVersion> getBaseMapper() {
        return artifactVersionMapper;
    }

    @Override
    public int getMaxVersionById(Integer id) {
        Integer maxVersion = artifactVersionMapper.getMaxVersionById(id);
        return maxVersion == null ? 0 : maxVersion;
    }

    @Override
    public ResponseEntity<Object> download(Integer artifactVersionId, Integer tenantId) throws IOException {

        AccessTenant accessTenant = accessTenantService.checkExist(tenantId);
        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        ArtifactVersion artifactVersion = artifactVersionMapper.selectByPrimaryKey(artifactVersionId);
        String url = artifactVersion.getContent();
        String region = artifactVersion.getRegion();
        checkRegion(region);
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(region);
        InputStreamResource resource;
        String download = null;
        try {
            download = cloudClientUtil.download(url);
            FileSystemResource file = new FileSystemResource(download);
            resource = new InputStreamResource(file.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("attachment;filename=%s;filename*=utf-8''%s",
                    URLEncodeUtil.encode(artifactVersion.getFileName()), URLEncodeUtil.encode(artifactVersion.getFileName())));
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            ResponseEntity.BodyBuilder ok = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType("application/x-java-archive"));

            if (DsTaskConstant.ARTIFACT_TYPE_TXT.equals(artifactVersion.getTypeCode())) {
                ok.contentType(MediaType.parseMediaType("application/txt"));
            }
            return ok.body(resource);
        } catch (IOException e) {
            throw new ServiceException(BaseResponseCodeEnum.DOWNLOAD_FAIL);
        } finally {
            if (StringUtils.isNotEmpty(download)) {
                File file = new File(download);
                if (file.exists()) {
                    org.apache.flink.util.FileUtils.deleteFileOrDirectory(file);
                }
            }
        }
    }

    /**
     * 获取工件下所有版本信息 对接的接口是 /page
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param paramMap 查询參數
     * @return
     */
    @Override
    public PageInfo<ArtifactVersion> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        PageInfo<ArtifactVersion> pageInfo = super.listByPage(pageNum, pageSize, paramMap);
        padVersionInfos(pageInfo.getList());
        return pageInfo;
    }

    @Override
    public List<ArtifactVersion> listByExample(ArtifactVersion artifactVersion) {
        CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
        if (StringUtils.isEmpty(artifactVersion.getRegion())) {
            artifactVersion.setRegion(defaultRegionConfig.getRegionAlias());
        }
        List<ArtifactVersion> list = artifactVersionMapper.selectByIdAndRegion(ArtifactVersion.builder()
                .artifactId(artifactVersion.getArtifactId()).region(artifactVersion.getRegion()).build());
        padVersionInfos(list);
        list.add(0, ArtifactVersion.CreateCurrent(artifactVersion));
//        Integer maxVersionById = artifactVersionMapper.getMaxVersionById(artifactVersion.getArtifactId());
//        //如果 list中存在最大的版本
//        if (maxVersionById != null && !list.isEmpty()) {
//            List<Integer> collect = list.stream().map(data -> data.getVersion()).collect(Collectors.toList());
//            if (collect.contains(maxVersionById)) {
//                list.add(0, ArtifactVersion.CreateCurrent(artifactVersion));
//            }
//        }

        return list;
    }

    private void padVersionInfos(List<ArtifactVersion> list) {
        list.stream().forEach(artifactVersion -> {
            padVersionInfo(artifactVersion);
        });
    }

    private void padVersionInfo(ArtifactVersion artifactVersion) {
        artifactVersion.setDisplayVersion("V" + artifactVersion.getVersion());
        artifactVersion.setDisplayFileSize(CommonUtil.convertSize(artifactVersion.getFileSize()));
    }

    @Override
    public ArtifactVersion selectByArtiIdAndArtiVersionId(Integer artifactId, Integer artifactVersionId) {
        ArtifactVersion artifactVersion = artifactVersionMapper.selectByArtiIdAndArtiVersionId(artifactId, artifactVersionId);
        return artifactVersion;
    }


    @Override
    public List<ArtifactVersion> getDisplayArtifact(String dependJars, String region) {
        // 6:-1 or 6:7 or 6:-1,6:7
        String[] depJarIds = dependJars.split(",");
        log.info(" dependJars is :" + dependJars + " region is :" + region);
        ArrayList<ArtifactVersion> list = new ArrayList<>(16);
        for (String depJarId : depJarIds) {
            String[] entry = depJarId.split(":");
            ArtifactVersion artifactVersion;
            if ("-1".equals(entry[1])) {
                List<ArtifactVersion> maxVersionByArtifactId = artifactVersionMapper.getMaxVersionByArtifactId(Integer.parseInt(entry[0]), region);
                if (maxVersionByArtifactId == null || maxVersionByArtifactId.isEmpty()) {
                    continue;
                }
                ArtifactVersion artifact = maxVersionByArtifactId.stream().findFirst().orElse(null);
                artifactVersion = (ArtifactVersion) new ArtifactVersion()
                        .setArtifactId(artifact.getArtifactId())
                        .setVersion(-1)
                        .setRegion(artifact.getRegion())
                        .setDisplayVersion("Current")
                        .setName(artifact.getName())
                        .setModeCode(artifact.getModeCode())
                        .setTypeCode(artifact.getTypeCode())
                        .setContent(artifact.getContent())
                        .setDescription(artifact.getDescription())
                        .setCreateBy(artifact.getCreateBy())
                        .setUpdateBy(artifact.getUpdateBy())
                        .setCreateTime(artifact.getCreateTime());
                artifactVersion.setId(-1);
            } else {
                artifactVersion = super.getById(entry[1]);
            }

            list.add(artifactVersion);
        }
        return list;
    }

    @Override
    public Integer getNewArtifactVersionCount() {
        return artifactVersionMapper.getNewArtifactVersionCount();
    }

    @Override
    public HashMap<String, Integer> getArtifactIndicators() {
        HashMap<String, Integer> res = new HashMap<>();
        res.put(DsIndicatorsEnum.NEW_ARTIFACT_COUNT.name(), artifactVersionMapper.getNewArtifactVersionCount());
        res.put(DsIndicatorsEnum.ACC_ARTIFACT_COUNT.name(), artifactVersionMapper.getAccArtifactVersionCount());
        return res;
    }

    @Override
    public List<ArtifactVersion> selectByArtifactid(Integer id) {
        return artifactVersionMapper.select(ArtifactVersion.builder().artifactId(id).build());
    }

    @Override
    public List<ArtifactVersion> listByArtifactIds(List<Integer> artifactIds) {
        if (artifactIds == null || artifactIds.isEmpty()) {
            return new ArrayList<>();
        }
        Example example = new Example(ArtifactVersion.class);
        Example.Criteria criteria = example.or();
        criteria.andIn("artifactId", artifactIds);
        return artifactVersionMapper.selectByExample(example);
    }

    @Override
    public List<ArtifactVersion> selectByRegion(String region) {
        ArtifactVersion build = ArtifactVersion.builder().region(region).build();
        return artifactVersionMapper.select(build);
    }

    private String getRegion(ArtifactVersion artifactVersion) {
        Artifact artifact = artifactMapper.selectByPrimaryKey(artifactVersion.getArtifactId());
        return artifact.getRegion();
    }

    private void checkRegion(String region) {
        if (StringUtils.isEmpty(region)) {
            throw new ServiceException(BaseResponseCodeEnum.ARTIFACT_REGION_IS_NULL);
        }
    }
}
