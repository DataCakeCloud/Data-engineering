package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Accessors(chain = true)
public class ArtifactBase extends DataEntity {
    @NotBlank(message = "应用名字不能为空")
    @ApiModelProperty(value = "应用名字")
    private String name;

    @ApiModelProperty("模式 ONLINE/UPLOAD")
    @Column(name = "mode_code")
    private String modeCode;

    @ApiModelProperty("类型 JAR/CSV/JSON")
    @Column(name = "type_code")
    private String typeCode;

    @ApiModelProperty("文件名")
    @Column(name = "file_name")
    private String fileName;

    @ApiModelProperty("文件大小")
    @Column(name = "file_size")
    private Long fileSize;

    @ApiModelProperty("内容 ONLINE：编辑内容；UPLOAD：文件线上地址")
    @Column(name = "content")
    private String content;

    @Column(name = "delete_status")
    private Integer deleteStatus;


    public void copy(ArtifactBase artifactBase) {
        this.name = artifactBase.getName();
        this.modeCode = artifactBase.getModeCode();
        this.typeCode = artifactBase.getTypeCode();
        this.fileName = artifactBase.getFileName();
        this.fileSize = artifactBase.getFileSize();
        this.content = artifactBase.getContent();
        this.setDescription(artifactBase.getDescription());
        this.setCreateBy(artifactBase.getCreateBy());
        this.setUpdateBy(artifactBase.getUpdateBy());
    }
}
