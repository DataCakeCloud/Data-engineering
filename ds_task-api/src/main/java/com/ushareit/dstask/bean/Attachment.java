package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author fengxiao
 * @date 2021/11/1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "attachment")
public class Attachment extends DeleteEntity {
    private static final long serialVersionUID = 4793705614409206270L;

    @ApiModelProperty(value = "附件地址")
    private String fileUrl;

    @ApiModelProperty(value = "附件文件名")
    private String fileName;

    @ApiModelProperty(value = "附件类型")
    private String contentType;

    @ApiModelProperty(value = "文件大小")
    private Long fileSize;
}
