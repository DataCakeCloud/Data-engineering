package com.ushareit.dstask.service.impl;

import cn.hutool.core.net.URLEncodeUtil;
import com.github.pagehelper.PageInfo;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.bean.meta.TaskUsage;
import com.ushareit.dstask.common.param.FeedbackSearchParam;
import com.ushareit.dstask.common.vo.FeedbackVO;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.FeedbackMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AttachmentService;
import com.ushareit.dstask.service.DutyInfoService;
import com.ushareit.dstask.service.FeedbackProcessItemService;
import com.ushareit.dstask.service.FeedbackService;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.third.scmp.SCMPService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.utils.RetryUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Service
public class FeedbackServiceImpl extends AbstractBaseServiceImpl<Feedback> implements FeedbackService {

    private static final Map<String, List<String>> MODULE_SHAREIDS = new HashMap<>(4);

    private static final List<String> CC_PERSON = new ArrayList<>();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FeedbackServiceImpl.class);

    private static String DINGDING_SECRET = DsTaskConstant.DINGDING_WORKORDER_SECRET;
    private static String DINGDING_WEBHOOK = DsTaskConstant.DINGDING_WORKORDER_WEBHOOK;

    private static String GROUP_NOTIFY_TEXT = "\uD83D\uDCE2响应超时工单，请@%s尽快处理\n工单号：%s \n紧急程度：%s \n提出人：%s    \n问题标题：【%s】 \n问题描述：%s";

    /**
     * 以下三个均以秒为单位
     */
    private static final long FIRST_TIME = 13 * 60;
    private static final long SECOND_TIME = 27 * 60;
    private static final long GAP = 10;

    /**
     * 毫秒单位
     */
    private static final long EIGHT_HOUR = 8 * 60 * 60 * 1000;


    static {
        if (StringUtils.isEmpty(InfTraceContextHolder.get().getEnv()) ||
                 DsTaskConstant.TEST.equalsIgnoreCase(InfTraceContextHolder.get().getEnv()) ||
                 DsTaskConstant.DEV.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())) {
            DINGDING_SECRET = DsTaskConstant.DINGDING_WORKORDER_SECRET_TEST;
            DINGDING_WEBHOOK = DsTaskConstant.DINGDING_WORKORDER_WEBHOOK_TEST;
        }

        // TODO 需添加
        List<String> de = Arrays.asList("licg");
        List<String> qe = Arrays.asList("tangjk");
        List<String> bi = Arrays.asList("wangsy1");
        List<String> lakeCat = Arrays.asList("sunlongjian", "hongyg");
        MODULE_SHAREIDS.put("QE", qe);
        MODULE_SHAREIDS.put("DE", de);
        MODULE_SHAREIDS.put("BI", bi);
        MODULE_SHAREIDS.put("LAKECAT", lakeCat);

        CC_PERSON.add("shilidong");
        CC_PERSON.add("xiari");
        CC_PERSON.add("licg");
    }

    @Value("${server-url.host}")
    private String serverHost;

    @Resource
    private FeedbackMapper feedbackMapper;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private SCMPService scmbService;

    @Resource
    private FeedbackProcessItemService feedbackProcessItemService;

    @Resource
    private DingDingService dingDingService;

    @Resource
    private FeedbackService feedbackService;

    @Resource
    private ActorServiceImpl actorService;

    @Resource
    private DutyInfoService dutyInfoService;

    @Resource
    private UserGroupServiceImpl userGroupServiceImpl;

    @Override
    public CrudMapper<Feedback> getBaseMapper() {
        return feedbackMapper;
    }

    @Scheduled(initialDelay = 10, fixedDelay = 600000)
    public void ScheduledMonitorFeedbackTimeout() {
//        feedbackService.monitorFeedbackTimeout();
    }

    /**
     * 异步+定时监控工单超时
     */
    @DisLock(key = "monitorFeedbackTimeout", expiredSeconds = 4, isRelease = false)
    @Override
    public void monitorFeedbackTimeout() {
        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        // TODO 需添加
        log.debug("monitorFeedbackTimeout...");
        String dutyMan = getDutyMan();
//        String dutyMan = "shilidong";
        CC_PERSON.add(0, dutyMan);

        List<Feedback> feedbacks = feedbackMapper.selectNonAcceptFeedbacks();
        long currentTime = System.currentTimeMillis();
        feedbacks.stream()
                .filter(feedback -> !(Feedback.NOTIFY.equals(feedback.getFirstNotify()) && Feedback.NOTIFY.equals(feedback.getSecondNotify())))
                .forEach(feedback -> notifyRobotAsync(currentTime, feedback, dutyMan));
    }

    @Override
    public List<TaskUsage> selectDayFeedbacks(String create, String start, String end) {
        return feedbackMapper.selectDayFeedbacks(create, start, end);
    }

    private void notifyRobotAsync(long currentTime, Feedback feedback, String dutyMan) {
        long createTime = feedback.getCreateTime().getTime();
        if (DsTaskConstant.DEV.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())) {
            createTime = createTime + EIGHT_HOUR;
            log.info("notifyRobotAsync...createTime:8,feedback id:" + feedback.getId());
        }
        long timeout = (currentTime - createTime) / 1000;

        if (timeout < FIRST_TIME) {
            return;
        }
        log.info("notifyRobotAsync..." + timeout + ",feedback id:" + feedback.getId());


        try {
            RetryUtil.asyncExecuteWithRetry((Callable<Void>) () -> {
                notifyRobotAndUpdateFeedback(timeout, dutyMan, CC_PERSON, feedback);
                return null;
            }, DsTaskConstant.MAX_JOB_CREATION_ATTEMPTS, 1000L, true);
        } catch (Exception e) {
        }
    }

    public void notifyRobotAndUpdateFeedback(long timeout, String dutyMan, List<String> notifyMen, Feedback feedback) {
        Boolean firstTimeFlag = timeout < SECOND_TIME || timeout >= FIRST_TIME;
        Boolean secondTimeFlag = timeout >= SECOND_TIME;

        if ((firstTimeFlag && Feedback.NOT_NOTIFY.equals(feedback.getFirstNotify()))) {
            notifyMen = Arrays.asList(dutyMan);
            feedback.setFirstNotify(Feedback.NOTIFY);
            log.info("feedback id:" + feedback.getId() + ",first:" + dutyMan + ",timeout=" + timeout);
        } else if ((secondTimeFlag && Feedback.NOT_NOTIFY.equals(feedback.getSecondNotify()))) {
            feedback.setSecondNotify(Feedback.NOTIFY);
            log.info("feedback id:" + feedback.getId() + ",second:" + dutyMan + ",timeout=" + timeout);
        } else {
            log.info("feedback id:" + feedback.getId() + ",no notify! timeout=" + timeout);
            return;
        }

       /* dingDingService.notifyRobot(String.format(GROUP_NOTIFY_TEXT,
                dutyMan,
                feedback.getId(),
                feedback.getFeedbackLevel(),
                feedback.getCreateBy(),
                feedback.getTitle(),
                feedback.getDescription()),
                DsTaskConstant.DINGDING_WORKORDER_SECRET,
                DsTaskConstant.DINGDING_WORKORDER_WEBHOOK,
                false,
                new ArrayList<>(),
                notifyMen
        );*/
        feedbackMapper.updateByPrimaryKeySelective(feedback);
        log.info("feedback id:" + feedback.getId() + ",notify success!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object save(Feedback feedback) {

        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        feedback.setTenantId(tenantId);
        String attachIds = attachmentService.saveList(feedback.getAttachmentList());
        feedback.setFirstAcceptTime(new Timestamp(9999999999999L));
        feedback.setFirstCloseTime(new Timestamp(9999999999999L));
        feedback.setFirstNotify(Feedback.NOT_NOTIFY);
        feedback.setSecondNotify(Feedback.NOT_NOTIFY);
        // TODO 需添加
        String database = feedback.getDatabase();


        feedback.setChargePerson(getDutyMan());
//        feedback.setChargePerson("wuyan");
        feedbackMapper.insertSelective(feedback.setAttachmentIds(attachIds));

        feedbackProcessItemService.saveItem(feedback.getId(), String.format("提交工单，标题【%s】,问题描述：%s",
                feedback.getTitle(), feedback.getDescription()), InfTraceContextHolder.get().getUserName(),attachIds);
        sendNotifyByApi();
//        UserGroup userGroup = userGroupServiceImpl.selectUserGroupByUuid(InfTraceContextHolder.get().getUuid());
//        Set<String> CurrentUserGroupOwners = userGroup.getUserGroupRelationList().stream()
//                .filter(data -> data.getOwner() == 1)
//                .map(UserGroupRelation::getUserName)
//                .collect(Collectors.toSet());
//
//        actorService.selectActorByDatabase(database)
//                .stream()
//                        .map(Actor::getGroups)
//                                .map(data->
//                                    userGroupServiceImpl.selectUserGroupById(Integer.parseInt(data)));

//        dingDingService.notifyCard(Collections.singletonList(feedback.getChargePerson()),
//                feedback.getId(),
//                String.format("工单号：%s  紧急程度：%s   \n提出人：%s   \n 问题标题：【%s】  \n 问题描述：%s  \n PC端处理链接：%s   \n 移动端处理：\n ",
//                        feedback.getId(),
//                        Objects.requireNonNull(FeedbackLevelEnum.of(feedback.getFeedbackLevel())).getLevelName(),
//                        feedback.getCreateBy(),
//                        feedback.getTitle(),
//                        feedback.getDescription(),
//                        serverHost + "admin/order/list"), true);
        feedbackProcessItemService.saveItem(feedback.getId(), String.format("已钉钉通知值班人【%s】",
                feedback.getChargePerson()), "system");

        return feedback.getId();
    }
    public void sendNotifyByApi(){
        log.info("发送第三方接口通知................");
    }

    @Override
    public Feedback getById(Object id) {
        Feedback feedback = super.getById(id);
        List<Attachment> attachmentList = new ArrayList<>();
        if (StringUtils.isNotEmpty(feedback.getAttachmentIds())) {
            attachmentList = attachmentService.listByIds(Arrays.stream(feedback.getAttachmentIds()
                    .split(SymbolEnum.COMMA.getSymbol())).map(Integer::parseInt));
        }
        return feedback.setAttachmentList(attachmentList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(Integer feedbackId, String source, String accept) {
        Feedback feedback = checkAndGet(feedbackId);
        // 防止重复受理
        if (FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.UN_ACCEPT) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只能受理未处理的工单，请核实");
        }

        if (StringUtils.isEmpty(source)) {
            if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())
                    && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), getDutyMan())
                    && !InfTraceContextHolder.get().getAdmin()) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有处理人或值班人或管理员能受理工单，请核实");
            }
        }

        accept = StringUtils.isEmpty(accept) ? InfTraceContextHolder.get().getUserName() : accept;

        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedbackId);
        toUpdateParam.setHandleBy(accept);
        toUpdateParam.setStatus(FeedbackStatusEnum.ACCEPTED.name());
        if (feedback.getFirstAcceptTime().getTime() > (new Timestamp(System.currentTimeMillis()).getTime())) {
            toUpdateParam.setFirstAcceptTime(new Timestamp(System.currentTimeMillis()));
        }
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);
        feedbackProcessItemService.saveItem(feedbackId, "工单被受理", accept);

        //发给 对应租户下的用户
