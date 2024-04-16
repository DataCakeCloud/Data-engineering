package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.FeedbackStatusEnum;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Data
@Slf4j
public class FeedbackSaveParam {

    private String appName = "ds";

    private String module = StringUtils.EMPTY;

    private String title = StringUtils.EMPTY;

    @NotBlank(message = "问题类型不能为空")
    private String type;

    @NotBlank(message = "问题描述不能为空")
    @Length(max = 2000, message = "描述长度不能超过2000个字符")
    private String description;

    private String pageUri;

    private String createBy;

    private String feedbackLevel;

    private String taskId;

    private List<MultipartFile> attachmentList;

    private String database;
    private List<String> tables;

    public Feedback toEntity(CloudFactory cloudFactory) {
        if(DsTaskConstant.NineBotDataApplication.equals(type)){
            description = String.format("%s  申请的库名为:%s,申请的表名包括:%s",description,database,tables.stream().collect(Collectors.joining("、")));
        }
        Feedback feedback = new Feedback();
        feedback.setModule(module);
        feedback.setTitle(title);
        feedback.setType(type);
        feedback.setDescription(description);
        feedback.setPageUri(pageUri);
        feedback.setTaskId(taskId);
        feedback.setDatabase(database);
        feedback.setStatus(FeedbackStatusEnum.UN_ACCEPT.name());
        feedback.setCreateBy(InfTraceContextHolder.get().getUserName());
        feedback.setAppName(appName);
        feedback.setFeedbackLevel(feedbackLevel);
        feedback.setUserGroup(InfTraceContextHolder.get().getUuid());
        if (StringUtils.isBlank(createBy)) {
            feedback.setCreateBy(InfTraceContextHolder.get().getUserName());
        } else {
            feedback.setCreateBy(createBy);
        }

        feedback.setAttachmentList(new ArrayList<>());
        for (MultipartFile attachment : CollectionUtils.emptyIfNull(attachmentList)) {
            if (attachment != null && !attachment.isEmpty()) {
                CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
                CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(defaultRegionConfig.getRegionAlias());
                String url = cloudClientUtil.upload(attachment, "feedback", defaultRegionConfig.getRegionAlias());
                feedback.getAttachmentList().add(new Attachment()
                        .setFileUrl(url)
                        .setFileName(attachment.getOriginalFilename())
                        .setFileSize(attachment.getSize())
                        .setContentType(attachment.getContentType()));

                log.info("attachment url is {}", url);
            }
        }
        return feedback;
    }

}
