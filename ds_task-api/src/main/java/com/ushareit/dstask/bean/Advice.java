package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author xuebotao
 * @date 2022-08-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "advice")
public class Advice extends DeleteEntity {

    private static final long serialVersionUID = -7507311261336761336L;

    @ApiModelProperty("产品名称")
    private String appName;

    @ApiModelProperty("租户ID")
    private Integer tenantId;

    @ApiModelProperty("用户满意度")
    private Integer score;

    @ApiModelProperty("建议信息")
    private String adviceInfo;

    @ApiModelProperty("附件列表")
    private String attachmentIds;

    @Transient
    private List<MultipartFile> multipartFileLists;

    @Transient
    private List<Attachment> attachmentLists;

}