//        dingDingService.notify(Collections.singletonList(feedback.getCreateBy()),
//                String.format("%s您好，你提交的工单《%s》已被DataCake工作人员%s响应，" +
//                                "请知悉～如您很着急工单被处理，可以通过钉钉联系%s，谢谢。 \n %s", feedback.getCreateBy(),
//                        feedback.getTitle(), accept, accept,
//                        serverHost + "admin/order/list"), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Integer feedbackId, String assignee, String reason, String module) {
        if (StringUtils.isNotEmpty(module)) {
            assignee = getRandomAssignee(module);
        }

        if (StringUtils.isEmpty(reason)) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_ASSIGN_REASON_ERR);
        }
        Feedback feedback = checkAndGet(feedbackId);
        if (StringUtils.isEmpty(module)) {
            if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())
                    && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), getDutyMan())
                    && !InfTraceContextHolder.get().getAdmin()) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有处理人或值班人或管理员能转让工单，请核实");
            }
        }

        if (FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.UN_ACCEPT
                && FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.ACCEPTED) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只能转让未受理及已受理的工单，请核实");
        }

        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedbackId);
        toUpdateParam.setHandleBy(assignee);
        if (feedback.getFirstAcceptTime().getTime() > (new Timestamp(System.currentTimeMillis()).getTime())) {
            toUpdateParam.setFirstAcceptTime(new Timestamp(System.currentTimeMillis()));
        }
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);
        feedbackProcessItemService.saveItem(feedbackId, String.format("工单被转让给处理人：%s,转让原因：%s", assignee, reason),
                InfTraceContextHolder.get().getUserName());

        //内部转让
