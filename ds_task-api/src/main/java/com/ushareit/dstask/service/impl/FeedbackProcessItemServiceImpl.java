package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.bean.FeedbackProcessItem;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.FeedbackLevelEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.FeedbackProcessItemMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AttachmentService;
import com.ushareit.dstask.service.DutyInfoService;
import com.ushareit.dstask.service.FeedbackProcessItemService;
import com.ushareit.dstask.service.FeedbackService;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.third.scmp.SCMPService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/3/8
 */
@Service
public class FeedbackProcessItemServiceImpl extends AbstractBaseServiceImpl<FeedbackProcessItem>
        implements FeedbackProcessItemService {

    @Value("${server-url.host}")
    private String serverHost;

    @Resource
    private AttachmentService attachmentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackProcessItemMapper feedbackProcessItemMapper;

    @Resource
    private DingDingService dingDingService;

    @Resource
    private SCMPService scmbService;

    @Resource
    private DutyInfoService dutyInfoService;

    @Resource
    private CloudFactory cloudFactory;

    @Override
    public CrudMapper<FeedbackProcessItem> getBaseMapper() {
        return feedbackProcessItemMapper;
    }

    @Override
    public void saveItem(Integer feedbackId, String message, String operator, String attachIds) {
        FeedbackProcessItem toSaveEntity = new FeedbackProcessItem()
                .setFeedbackId(feedbackId)
                .setDetail(message);
        if (StringUtils.isNotEmpty(attachIds)) {
            toSaveEntity.setAttachmentIds(attachIds);
        }
        toSaveEntity.setCreateBy(operator);
        toSaveEntity.setUpdateBy(operator);

        save(toSaveEntity);
    }

    @Override
    public void saveItem(Integer feedbackId, String message, String operator) {
        saveItem(feedbackId, message, operator, null);
    }

    @Override
    public List<FeedbackProcessItem> getItemList(Integer feedbackId) {
        Example example = new Example(FeedbackProcessItem.class);
        example.or()
                .andEqualTo("feedbackId", feedbackId);
        example.setOrderByClause("id asc");

        return feedbackProcessItemMapper.selectByExample(example);
    }

    @Override
    public List<FeedbackProcessItem> getItemListByFids(List<Integer> feedbackIds) {
        Example example = new Example(FeedbackProcessItem.class);
        example.or().andIn("feedbackId", feedbackIds);
        example.setOrderByClause("id asc");
        return feedbackProcessItemMapper.selectByExample(example);
    }


    public Feedback getDetailInformation(FeedbackProcessItem feedbackProcessItem) {
        Feedback feedRes = checkExist(feedbackProcessItem.getFeedbackId());
        if (feedbackProcessItem.getMaxId() == null) {
            feedbackProcessItem.setMaxId(0);
        }
        List<FeedbackProcessItem> feedbackProcessItems = feedbackProcessItemMapper.selectByFeedId(feedbackProcessItem.getFeedbackId()
                , feedbackProcessItem.getMaxId());
        List<Integer> attachIds = feedbackProcessItems.stream()
                .flatMap(item -> Arrays.stream(item.getAttachmentIds().split(SymbolEnum.COMMA.getSymbol())))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(Integer::parseInt).collect(Collectors.toList());

        HashMap<Integer, Attachment> collectMap = attachmentService.listByIds(attachIds)
                .stream()
                .collect(HashMap::new, (m, item) -> m.put(item.getId(), item), HashMap::putAll);
        List<FeedbackProcessItem> collectRes = feedbackProcessItems.stream().map(feedback -> {
            if (StringUtils.isNotEmpty(feedback.getCreateBy()) &&
                    feedback.getCreateBy().equals(InfTraceContextHolder.get().getUserName())) {
                feedback.setIsCurrentUser(true);
            }
            List<Attachment> attachmentList = new ArrayList<>();
            String attachmentIds = feedback.getAttachmentIds();
            if (StringUtils.isNotEmpty(attachmentIds)) {
                List<String> attIds = Arrays.asList(feedback.getAttachmentIds().split(SymbolEnum.COMMA.getSymbol()));
                for (String attid : attIds) {
                    Attachment attachment = collectMap.getOrDefault(Integer.parseInt(attid), null);
                    if (attachment != null) {
                        attachmentList.add(attachment);
                    }
                }
            }
            feedback.setAttachmentList(attachmentList);
            return feedback;
        }).collect(Collectors.toList());
        feedRes.setFeedbackLevel(Objects.requireNonNull(FeedbackLevelEnum.of(feedRes.getFeedbackLevel())).getLevelName());
        feedRes.setFeedbackProcessItemList(collectRes);

        String currentName = InfTraceContextHolder.get().getUserName();
        if (!StringUtils.equalsIgnoreCase(currentName, feedRes.getCreateBy())
                && !StringUtils.equalsIgnoreCase(currentName, feedRes.getHandleBy())
                && !StringUtils.equalsIgnoreCase(currentName, feedRes.getChargePerson())) {
            feedRes.setSendCloseFlag(false);
        }
        return feedRes;
    }


    public Feedback checkExist(Integer id) {
        if (id == null || id <= 0) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_ID_ERR);
        }
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null || feedback.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        return feedback;
    }

    @Override
    public Feedback messageSend(FeedbackProcessItem feedbackProcessItem) {
        chekParam(feedbackProcessItem);
        Feedback feedback = feedbackService.getById(feedbackProcessItem.getFeedbackId());
        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), getDutyMan())) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_USER_MESSAGE_FAILURE);
        }
        List<Attachment> attachmentList = new ArrayList<>();
        List<MultipartFile> multipartFileList = feedbackProcessItem.getMultipartFileList();
        //1.先保存
        if (multipartFileList != null && !multipartFileList.isEmpty()) {
            for (MultipartFile multipartFile : CollectionUtils.emptyIfNull(multipartFileList)) {
                if (multipartFile != null && !multipartFile.isEmpty()) {
                    CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
                    CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(defaultRegionConfig.getRegionAlias());
                    String url = cloudClientUtil.upload(multipartFile, "feedback", defaultRegionConfig.getRegionAlias());
                    attachmentList.add(new Attachment()
                            .setFileUrl(url)
                            .setFileName(multipartFile.getOriginalFilename())
                            .setFileSize(multipartFile.getSize())
                            .setContentType(multipartFile.getContentType()));
                }
            }
            attachmentService.saveList(attachmentList);
        }
        String attachmentStr = CollectionUtils.emptyIfNull(attachmentList).stream().map(item -> item.getId().toString())
                .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
        saveItem(feedbackProcessItem.getFeedbackId(), feedbackProcessItem.getDetail(),
                InfTraceContextHolder.get().getUserName(), attachmentStr);
        String sendPerson = feedback.getChargePerson();
        if (StringUtils.isNotEmpty(feedback.getHandleBy())) {
            sendPerson = feedback.getHandleBy();
        }
        if (InfTraceContextHolder.get().getUserName().equals(feedback.getHandleBy())
                || InfTraceContextHolder.get().getUserName().equals(feedback.getChargePerson())) {
            sendPerson = feedback.getCreateBy();
        }
