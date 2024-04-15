package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

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
@Table(name = "artifact")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("工件类")
public class Artifact extends ArtifactBase {
    @Column(name = "delete_status")
    private Integer deleteStatus;

    @Transient
    @JSONField(serialize = false)
    private transient MultipartFile jarFile;

    @Transient
    private String region;

    @Transient
    private String regionList;

    //0 是公共 1 是私有
    public Integer isPublic ;

    @Column(name = "user_group")
    private String userGroup;

    @Column(name = "folder_id")
    private Integer folderId;

}