//        dingDingService.notifyCard(Collections.singletonList(assignee),
//                feedback.getId(),
//                String.format("工单号：%s  紧急程度：%s   \n提出人：%s   \n 问题标题：【%s】  \n 问题描述：%s  \n PC端处理链接：%s   \n 移动端处理：\n ",
//                        feedback.getId(),
//                        Objects.requireNonNull(FeedbackLevelEnum.of(feedback.getFeedbackLevel())).getLevelName(),
//                        feedback.getCreateBy(),
//                        feedback.getTitle(),
//                        feedback.getDescription(),
//                        serverHost + "admin/order/list"), true);
    }

    private String getRandomAssignee(String module) {
        List<String> shareIds = MODULE_SHAREIDS.get(module);
        if (shareIds.size() == 1) {
            return shareIds.get(0);
        }

        int random = (int)(Math.random() * shareIds.size());
        return shareIds.get(random);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(Integer feedbackId, String reason) {
        Feedback feedback = checkAndGet(feedbackId);
        if (FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.ACCEPTED) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有已受理的工单可以更新解决方案，请核实");
        }

        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有受理人可以更新解决方案，请核实");
        }

        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedbackId);
        toUpdateParam.setResolveReply(reason);
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);
        feedbackProcessItemService.saveItem(feedbackId, StringUtils.isBlank(feedback.getResolveReply()) ?
                        String.format("工单添加解决方案：%s", reason) : String.format("工单更新解决方案：%s", reason),
                InfTraceContextHolder.get().getUserName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Integer feedbackId, String reason) {
        Feedback feedback = checkAndGet(feedbackId);
        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), getDutyMan())
                && !InfTraceContextHolder.get().getAdmin()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有提交人、处理人、值班人或管理员能关闭工单，请核实");
        }

        if (FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.UN_ACCEPT
                && FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.ACCEPTED) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只能关闭未受理及已受理的工单，请核实");
        }

        // 工单发起人关闭工单
        if (StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())) {
            feedbackProcessItemService.saveItem(feedbackId, String.format("工单被创建者关闭，原因是: %s", reason),
                    InfTraceContextHolder.get().getUserName());
        } else if (StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())) {
            feedbackProcessItemService.saveItem(feedbackId, String.format("工单已解决，解决方案是: %s", reason),
                    InfTraceContextHolder.get().getUserName());

            String url = serverHost + "admin/order/list";
            //发给用户
//            dingDingService.notify(Collections.singletonList(feedback.getCreateBy()),
//                    String.format("工单《%s》已被DataCake工作人员关闭并回复解决方案，请您登录DataCake平台查看 \n %s ，" +
//                                    "如您感觉满意，点击下方链接，给予评价 \n %s", feedback.getTitle(),
//                            url, url + "?feedbackId=" + feedbackId), false);
        } else {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有创建人及受理人能关闭工单");
        }

        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedbackId);
        toUpdateParam.setStatus(FeedbackStatusEnum.SOLVED.name());
        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        if (feedback.getFirstCloseTime().getTime() > nowTimestamp.getTime()) {
            toUpdateParam.setFirstCloseTime(nowTimestamp);
        }
        if (feedback.getFirstAcceptTime().getTime() > nowTimestamp.getTime()) {
            toUpdateParam.setFirstAcceptTime(nowTimestamp);
        }
        toUpdateParam.setResolveReply(reason);
        toUpdateParam.setResolveTime(new Date());
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);
    }

    @Override
    public Feedback reopen(FeedbackProcessItem param) {
        Feedback feedback = checkAndGet(param.getFeedbackId());
        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_USER_REOPEN_FAILURE);
        }
        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedback.getId());
        toUpdateParam.setStatus(FeedbackStatusEnum.ACCEPTED.name());
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);
        feedbackProcessItemService.saveItem(param.getFeedbackId(), "工单已被重新打开",
                InfTraceContextHolder.get().getUserName());
        //钉钉通知
        String sendPerson = feedback.getHandleBy();
        if (StringUtils.isEmpty(feedback.getHandleBy())) {
            sendPerson = feedback.getChargePerson();
        }