//        boolean isMaster = sendPerson.equals(feedback.getCreateBy());
//        dingDingService.notify(Collections.singletonList(sendPerson),
//                String.format("您好,工单编号：%s收到一条消息，请查看。 \n %s", feedback.getId(),
//                        serverHost + "admin/order/list"), isMaster);
        return getDetailInformation(feedbackProcessItem);
    }

    private void chekParam(FeedbackProcessItem feedbackProcessItem) {
        //不能发空消息
        if (StringUtils.isEmpty(feedbackProcessItem.getDetail()) &&
                feedbackProcessItem.getMultipartFileList() == null &&
                feedbackProcessItem.getMultipartFileList().isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_MESSAGE_ERR);
        }
    }

    @Cacheable(cacheNames = {"feedback"}, key = "#tenantName+'-'+#isPrivate+'-'+#dt")
    public String cacheDutyMan(String tenantName, Boolean isPrivate, String dt) {
        if (isPrivate) {
            String feedback = dutyInfoService.getDutyMan("feedback");
            InfTraceContextHolder.get().setTenantName(tenantName);
            return feedback;
        }
        return scmbService.getDutyMan();
    }

    public String getDutyMan() {
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        long utc_8Time = new Timestamp(System.currentTimeMillis()).getTime() + 28800000;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dt = format.format(utc_8Time);
        return cacheDutyMan(tenantName, isPrivate, dt);
    }
}
