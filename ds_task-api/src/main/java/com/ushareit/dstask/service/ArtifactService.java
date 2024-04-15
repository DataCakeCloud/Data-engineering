package com.ushareit.dstask.service;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Artifact;
import com.ushareit.dstask.common.vo.ArtifactNameVO;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface ArtifactService extends BaseService<Artifact> {

    /**
     * 按工件名称模糊查询
     *
     * @param name 工件名字
     */
    List<ArtifactNameVO> searchByName(String name);


    PageInfo<Artifact> page(Integer pageNum, Integer pageSize, Map<String, String> paramMap);

}
