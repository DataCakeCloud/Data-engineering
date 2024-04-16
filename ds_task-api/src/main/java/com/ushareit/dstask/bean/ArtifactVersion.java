package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "artifact_version")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("工件版本类")
public class ArtifactVersion extends ArtifactBase {
    @ApiModelProperty("工件ID")
    @Column(name = "artifact_id")
    private Integer artifactId;

    @ApiModelProperty("版本号")
    private Integer version;

    @Transient
    private String displayVersion;

    @Transient
    private String displayFileSize;

    @ApiModelProperty("所属云 aws | obs")
    @Column(name = "region")
    private String region;

    public ArtifactVersion(ArtifactBase artifactBase) {
        super.copy(artifactBase);
    }

    public static ArtifactVersion CreateCurrent(ArtifactVersion artifactVersion){
        ArtifactVersion current = ArtifactVersion.builder().artifactId(artifactVersion.artifactId).version(-1).displayVersion("Current").build();
        current.setId(-1);
        return current;
    }
}