//        dingDingService.notify(Collections.singletonList(sendPerson),
//                String.format("[%s]号工单《%s》已被用户重开，请尽快查看问题。 \n %s", feedback.getId(), feedback.getTitle(),
//                        serverHost + "admin/order/list"), true);
        return feedbackProcessItemService.getDetailInformation(param);
    }

    @Override
    public Feedback updateAndSelect(Feedback param) {
        Feedback feedback = checkAndGet(param.getId());
        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getHandleBy())
                && !StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), getDutyMan())
                && !InfTraceContextHolder.get().getAdmin()) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_USER_UPDATE_FAILURE);
        }
        Feedback updateFeedback = new Feedback();
        updateFeedback.setId(feedback.getId());
        StringBuilder stringBuilder = new StringBuilder("工单信息更新:");
        boolean flag = false;
        if (StringUtils.isNotEmpty(feedback.getModule()) && StringUtils.isNotEmpty(param.getModule()) &&
                !feedback.getModule().equals(param.getModule())) {
            updateFeedback.setModule(param.getModule());
            feedback.setModule(param.getModule());
            flag = true;
            stringBuilder.append("所属产品:").append(param.getModule());
        }
        if (StringUtils.isNotEmpty(feedback.getType()) && StringUtils.isNotEmpty(param.getType()) &&
                !feedback.getType().equals(param.getType())) {
            updateFeedback.setType(param.getType());
            flag = true;
            feedback.setType(param.getType());
            stringBuilder.append("问题分类:").append(param.getType());
        }
        if (flag) {
            feedbackMapper.updateByPrimaryKeySelective(updateFeedback);
            feedbackProcessItemService.saveItem(param.getId(), stringBuilder.toString(),
                    InfTraceContextHolder.get().getUserName());
        }
        FeedbackProcessItem feedbackProcessItem = new FeedbackProcessItem();
        feedbackProcessItem.setFeedbackId(param.getId());
        feedbackProcessItem.setMaxId(param.getMaxId());
        return feedbackProcessItemService.getDetailInformation(feedbackProcessItem);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void score(Integer feedbackId, int score) {
        Feedback feedback = checkAndGet(feedbackId);
        if (!StringUtils.equalsIgnoreCase(InfTraceContextHolder.get().getUserName(), feedback.getCreateBy())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有创建者可以打分");
        }
        if (FeedbackStatusEnum.of(feedback.getStatus()) != FeedbackStatusEnum.SOLVED) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "只有已完成状态的工单可以打分，请核实");
        }

        Feedback toUpdateParam = new Feedback();
        toUpdateParam.setId(feedbackId);
        toUpdateParam.setScore(score);
        toUpdateParam.setStatus(FeedbackStatusEnum.SCORED.name());
        feedbackMapper.updateByPrimaryKeySelective(toUpdateParam);

        //发给平台
        feedbackProcessItemService.saveItem(feedbackId, String.format("工单被评分，分值：%s", score),
                InfTraceContextHolder.get().getUserName());
    }

    @Override
    public PageInfo<FeedbackVO> page(Integer pageNum, Integer pageSize, Example param) {
        PageInfo<Feedback> pageInfo = super.listByPage(pageNum, pageSize, param);

        String currentName = InfTraceContextHolder.get().getUserName();
        Stream<Integer> attachIds = pageInfo.getList().stream()
                .map(feedback -> {
                    Timestamp noewTime = new Timestamp(System.currentTimeMillis());
                    Timestamp firstCloseTime = feedback.getFirstCloseTime().getTime() > noewTime.getTime() ? noewTime : feedback.getFirstCloseTime();
                    Timestamp firstAcceptTime = feedback.getFirstAcceptTime().getTime() > noewTime.getTime() ? noewTime : feedback.getFirstAcceptTime();
                    Timestamp createTime = feedback.getCreateTime();
                    UserGroup userGroup = userGroupServiceImpl.selectUserGroupByUuid(feedback.getUserGroup());
                    if(userGroup != null){
                        feedback.setUserGroup(userGroup.getName());
                    }
                    feedback.setFirstAcceptDuration(getMinSend(firstAcceptTime.getTime() - createTime.getTime()));
                    feedback.setFirstCloseDuration(getMinSend(firstCloseTime.getTime() - createTime.getTime()));
                    feedback.setFeedbackLevel(Objects.requireNonNull(FeedbackLevelEnum.of(feedback.getFeedbackLevel())).getLevelName());
                    if (StringUtils.isNotEmpty(feedback.getPageUri()) && feedback.getPageUri().contains("http")) {
                        feedback.setPageUri("-");
                    }
                    if (!StringUtils.equalsIgnoreCase(currentName, feedback.getCreateBy())
                            && !StringUtils.equalsIgnoreCase(currentName, feedback.getHandleBy())
                            && !StringUtils.equalsIgnoreCase(currentName, feedback.getChargePerson())) {
                        feedback.setSendCloseFlag(false);
                    }
                    return feedback;
                })
                .flatMap(item -> Arrays.stream(item.getAttachmentIds().split(SymbolEnum.COMMA.getSymbol())))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(Integer::parseInt);

        Map<Integer, Attachment> attachmentMap = attachmentService.listByIds(attachIds)
                .stream()
                .collect(HashMap::new, (m, item) -> m.put(item.getId(), item), HashMap::putAll);
        return PageUtils.map(pageInfo, item -> new FeedbackVO(item, attachmentMap));
    }

    public String getMinSend(long ms) {
        if (ms < 0) {
            ms = ms + 28800000L;
        }
        long ONE_HOUR = 3600000L;
        long ONE_DAY = 86400000L;
        int mi = 60;
        int hh = 60 * 60;
        int dd = 60 * 60 * 24;
        if (ms > ONE_DAY ) {
            long acceptDay = (ms / 1000) / dd;
            long acceptHour = ((ms / 1000) % dd) / hh;
            long acceptMinutes = (((ms / 1000) % dd) % hh) / mi;
            return String.format("%s天%s时%s分",acceptDay,acceptHour, acceptMinutes);
        }
        if (ms > ONE_HOUR) {
            long acceptHour = (ms / 1000)  / hh;
            long acceptMinutes = ((ms / 1000) % hh) / mi;
            long acceptSend = ((ms / 1000) % hh) % mi;
            return String.format("%s时%s分%s秒",acceptHour, acceptMinutes,acceptSend);
        }
        long acceptMinutes = (ms / 1000)/ mi;
        long acceptSend = (ms / 1000) % mi;
        return String.format("%s分%s秒", acceptMinutes, acceptSend);
    }

    private Feedback checkAndGet(Integer feedbackId) {
        Feedback feedback = feedbackMapper.selectByPrimaryKey(feedbackId);
        if (feedback == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工单信息不存在，请核实");
        }

        return feedback;
    }

    @Override
    public void export(FeedbackSearchParam param, HttpServletResponse response) {
        List<FeedbackVO> feedbackList = page(0, Integer.MAX_VALUE, param.toExample()).getList();
        if (feedbackList == null || feedbackList.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_EXPORT_MESSAGE_ERR);
        }
        //拼接 详情信息，如果是附件就是url
        List<Integer> detailId = feedbackList.stream().map(FeedbackVO::getId).collect(Collectors.toList());
        Map<Integer, List<FeedbackProcessItem>> feedbackProcessItemMap = feedbackProcessItemService.getItemListByFids(detailId)
                .stream().collect(Collectors.groupingBy(FeedbackProcessItem::getFeedbackId));

        List<FeedbackProcessItem> feedbackProcessItemList = new ArrayList<>();
        List<List<FeedbackProcessItem>> allList = new ArrayList<>(feedbackProcessItemMap.values());
        for (List<FeedbackProcessItem> list : allList) {
            feedbackProcessItemList.addAll(list);
        }
        List<Integer> attachmentIds = feedbackProcessItemList.stream()
                .flatMap(data -> Arrays.stream(data.getAttachmentIds().split(SymbolEnum.COMMA.getSymbol())))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(Integer::parseInt).collect(Collectors.toList());
        Map<String, Attachment> attachmentMap = attachmentService.listByIds(attachmentIds)
                .stream()
                .collect(HashMap::new, (m, item) -> m.put(item.getId().toString(), item), HashMap::putAll);
        //遍历collect 改变描述后加url;
        for (Integer key : feedbackProcessItemMap.keySet()) {
            List<FeedbackProcessItem> list = feedbackProcessItemMap.get(key);
            for (FeedbackProcessItem feedbackProcessItem : list) {
                String attIds = feedbackProcessItem.getAttachmentIds();
                if (StringUtils.isEmpty(attIds)) {
                    continue;
                }
                String[] attIdArr = attIds.split(SymbolEnum.COMMA.getSymbol());
                StringBuilder stringBuilder = new StringBuilder();
                for (String attid : attIdArr) {
                    stringBuilder.append(attachmentMap.get(attid).getFileUrl());
                    if (!attid.equals(attIdArr[attIdArr.length - 1])) {
                        stringBuilder.append(";");
                    }
                }
                feedbackProcessItem.setFileUrl(stringBuilder.toString());
            }
        }

        for (FeedbackVO feedback : feedbackList) {
            feedback.setStatus(Objects.requireNonNull(FeedbackStatusEnum.of(feedback.getStatus())).getDesc());
            List<FeedbackProcessItem> list = feedbackProcessItemMap.get(feedback.getId());
            if (list == null || list.isEmpty()) {
                continue;
            }
            String allDetail = "";
            for (FeedbackProcessItem item : list) {
                String join = joinString(item.getDetail(), item.getFileUrl());
                allDetail = joinString(allDetail, join);
            }
            if (!list.isEmpty()) {
                feedback.setAllDetail(allDetail);
            }
        }
        try {
            String fileName = "工单详情.xls";
            Workbook wb = buildWorkBook();
            writeWorkBook(wb, feedbackList);
            writeToResponse(fileName, wb, response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException(BaseResponseCodeEnum.FEEDBACK_EXPORT_MESSAGE_FAILURE);
        }
    }

    public String joinString(String frontStr, String behindStr) {
        if (StringUtils.isEmpty(frontStr) && StringUtils.isEmpty(behindStr)) {
            return "";
        }
        if (StringUtils.isNotEmpty(frontStr) && StringUtils.isEmpty(behindStr)) {
            return frontStr;
        }
        if (StringUtils.isEmpty(frontStr) && StringUtils.isNotEmpty(behindStr)) {
            return behindStr;
        }
        return frontStr + ";" + behindStr;
    }


    protected void writeWorkBook(Workbook wb, List<FeedbackVO> dataList) {
        if (CollectionUtils.isNotEmpty(dataList)) {
            Sheet sheet = wb.getSheetAt(0);
            int lineNum = 0;
            Object[] headFeild = getHeadFeild();
            for (FeedbackVO data : dataList) {
                int i = 0;
                Object[] values;
                XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
                Row row = sheet.createRow(lineNum + 1);
                values = new Object[headFeild.length];
                values[i++] = data.getId();
                values[i++] = data.getModule();
                values[i++] = data.getType();
                values[i++] = data.getTitle();
                values[i++] = data.getTaskId();
                values[i++] = data.getCreateBy();
                values[i++] = data.getFeedbackLevel();
                values[i++] = data.getStatus();
                values[i++] = data.getChargePerson();
                values[i++] = data.getHandleBy();
                values[i++] = data.getCreateTime();
                values[i++] = data.getFirstAcceptDuration();
                values[i++] = data.getFirstCloseDuration();
                values[i++] = data.getAllDetail();
                lineNum++;
                fillRow(row, values, cellStyle);
            }
        }
    }

    protected Workbook buildWorkBook() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        sheet.setDefaultColumnWidth(15);
        XSSFCellStyle headTitleStyle = (XSSFCellStyle) wb.createCellStyle();
        headTitleStyle.setWrapText(true);
        Object[] titles = getHeadFeild();
        Row titleRow = sheet.createRow(0);
        fillRow(titleRow, titles, headTitleStyle);
        return wb;
    }

    /**
     * 填充行的方法
     */
    protected void fillRow(Row row, Object[] values, CellStyle cellStyle) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(String.valueOf(values[i]));
                cell.setCellStyle(cellStyle);
            }
        }
    }

    public static Object[] getHeadFeild() {
        return new Object[]{"工单编号", "产品模块", "问题分类", "问题标题", "任务ID", "提交用户",
                "紧急程度", "状态", "值班人", "处理人", "提交时间", "响应时间", "解决时间", "明细详情"};
    }

    /**
     * 下载导出文件
     */
    protected void writeToResponse(String fileName, Workbook wb, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", String.format("attachment;filename=%s;filename*=utf-8''%s",
                URLEncodeUtil.encode(fileName), URLEncodeUtil.encode(fileName)));

        wb.write(response.getOutputStream());
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
        log.info(" tenantName is : " + tenantName + " isPrivate is :" + isPrivate + " dt is : " + dt);
        return cacheDutyMan(tenantName, isPrivate, dt);
    }

}
