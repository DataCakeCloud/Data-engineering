package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "feedback")
public class Feedback extends DeleteEntity {
    private static final long serialVersionUID = -7661645362254593196L;

    public static final Integer NOT_NOTIFY = 0;
    public static final Integer NOTIFY = 1;

    @ApiModelProperty("租户ID")
    private Integer tenantId;

    @ApiModelProperty("产品名称")
    private String appName;

    @ApiModelProperty("问题模块")
    private String module;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("问题类别")
    private String type;

    @ApiModelProperty("页面地址")
    private String pageUri;

    @ApiModelProperty("任务Id")
    private String taskId;

    @ApiModelProperty("附件ID集合")
    private String attachmentIds;

    @ApiModelProperty("反馈状态")
    private String status;

    @ApiModelProperty("负责人")
    private String chargePerson;

    @ApiModelProperty("当前处理人")
    private String handleBy;

    @ApiModelProperty("问题解决时间")
    private Date resolveTime;

    @ApiModelProperty("解决方案")
    private String resolveReply;

    @ApiModelProperty("用户打分")
    private Integer score;

    @ApiModelProperty("工单首次受理时间")
    private Timestamp firstAcceptTime;

    @ApiModelProperty("工单首次关闭时间")
    private Timestamp firstCloseTime;

    @ApiModelProperty("工单首次通知")
    @Column(name = "first_notify")
    private Integer firstNotify;

    @ApiModelProperty("工单二次通知")
    @Column(name = "second_notify")
    private Integer secondNotify;

    @ApiModelProperty("用户组")
    @Column(name = "user_group")
    private String userGroup;

    @ApiModelProperty("工单级别")
    private String feedbackLevel;

    @Transient
    private String firstAcceptDuration;

    @Transient
    private String firstCloseDuration;

    @Transient
    private List<Attachment> attachmentList;

    @Transient
    public List<FeedbackProcessItem> feedbackProcessItemList;

    @Transient
    public Integer maxId;

    @Transient
    public String database;

    @Transient
    public Boolean sendCloseFlag = true;
}
