package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Artifact;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Data
public class ArtifactNameVO {

    private Integer id;
    private String name;

    public ArtifactNameVO(Artifact artifact) {
        this.id = artifact.getId();
        this.name = artifact.getName();
    }
}
