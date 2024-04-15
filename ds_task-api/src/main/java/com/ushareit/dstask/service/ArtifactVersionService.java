package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.ArtifactVersion;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface ArtifactVersionService extends BaseService<ArtifactVersion> {
    /**
     * 根据应用ID查询最大版本号
     *
     * @param id
     * @return
     */
    int getMaxVersionById(Integer id);

    /**
     * 查询依赖工件
     *
     * @param dependJars
     * @return
     */
    List<ArtifactVersion> getDisplayArtifact(String dependJars,String region);

    /**
     * 下载jar
     *
     * @param artifactVersionId
     * @return
     */
    ResponseEntity<Object> download(Integer artifactVersionId,Integer tenantId) throws IOException;

    /**
     * 查询当天新建工件数
     *
     * @return
     */
    Integer getNewArtifactVersionCount();

    ArtifactVersion selectByArtiIdAndArtiVersionId(Integer artifactId, Integer artifactVersionId);

    /**
     * 查询工件相关指标
     */
    HashMap<String, Integer> getArtifactIndicators();

    List<ArtifactVersion> selectByArtifactid(Integer id);

    List<ArtifactVersion> listByArtifactIds(List<Integer> ids);

    List<ArtifactVersion> selectByRegion(String region);

}
