package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/3/8
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "feedback_process_item")
public class FeedbackProcessItem extends DeleteEntity {
    private static final long serialVersionUID = -5145921671266272563L;

    @ApiModelProperty("用户反馈（工单）ID")
    private Integer feedbackId;

    @ApiModelProperty("处理明细信息")
    private String detail;

    @ApiModelProperty("附件id")
    private String attachmentIds;

    @Transient
    private Integer maxId;

    @Transient
    private List<MultipartFile> multipartFileList;

    @Transient
    private List<Attachment> attachmentList;

    @Transient
    @ApiModelProperty("详情附件地址，用;好分割")
    public String fileUrl;

    @Transient
    @ApiModelProperty("是否是当前用户")
    public Boolean isCurrentUser = false;

    @Transient
    public String reason;

    @Transient
    public Integer score;

}
