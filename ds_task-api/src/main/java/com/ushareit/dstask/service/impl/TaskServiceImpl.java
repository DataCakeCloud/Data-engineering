package com.ushareit.dstask.service.impl;

import cn.hutool.core.net.URLEncodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.api.TaskSchedulerApi;
import com.ushareit.dstask.api.TaskSchedulerRpcApiGrpc;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.format.TaskFormatter;
import com.ushareit.dstask.common.vo.TaskFolderRelationVo;
import com.ushareit.dstask.common.vo.TaskNameVO;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.FileManagerMapper;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.validator.ValidatorService;
import com.ushareit.dstask.validator.impl.BaseTaskParamValidator;
import com.ushareit.dstask.web.ddl.DdlFactory;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.model.KafkaDdl;
import com.ushareit.dstask.web.ddl.model.MetisDdl;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.JobFactory;
import com.ushareit.dstask.web.factory.Submitter;
import com.ushareit.dstask.web.factory.flink.job.FlinkBaseJob;
import com.ushareit.dstask.web.factory.flink.job.FlinkSqlJob;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.flink.submitter.AutoScaleSubmitter;
import com.ushareit.dstask.web.factory.flink.submitter.FlinkBatchSubmitter;
import com.ushareit.dstask.web.factory.flink.submitter.K8sNativeSubmitter;
import com.ushareit.dstask.web.factory.scheduled.OfflineSubmitter;
import com.ushareit.dstask.web.factory.scheduled.ScheduledJob;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import com.ushareit.dstask.web.utils.*;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.flink.api.common.JobStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class TaskServiceImpl extends AbstractBaseServiceImpl<Task> implements TaskService {

    public static final String[] TASK_RESULT = {"id", "name",
            "templateCode", "createBy", "last7Status", "inputGuids", "outputGuids", "updateTime", "online"};
    @GrpcClient("pipeline-server")
    private static TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub;
    @Resource
    public SchedulerServiceImpl schedulerService;
    @Resource
    public TaskVersionService taskVersionService;
    @Resource
    public TaskInstanceService taskInstanceService;
    @Resource
    public HiveTablesService hiveTablesService;
    @Resource
    public TaskSnapshotService taskSnapshotService;
    @Resource
    public FlinkClusterService flinkClusterService;
    @Resource
    public AccumulateOnlineTaskService accumulateOnlineTaskService;
    @Resource
    public AccumulateUserService accumulateUserService;
    @Resource
    public LabelService labelService;
    @Resource
    public TemplateRegionImpService templateRegionImpService;
    @Resource
    private FileManagerServiceImpl fileManagerServiceImpl;
    @Resource
    public DingDingService dingDingService;
    @Resource
    public OperateLogService operateLogService;
    @Resource
    public TaskScaleStrategyService taskScaleStrategyService;
    @Resource
    public AccessGroupService accessGroupService;
    @Resource
    public AccessTenantService accessTenantService;
    @Resource
    public TaskMapper taskMapper;
    @Resource
    private ArtifactVersionService artifactVersionService;
    @Resource
    public AccessUserService accessUserService;
    @Resource
    private TaskService taskService;
    @Resource
    private CostAllocationService costAllocationService;
    @Resource
    private ValidatorService validatorService;
    @Resource
    public AlarmNoticeUtil alarmNoticeUtil;
    @Resource
    public SysDictService sysDictService;
    @Resource
    public CloudFactory cloudFactory;
    @Resource
    public ScmpUtil scmpUtil;
    @Resource
    public MavenUtil mavenUtil;
    @Resource
    public OlapGateWayUtil olapGateWayUtil;
    @Resource
    public UserGroupServiceImpl userGroupServiceImpl;
    @Resource
    public UserGroupService userGroupService;

    @Resource
    private EmailUtils emailUtils;

    @Resource
    private TaskFolderService taskFolderService;

    @Getter
    @Value("${gateway.url}")
    private String gatewayUrl;
    @Value("${log.url.gcsPrefix}")
    private String GCSBucketPrefix;
    @Value("${log.url.ksPrefix}")
    private String KSBucketPrefix;
    @Value("${log.url.s3Prefix}")
    private String S3BucketPrefix;
    @Resource
    public CloudResourcesService cloudResourcesService;
    @Resource
    public ActorService actorService;
    @Resource
    public MetaDataService metaDataService;

    @Resource
    public Lakecatutil lakecatutil;

    @Resource
    public ActorDefinitionService actorDefinitionService;

    @Resource
    public FileManagerMapper fileManagerMapper;

    public String getTransformedRegion(String region) {
        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<DataCakeRegionProperties.RegionConfig> configurations = DataCakeConfigUtil.getDataCakeRegionProperties().getConfigurations();
        HashMap<String, String> regionEnum = new HashMap<>();
        for (CloudResouce.DataResource regionConfig : cloudResource.getList()) {
            regionEnum.put(regionConfig.getRegionAlias(), regionConfig.getRegion());
        }
        return regionEnum.get(region);
    }

    public static long getOffsetByGranularity(String date, String granularity) {
        System.out.println(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
        long offset;
        try {
            Date parse = simpleDateFormat.parse(date);
            offset = (parse.getTime() - simpleDateFormat.parse(simpleDateFormat.format(new Date())).getTime()) / (1000 * 3600);
        } catch (ParseException e) {
            return Integer.MAX_VALUE;
        }

        switch (granularity) {
            case "daily":
                offset = (long) Math.floor((double) offset / 24);
                break;
            case "weekly":
                offset = (long) Math.floor((double) offset / (7 * 24));
                break;
            case "monthly":
                offset = (long) Math.floor((double) offset / (31 * 24));
                break;
        }
        return offset;

    }

    public EmailUtils getEmailUtils(){
        return emailUtils;
    }

    public String getNamespace(){
        return DataCakeConfigUtil.getDataCakeConfig().namespace;
    }

    public static String readStream(InputStream inStream) throws Exception {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(inputStreamReader);
        String str;
        StringBuilder stringBuffer = new StringBuilder();
        while ((str = bufferedReader.readLine()) != null) {
            stringBuffer.append(str).append("\n");
        }
        return stringBuffer.toString();
    }

    @Override
    public CrudMapper<Task> getBaseMapper() {
        return taskMapper;
    }

    @Scheduled(cron = "55 59 23 * * ?")
    public void scheduledStatics() {
        taskService.statics();
    }

    /**
     * 统计累计上线任务数和用户数 定时任务
     */
    @MultiTenant
    @DisLock(key = "statics", expiredSeconds = 10 * 60 * 60, isRelease = false)
    public void statics() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String current = formatter.format(date);
        log.info("statics current date is " + current);

        Integer accumulativeTasks = taskMapper.getAccumulativeTasks(current);
        AccumulateOnlineTask accumulateOnlineTask = new AccumulateOnlineTask(current, accumulativeTasks);
        accumulateOnlineTaskService.save(accumulateOnlineTask);

        Integer accumulativeUser = taskMapper.getAccumulativeUser(current);
        AccumulateUser accumulateUser = new AccumulateUser(current, accumulativeUser);
        accumulateUserService.save(accumulateUser);
    }

    @Override
    public List<Task> selectDayOnlinedTasks(String start, String end, String userGroup) {
        userGroup=StringUtils.isBlank(userGroup)?null:userGroup;
        return taskMapper.selectDayOnlinedTasks(start, end, userGroup);
    }

    @Override
    public List<Task> sumAllOnlinedTasks(String userGroup) {
        userGroup=StringUtils.isBlank(userGroup)?null:userGroup;
        return taskMapper.sumAllOnlinedTasks(userGroup);
    }

    @Override
    public void deleteAndNotify(Integer id, Boolean ifNotify) {
        Task task = checkExist(id);
        //TODO 实时任务：正在运行不可删除；离线任务：删除需同步pipeline
        if (isStreaming(task)) {

        } else {
            TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.taskDelete(
                    TaskSchedulerApi.DeleteTaskRequest
                            .newBuilder()
                            .setName(task.getName())
                            .build()
            );

            Integer code = taskCommonResponse.getCode();
            if (code != 0 && code != 3) {
                String msg = taskCommonResponse.getMessage();
                throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
            }

            //任务下线，通知直接下游逻辑
            if (ifNotify) {
                List<Task> tasks = taskMapper.selectChildDependendcies(task.getId(), task.getOutputGuids());
                List<Task> list = taskMapper.selectChildrenByEventDepends(task.getId());
                tasks.addAll(list);
                Set<String> notifySet = new HashSet<>();
                for (Task perTask : tasks) {
                    notifySet.add(perTask.getCreateBy());
                    if (StringUtils.isNotEmpty(perTask.getCollaborators())) {
                        notifySet.addAll(Arrays.asList(perTask.getCollaborators().split(",")));
                    }
                }
                String message = "任务：" + task.getName() + ",被" + InfTraceContextHolder.get().getUserName() + "删除，请确定是否对下游任务有影响。";
                dingDingService.notify(new ArrayList<>(notifySet), message);
                log.info("删除通知：" + message + " 人数：" + notifySet.size());
            }
        }

        task.setDeleteStatus(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(task);
        // 删除文件夹中的任务
        fileManagerServiceImpl.deleteByEntityId(id,FileManager.Module.TASK.name());
    }

    @Override
    public List<Task> downstreamTask(Integer id) {
        Task task = checkExist(id);
        List<Task> tasks = taskMapper.selectChildDependendcies(task.getId(), task.getOutputGuids());
        List<Task> list = taskMapper.selectChildrenByEventDepends(task.getId());
        tasks.addAll(list);
        return tasks.stream().map(data -> {
            if (StringUtils.isNotEmpty(data.getUserGroup())) {
                UserGroup build = new UserGroup();
                build.setUuid(data.getUserGroup());
                UserGroup userGroup = userGroupService.selectOne(build);
                data.setUserGroup(userGroup.getName());
            }
            return data;
        }).collect(Collectors
                .collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Task::getId))),
                        ArrayList::new));
    }


    @Override
    public Task checkExist(Object id) {
        Task task = getById(id);
        if (task == null || task.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_DELETE);
        }
        return task;
    }

    @Override
    public List<Task> checkExist(List<Integer> taskIds) {
        List<Task> tasks = taskMapper.selectWithIds(taskIds);
        if (tasks.size() != taskIds.size()) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "有任务已删除，请检查");
        }

        tasks.forEach(task -> {
            setCanEdit(task);
        });
        return tasks;
    }

    @Override
    public void changeStateCode(Task task, String status) {
        log.info("changeStateCode, task id is " + task.getId() + ", status is " + status);
        Task toUpdateParam = new Task();
        toUpdateParam.setId(task.getId());
        toUpdateParam.setUpdateBy(task.getUpdateBy());
        toUpdateParam.setUpdateTime(task.getUpdateTime());
        toUpdateParam.setStatusCode(status);

        if (InfTraceContextHolder.get().getUserName() != null) {
            toUpdateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
        }
        super.update(toUpdateParam);
    }

    @Override
    public Object save(Task taskFromWeb) {
        // 格式化 task 数据
        TaskFormatter.formatFromWeb(taskFromWeb);

        // 校验各项参数
        validatorService.validTask(taskFromWeb);

        addDefaultUserGroup(taskFromWeb);

        super.save(taskFromWeb);

        if(taskFromWeb.getFolderId()!=null){
            // 任务归属文件夹，添加到文件管理表中
            FileManager fm = new FileManager();
            fm.setName(taskFromWeb.getName())
                    .setModule(FileManager.Module.TASK.name())
                    .setParentId(taskFromWeb.getFolderId())
                    .setUserGroup(taskFromWeb.getUserGroup())
                    .setIsFolder(false)
                    .setEntityId(taskFromWeb.getId());
            fileManagerServiceImpl.add(fm);
        }

        Task taskRes = super.getByName(taskFromWeb.getName());
        if (taskFromWeb.getAuditStatus() != null &&
                taskFromWeb.getAuditStatus().equals(BaseActionCodeEnum.CREATEANDSTART.name())) {
            addTaskVersionAndAudit(taskRes, BaseActionCodeEnum.CREATEANDSTART.name());
        } else {
            addTaskVersionAndAudit(taskRes, BaseActionCodeEnum.CREATE.name());
        }

        //判断是否是实时的经济模式 保存对应的策略
        addStreamingStrategy(taskFromWeb);

//        costAllocationService.updateCostAllocation(taskFromWeb);
        return taskFromWeb;
    }


    public static void addDefaultUserGroup(Task taskFromWeb) {

        String runtimeConfig = taskFromWeb.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        String currentGroup = InfTraceContextHolder.get().getGroupId();
        log.info(" current user group is :" + currentGroup);
        if (StringUtils.isNotEmpty(currentGroup)) {
            String advancedParameters = runtimeConfigObject.getString("advancedParameters");
            if (advancedParameters != null) {
                JSONObject advancedParametersObject = JSON.parseObject(advancedParameters);
                processParamGroup(advancedParametersObject, currentGroup);
                runtimeConfigObject.put("advancedParameters",advancedParametersObject);
            } else {
                processParamGroup(runtimeConfigObject, currentGroup);
            }
        }
        taskFromWeb.setRuntimeConfig(JSONObject.toJSONString(runtimeConfigObject));
    }


    public static void processParamGroup(JSONObject jsonObject, String currentGroup) {
        String dsGroups = jsonObject.getString("dsGroups");
        String[] split = currentGroup.split(",");
        List<Integer> newGroupIds = Arrays.stream(split).collect(Collectors.toList())
                .stream().map(Integer::parseInt).collect(Collectors.toList());

        if (StringUtils.isEmpty(dsGroups) || "[]".equalsIgnoreCase(dsGroups)) {
            jsonObject.put("dsGroups", newGroupIds.toArray());
        }

//        List<Integer> groupIds = JSON.parseArray(dsGroups, Integer.class);
//        if (!newGroupIds.isEmpty()) {
//            groupIds.addAll(newGroupIds);
//            jsonObject.put("dsGroups", groupIds.toArray());
//        }
    }

    @Override
    public void update(Task taskFromWeb) {
        //1.ID不为空校验
        if (taskFromWeb == null || taskFromWeb.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.FAILED_UPDATE);
        }

        Task taskFromDb = super.getById(taskFromWeb.getId());
        String uuid = InfTraceContextHolder.get().getUuid();
        if (StringUtils.isNotEmpty(taskFromDb.getUserGroup()) && !taskFromDb.getUserGroup().equals(uuid)) {
            throw new ServiceException(BaseResponseCodeEnum.UPDATE_FAIL);
        }

        taskFromWeb.setTemplateCode(taskFromDb.getTemplateCode());
        taskFromWeb.setTenancyCode(taskFromDb.getTenancyCode());
        taskFromWeb.setCreateBy(taskFromDb.getCreateBy());
        taskFromWeb.setCreateTime(taskFromDb.getCreateTime());
        taskFromWeb.setOnline(taskFromDb.getOnline());

        // 格式化 task 数据
        TaskFormatter.formatFromWeb(taskFromWeb);

        // 校验各项参数
        validatorService.validTask(taskFromWeb);

        addStreamingStrategy(taskFromWeb);

        // 文件管理相关的修改
        if (taskFromWeb.getFolderId() != null){
            FileManager fm = fileManagerMapper.selectByEntityId(taskFromWeb.getId(), FileManager.Module.TASK.name());
            if(!fm.getParentId().equals(taskFromWeb.getFolderId())){
                // 如果任务归属的文件夹发生更改，需要更新文件管理表中的数据
                fileManagerServiceImpl.move(fm.getId(),fm.getParentId(),taskFromWeb.getFolderId());
            }

            if(!taskFromWeb.getName().equals(taskFromDb.getName())){
                fm.setName(taskFromWeb.getName());
                // 如果任务名称修改了，需要更新文件管理表中的文件名
                fileManagerServiceImpl.update(fm);
            }
        }

//        costAllocationService.updateCostAllocation(taskFromWeb);

        Integer version;
        if (!InfTraceContextHolder.get().getUserName().equals("system")
                || (InfTraceContextHolder.get().getUserName().equals("system") &&
                StringUtils.isNotEmpty(taskFromWeb.getAuditStatus()) &&
                taskFromWeb.getAuditStatus().equals(BaseActionCodeEnum.UPDATEANDSTART.name()))) {
            if (StringUtils.isNotEmpty(taskFromWeb.getAuditStatus()) &&
                    taskFromWeb.getAuditStatus().equals(BaseActionCodeEnum.UPDATEANDSTART.name())) {
                version = addTaskVersionAndAudit(taskFromWeb, BaseActionCodeEnum.UPDATEANDSTART.name());
            } else {
                version = addTaskVersionAndAudit(taskFromWeb, BaseActionCodeEnum.UPDATE.name());
            }
            //1.实时更新处理
            //2.批任务更新处理   -->对任务进行更新 增加了版本实例 但同时需要更新任务的任务更新时间
            //3.批任务更新并启动处理
            if (isStreaming(taskFromWeb)) {
                taskFromWeb.setCurrentVersion(version);
                super.update(taskFromWeb);
            } else {
                if (taskFromWeb.getAuditStatus() != null &&
                        taskFromWeb.getAuditStatus().equals(BaseActionCodeEnum.UPDATEANDSTART.name())) {
                    taskFromWeb.setCurrentVersion(version);
                    super.update(taskFromWeb);
                } else {
                    Task updateTime = new Task();
                    updateTime.setId(taskFromWeb.getId());
                    updateTime.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    updateByid(updateTime);
                }
            }
        } else {
            super.update(taskFromWeb);
        }
    }

    private void addStreamingStrategy(Task taskFromWeb) {
        RuntimeConfig runtimeConfig = JSON.parseObject(taskFromWeb.getRuntimeConfig(), RuntimeConfig.class);
        if (isStreaming(taskFromWeb) && runtimeConfig.getIsAutoScaleMode()
                && runtimeConfig.getStrategyList() != null) {
            taskScaleStrategyService.updateStrategy(taskFromWeb);
        }

        super.update(taskFromWeb);
    }

    @Override
    public void tag(Task taskFromWeb) {
        this.update(taskFromWeb);

        int currentMaxVersion = taskVersionService.getMaxVersionById(taskFromWeb.getId());
        TaskVersion tag = new TaskVersion(taskFromWeb).setTaskId(taskFromWeb.getId()).setVersion(++currentMaxVersion);

        taskVersionService.save(tag);
    }

    @Override
    public Map<String, String> getTaskCode(Task task) {
        Job job = JobFactory.getJob(task, null, null, this);
        Submitter submitter = buildSubmitter(job, taskSchedulerRpcApiBlockingStub);
        return submitter.update();
    }

    private void start(Task task, Integer tagId, Integer savepointId) throws Exception {
        Job job = null;
        Submitter submitter = null;
        try {
            job = JobFactory.getJob(task, tagId, savepointId, this);

            job.beforeExec();
            submitter = buildSubmitter(job, taskSchedulerRpcApiBlockingStub);
            submitter.submitAsync();


            // 上线 or 发版 需保存新的taskVersion
            task.setReleaseTime(new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error(String.format("taskId[%d] startApp error[%s]: %s", task.getId(), e.getMessage(), e));
            if (job == null || submitter == null) {
                throw new ServiceException(BaseResponseCodeEnum.APP_START_FAIL, e);
            }
            submitter.processException();
            changeStateCode(task, JobStatus.FAILED.name());

            if (submitter instanceof OfflineSubmitter && e instanceof ServiceException) {
                throw new ServiceException(BaseResponseCodeEnum.APP_START_FAIL, "调度模块响应错误，" + ((ServiceException) e).getData());
            }
            throw new ServiceException(BaseResponseCodeEnum.APP_START_FAIL, e);
        }
        super.update(task);
    }

    @Override
    public void start(Integer taskId, Integer tagId, Integer savepointId) throws Exception {
        Task task = checkExist(taskId);
        if (isStreaming(task)) {
            task.setStatusCode(DsTaskConstant.JOB_STATUS_INITIALIZING).setOnline(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        } else {
            task.setOnline(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        }
        start(task, tagId, savepointId);
    }

    @Override
    public Integer addTaskVersionAndAudit(Task task, String operation) {
        int currentMaxVersion = 1;
        if (operation.equals(BaseActionCodeEnum.CREATE.name())
                || operation.equals(BaseActionCodeEnum.COPY.name())
                || operation.equals(BaseActionCodeEnum.CREATEANDSTART.name())) {
            currentMaxVersion = task.getCurrentVersion();
        } else {
            currentMaxVersion = taskVersionService.getMaxVersionById(task.getId()) + 1;
        }
        TaskVersion tag = new TaskVersion(task).setTaskId(task.getId()).setVersion(currentMaxVersion);
        BaseActionCodeEnum actionCodeEnum = BaseActionCodeEnum.actionEnumMap.get(operation);
        String message = actionCodeEnum.getMessage();
        String currentUserName = InfTraceContextHolder.get().getUserName();
        if (!currentUserName.equals("system") &&
                (operation.equals(BaseActionCodeEnum.CREATE.name())
                        || operation.equals(BaseActionCodeEnum.COPY.name())
                        || operation.equals(BaseActionCodeEnum.CREATEANDSTART.name())
                        || operation.equals(BaseActionCodeEnum.UPDATE.name())
                        || operation.equals(BaseActionCodeEnum.UPDATEANDSTART.name()))
                        && StringUtils.isNotEmpty(task.getCommit())) {
            message = task.getCommit();
        }
        AuditlogUtil.auditlog(DsTaskConstant.TASK, task.getId(), currentMaxVersion, actionCodeEnum, message);
        taskVersionService.save(tag);
        return currentMaxVersion;
    }

    @Override
    public Integer addTaskVersion(Task task) {
        int currentMaxVersion = taskVersionService.getMaxVersionById(task.getId());
        currentMaxVersion = currentMaxVersion + 1;

        task.setCurrentVersion(currentMaxVersion);

        TaskVersion tag = new TaskVersion(task).setTaskId(task.getId()).setVersion(currentMaxVersion);
        taskVersionService.save(tag);
        return currentMaxVersion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<TaskVersion> addTaskVersions(List<Task> tasks) {
        if (tasks.size() == 0) {
            return new ArrayList<>();
        }

        // 设置最大版本号
        taskVersionService.setMaxVersions(tasks);
        return tasks.stream().map(task -> {
            // 设置上线时间
            task.setReleaseTime(new Timestamp(System.currentTimeMillis()));
            return new TaskVersion(task).setTaskId(task.getId()).setVersion(task.getCurrentVersion());
        }).collect(Collectors.toList());
    }

    private Submitter buildSubmitter(Job job, TaskSchedulerRpcApiGrpc.TaskSchedulerRpcApiBlockingStub taskSchedulerRpcApiBlockingStub) {
        if (job instanceof ScheduledJob) {
            return new OfflineSubmitter(job, taskSchedulerRpcApiBlockingStub);
        }
        FlinkBaseJob baseJob = ((FlinkBaseJob) job);
        Boolean isAutoScaleMode = baseJob.runtimeConfig.getIsAutoScaleMode();
        Boolean isBatchTask = baseJob.runtimeConfig.getIsBatchTask();

        if (isBatchTask) {
            // Flink批任务
            return new FlinkBatchSubmitter(this, job);
        }

        FlinkVersion flinkVersion = FlinkVersion.fromVersionString(((FlinkBaseJob) job).cluster.getVersion());
        boolean greaterThanFlink113 = flinkVersion.isGreaterThanFlink113();
        if (isAutoScaleMode && greaterThanFlink113) {
            return new AutoScaleSubmitter(this, job);
        }

        if (isAutoScaleMode) {
            throw new ServiceException(BaseResponseCodeEnum.CLUSTER_VERSION_NOT_MATCH);
        }
        return new K8sNativeSubmitter(this, job);
    }

    private String getLocalPath(String url) throws IOException {
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtilByUrl(url);
        if (url.startsWith("https:")) {
            return cloudClientUtil.download(url);
        }
        String[] bucketFile = UrlUtil.getBucketFile(url);
        String path = "";
        if (url.startsWith("gs://")) {
            path = GCSBucketPrefix + bucketFile[0] + "/o/" + bucketFile[1];
        } else if (url.startsWith("ks3://")) {
            path = KSBucketPrefix + bucketFile[1];
        } else if (url.startsWith("s3://")) {
            path = S3BucketPrefix + bucketFile[1];
        }
        return cloudClientUtil.download(path);
    }

    @Override
    public String getServiceUi(Integer taskId, String genieJobId, String state, String logFileUrl) throws Exception {
        Task task = checkExist(taskId);
        String url = null;
        //TODO 实时任务：字符串组装；离线任务：根据genie_job_id从日子获取
        if (isStreaming(task)) {
            TaskInstance job = taskInstanceService.getLatestJobByTaskId(taskId);
            if (job != null) {
                url = FlinkApiUtil.getFlinkJobDetailUrl(job.getServiceAddress(), job.getEngineInstanceId());
            }
        } else {
            InputStreamReader inputStreamReader = null;
            if (StringUtils.isNotBlank(logFileUrl)) {
                String local = getLocalPath(logFileUrl);
                inputStreamReader = new InputStreamReader(new FileInputStream(local), "UTF-8");
            } else {
                throw new ServiceException(BaseResponseCodeEnum.NOT_HAVE_SPARK_UI);
            }
            BufferedReader jobStderrReader = new BufferedReader(inputStreamReader);
            String line;
            if (state.equalsIgnoreCase("running")) {
                while ((line = jobStderrReader.readLine()) != null) {
                    if (line.contains("Spark context Web UI available at")) {
                        String[] strings = line.split(" ");
                        url = strings[strings.length - 1];
                        break;
                    }
                }
                if (url == null || url.isEmpty()) {
                    throw new ServiceException(BaseResponseCodeEnum.UI_GET_FAIL);
                }
            } else {
                String sparkAppSelector = null;
                while ((line = jobStderrReader.readLine()) != null) {
                    if (line.contains("spark-app-selector ->")) {
                        String[] strings = line.split(" ");
                        int index = Arrays.asList(strings).indexOf("spark-app-selector");
                        sparkAppSelector = strings[index + 2].replace(",", "");
                        break;
                    }
                }
                if (sparkAppSelector == null || sparkAppSelector.isEmpty()) {
                    throw new ServiceException(BaseResponseCodeEnum.UI_GET_FAIL);
                }

                String runtimeConfigJson = task.getRuntimeConfig();
                JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
                String region = runtimeConfigObject.getString("sourceRegion");
                DataCakeRegionProperties.RegionConfig regionConfig = DataCakeConfigUtil.getDataCakeRegionProperties().getRegionConfig(region);
                url=MessageFormat.format(regionConfig.getSparkHistoryServerUrl(), sparkAppSelector);

            }
        }
        return url;
    }

    @Override
    public String getMetricsUi(Integer taskId, String genieJobId) {
        Task task = checkExist(taskId);
        String url = null;
        if (isStreaming(task)) {
            TaskInstance job = taskInstanceService.getLatestJobByTaskId(taskId);
            if (job != null) {
                url = FlinkApiUtil.getPerjobMetricsUrl(task.getName().toLowerCase(), job.getEngineInstanceId());
            }
        }
        return url;
    }

    @Override
    public String getlogsUi(Integer taskId, String executionDate) throws Exception {
        Task task = checkExist(taskId);
        String url = null;
        if (isStreaming(task)) {
            FlinkCluster flinkCluster = flinkClusterService.getById(task.getFlinkClusterId());
            if (flinkCluster != null) {
                url = FlinkApiUtil.getLogsUrl(flinkCluster.getLogEsSource(), task.getName());
            }
        } else {
            TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.getLogUrl(
                    TaskSchedulerApi.GetLogRequest
                            .newBuilder()
                            .setName(task.getName())
                            .setExecutionDate(executionDate)
                            .build()
            );
            Integer code = taskCommonResponse.getCode();
            if (code != 0) {
                String msg = taskCommonResponse.getMessage();
                throw new ServiceException(code.toString(), String.format("获取日志地址失败，请稍后再试。code: %s, message: %s", code, msg));
            } else {
                JSONObject data = JSON.parseObject(taskCommonResponse.getData());
                url = data.getString("logurl");
            }
        }
        return url;
    }

    @Override
    public ResponseEntity<Object> getInstanceLog(String url) throws IOException {
        log.info(" param url is :" + url);
        String local = getLocalPath(url);

        try {
            FileSystemResource file = new FileSystemResource(local);
            InputStreamResource resource = new InputStreamResource(file.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("inline;filename=%s;filename*=utf-8''%s",
                    URLEncodeUtil.encode(file.getFilename()), URLEncodeUtil.encode(file.getFilename())));
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            ResponseEntity.BodyBuilder ok = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType("text/plain"));

            return ok.body(resource);
        } catch (IOException e) {
            log.error(String.format("url[%s] failed to down load: %s", url, CommonUtil.printStackTraceToString(e)));
            throw new ServiceException(BaseResponseCodeEnum.DOWNLOAD_FAIL);
        }
    }

    /**
     * 返回码
     * 枚举: 0,1,2
     * 枚举备注:
     * 0 SUCCESS 代表一切ok
     * 1 WARNING 代表业务逻辑方面的判断不ok
     * 2 ERROR 代表程序内部有Exception
     */
    @Override
    public Object backFill(Integer id, String startDate, String endDate, Integer[] childIds, String isSendNotify, String isCheckDependency) {
        Task coreTask = checkExist(id);
        String ids = StringUtils.join(childIds,",");

        Boolean checkDependency = isCheckDependency != null && !isCheckDependency.isEmpty() && Boolean.parseBoolean(isCheckDependency);

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.taskBackfill(
                TaskSchedulerApi.BackFillRequest
                        .newBuilder()
                        .setIds(ids)
                        .setCoreTaskName(coreTask.getName())
                        .setOperator(InfTraceContextHolder.get().getUserName())
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .setIsSendNotify(Boolean.parseBoolean(isSendNotify))
                        .setIsCheckDependency(checkDependency)
                        .build()
        );

        if (taskCommonResponse.getCode() != 0) {
            log.error(String.format("[%d]backFill failed to pipeline: %s", id, taskCommonResponse.getData()));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
        }
        return BaseResponseCodeEnum.SUCCESS;
    }

    @Override
    public String process(Integer userActionId) {
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.progressReminder(
                TaskSchedulerApi.UserActionRequest
                        .newBuilder()
                        .setUserActionId(userActionId)
                        .build()
        );

        if (taskCommonResponse == null) {
            throw new ServiceException(BaseResponseCodeEnum.GET_BACKFILL_STATUS_FAIL, "获取深度补数任务状态失败,失败原因:" + taskCommonResponse.getMessage());
        }

        if (taskCommonResponse.getCode() == 2) {
            throw new ServiceException(BaseResponseCodeEnum.GET_BACKFILL_STATUS_FAIL, "获取深度补数任务状态失败，程序内部有Exception,失败原因:" + taskCommonResponse.getMessage());
        }

        if (taskCommonResponse.getCode() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.GET_BACKFILL_STATUS_FAIL, "获取深度补数任务状态失败，业务逻辑方面的判断有错误,失败原因:" + taskCommonResponse.getMessage());
        }

        return taskCommonResponse.getData();
    }

    @Override
    public List<Map<String, Object>> getCatalog() {
        return taskMapper.selectForCatalog().stream().collect(Collectors.groupingBy(Task::getTenancyCode))
                .entrySet().stream().map(entry -> {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("dept", entry.getKey());
                    map.put("children", entry.getValue());
                    return map;
                }).sorted((o1, o2) -> StringUtils.compareIgnoreCase(o1.get("dept").toString(), o2.get("dept").toString())).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> page(Integer pageNum, Integer pageSize, Map<String, String> paramMap) {
        Map<String, Object> result = new HashMap<>(4);
        // 处理按任务id查询
        Integer keyWordId = CommonUtil.getIdFromMap(paramMap, "keyWord");
        // 获取文件夹id
        Integer folderId = CommonUtil.getIdFromMap(paramMap, "folderId");
        // id是文件夹管理点击某个任务时需要传递的参数，和keywordid是联合查询，但如果传递了id，以id为准，keyword可以用来检索名称
        Integer id = CommonUtil.getIdFromMap(paramMap, "id");
        if(id != null){
            keyWordId = id;
        }

        List<Integer> taskIds= Lists.newArrayList();
        // 处理按label id查询
        List<Integer> taskIdsLable = getTaskIdsFromLabel(paramMap);

        // label不关联任务时，组装空结果
        if (paramMap.containsKey("labelId") && CollectionUtils.isEmpty(taskIdsLable)) {
            assembleBlankResult(result);
            return result;
        }else {
            taskIds=taskIdsLable;
        }
        // 如果查询的是文件夹下的任务
        if (folderId != null) {
            String fileName = "";
            if (paramMap.containsKey("fileName") && StringUtils.isNotBlank(paramMap.get("fileName"))) {
                fileName = paramMap.get("fileName");
            }
            taskIds = fileManagerServiceImpl.getEntityIdContainedInFolder(folderId, fileName);

            if (taskIds.size() == 0) {
                assembleBlankResult(result);
                return result;
            }
        }

        if (CollectionUtils.isEmpty(taskIds)){
            taskIds=null;
        }

        List<String> streamingTemplate = TemplateEnum.getStreamingTemplate().stream().map(Enum::name).collect(Collectors.toList());
        List<String> statusParam = getStreamingRunStatus();
                                         if (paramMap.containsKey("online") && paramMap.get("online").equals("0")) {
            paramMap.put("statusParam", " not in (" + StringUtils.join(statusParam.stream().map(data -> "\"" + data + "\"").toArray(), ",") + ")");
        }else {
            paramMap.put("statusParam", " in (" + StringUtils.join(statusParam.stream().map(data -> "\"" + data + "\"").toArray(), ",") + ")");
        }

        paramMap.put("streamingTemplateList", "(" + StringUtils.join(streamingTemplate.stream().map(data -> "\"" + data + "\"").toArray(), ",") + ")");

        if (paramMap.get("down")!=null && paramMap.get("down").equalsIgnoreCase("false")) {
            paramMap.remove("down");
        }
        PageHelper.startPage(pageNum, pageSize);
        if(("UNEXECUTED").equals(paramMap.get("statusCode"))){
            paramMap.put("statusCode","CREATED,SCHEDULED");
        }
        List<Task> tasks = taskMapper.listByMap(taskIds, keyWordId, paramMap);
        if (folderId != null){
            // 如果查询的是文件夹下的任务，则需要按照文件夹的规则排序展示结果
            tasks.sort(Comparator.comparing(Task::getName));
        }
        PageInfo<Task> pageInfo = new PageInfo<>(tasks);

        // 设置流式任务
        tasks.forEach(this::isStreaming);

        // 设置任务是否可编辑
        setCanEdit(tasks);
        List<Map<String, Object>> taskStatusCountList = taskMapper.taskStatusCount(taskIds, keyWordId, paramMap);
        assembleResult(result, taskStatusCountList);
        result.put("result", pageInfo);
        return result;
    }

    public List<String> getStreamingRunStatus() {
        String[] runStatus = new String[]{JobStatus.RUNNING.name(), JobStatus.FAILING.name(),
                JobStatus.RESTARTING.name(), JobStatus.RECONCILING.name(),
                DsTaskConstant.JOB_STATUS_INITIALIZING};
        return Arrays.asList(runStatus);
    }

    private List<Task> matchTasks(List<Task> list, String tableName, Boolean down) {
        if (StringUtils.isEmpty(tableName)) {
            return list;
        }

        return list.stream().filter(task -> {
            List<String> inputGuids = getGuids(task.getInputGuids());
            List<String> outputGuids = getGuids(task.getOutputGuids());

            if (down != null && down) {
                return matchTables(inputGuids, tableName);
            }

            Boolean input = matchTables(inputGuids, tableName);
            Boolean output = matchTables(outputGuids, tableName);
            return input || output;
        }).collect(Collectors.toList());
    }

    private Boolean matchTable(String table, String tableNameFromWeb) {
        if (StringUtils.isEmpty(table)) {
            return false;
        }
        return table.contains(tableNameFromWeb);
    }

    private Boolean matchTables(List<String> tables, String tableNameFromWeb) {
        for (String table : tables) {
            Boolean match = matchTable(table, tableNameFromWeb);
            if (match) {
                return true;
            }
        }
        return false;
    }

    private List<String> getGuids(String guids) {
        if (StringUtils.isNotEmpty(guids)) {
            return Arrays.asList(guids.split(","));
        }
        return new ArrayList<>();
    }

    @Override
    public Boolean isStreaming(Task task) {
        task.setIsStreamingTemplateCode(false);
        boolean stream = TemplateEnum.valueOf(task.getTemplateCode()).isStreamingTemplate();
        try {
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            if (stream) {
                Boolean isBatchTask = runtimeConfig.getAdvancedParameters().getIsBatchTask();
                task.setIsStreamingTemplateCode(isBatchTask == null || !isBatchTask);
            } else if (TemplateEnum.valueOf(task.getTemplateCode()) == TemplateEnum.Mysql2Hive) {
                Integer syncType = runtimeConfig.getAdvancedParameters().getSyncType();
                if (syncType == null) {
                    syncType = runtimeConfig.getCatalog().getSync_mode();
                }
                if (syncType == 2) task.setIsStreamingTemplateCode(true);
            }
        }catch (Exception e) {
            log.error(BaseResponseCodeEnum.SYS_ERR.name()+"runtimeConfig解析失败",e);
        }
        return task.getIsStreamingTemplateCode();
    }

    @Override
    public Map<String, String> getGitFileSql(String projectName, String filePath) throws IOException {
        checkParm(projectName, filePath);
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        String url = DsTaskConstant.UPLOAD_OBS_ADS_GIT_PATH + projectName + filePath;
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtilByUrl(url);
        FileSystemResource file = new FileSystemResource(cloudClientUtil.download(url));
        Map<String, String> res = new HashMap<>();
        String content;
        String conf = null;
        try {
            String allContent = readStream(file.getInputStream());
            String[] splits = allContent.split(DsTaskConstant.GIT_FLIE_SEGMENTATION);
            if (splits.length > 3) {
                throw new ServiceException(BaseResponseCodeEnum.GIT_FILE_CONTECT_ERR);
            }
            if (splits.length == 1) {
                content = encryption(splits[0]);
            } else {
                conf = splits[0];
                content = encryption(splits[1]);
            }
            res.put("content", content);
            res.put("conf", conf);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_FAIL);
        }
        return res;
    }

    public String encryption(String orgConect) throws Exception {
        return new String(Base64.getEncoder().encode(URLEncoder.encode(orgConect, "UTF-8").replaceAll("\\+", "%20").getBytes()));
    }

    public void checkParm(String projectName, String filePath) {
        if (StringUtils.isEmpty(projectName) || StringUtils.isEmpty(filePath)) {
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_PARAM);
        }
        String[] split = filePath.split("\\\\");
        if (!split[split.length - 1].contains(".")) {
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_PARAM_FILE);
        }
    }

    public List<Task> getTaskByMetadataId(String metadataId) {
        return taskMapper.selectWithMatadataid(metadataId);
    }

    public Task getTaskNameById(int id) {
        return taskMapper.getTaskNameById(id);
    }

    public List<Map<String, Object>> getTaskInfo(String name) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        List<Task> tasks = taskMapper.selectByMetadataIdOrName(name);
        tasks.forEach(task -> {
            HashMap<String, Object> taskInfo = new HashMap<>();

            //兼容旧历史任务
            produceOutputGra(task);
            TriggerParam triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
            if (triggerParam != null) {
                taskInfo.put("granularity", triggerParam.getOutputGranularity());
            }
            if (task.getOutputDataset() != null) {
                List<Dataset> datasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
                if (datasets != null && datasets.size() > 0) {
                    taskInfo.put("offset", datasets.get(0).getOffset());
                    taskInfo.put("metadataId", datasets.get(0).getId());
                }
            }
            taskInfo.put("name", task.getName());
            taskInfo.put("id", task.getId());
            taskInfo.put("templateCode", task.getTemplateCode());
            list.add(taskInfo);
        });
        return list;
    }

    @Override
    public List<Task> backUpdate() {
        List<Task> all = taskMapper.getAll();
        all.forEach(task -> {
            JSONObject jsonObject = JSONObject.parseObject(task.getRuntimeConfig());
            jsonObject.put("isAutoScaleMode", false);
            String s = JSON.toJSONString(jsonObject);
            task.setRuntimeConfig(s);
        });
        update(all);
        return all;
    }

    @Override
    public void autoScaleTm(Integer id, Integer count) {
        Task task = checkAutoScaleModeAndCount(id, count);
        Submitter submitter = buildSubmitterWithTaskInstance(task);
        submitter.autoScaleTm(count);
    }

    @Override
    public Integer getAutoScaleTaskParal(Integer id) {
        Task task = checkAutoScaleTask(id);
        Submitter submitter = buildSubmitterWithTaskInstance(task);
        return submitter.getTmNum();
    }

    @Override
    public void stop(Integer id) {
        Task task = checkExist(id);

        if (JobStatus.CANCELLING.name().equals(task.getStatusCode())) {
            throw new ServiceException(BaseResponseCodeEnum.APP_IS_CANCELING_OR_SUSPENDING);
        }

        taskInstanceService.stopWithSavepoint(task.getId());
        changeStateCode(task, JobStatus.SUSPENDED.name());

        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        Boolean isAutoScaleMode = runtimeConfig.getAdvancedParameters().getIsAutoScaleMode();
        if (isAutoScaleMode) {
            try {
                Submitter submitter = buildSubmitterWithTaskInstance(task);
                submitter.deleteResource();
            } catch (Exception e) {
                log.error("stop task id:" + id + ", name:" + task.getName() + " failed", e);
                changeStateCode(task, JobStatus.FAILED.name());
            }
        }
    }

    @Override
    public void cancel(Integer id) {
        Task task = checkExist(id);

        if (JobStatus.CANCELLING.name().equals(task.getStatusCode())) {
            throw new ServiceException(BaseResponseCodeEnum.APP_IS_CANCELING_OR_SUSPENDING);
        }

        if (!isStreaming(task)) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_IS_NOT_STREAMING_MODE);
        }

        taskInstanceService.cancelJob(task, null);
        changeStateCode(task, JobStatus.CANCELED.name());

        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        Boolean isAutoScaleMode = runtimeConfig.getAdvancedParameters().getIsAutoScaleMode();
        if (isAutoScaleMode) {
            try {
                Submitter submitter = buildSubmitterWithTaskInstance(task);
                submitter.deleteResource();
            } catch (Exception e) {
                log.error("cancel task id:" + id + ", name:" + task.getName() + " failed", e);
                changeStateCode(task, JobStatus.FAILED.name());
            }
        }
    }

    private Submitter buildSubmitterWithTaskInstance(Task task) {
        TaskInstance taskInstance = taskInstanceService.getLatestJobByTaskId(task.getId());
        Job job = JobFactory.getJob(task, taskInstance.getVersionId(), -1, this);
        FlinkCluster cluster = flinkClusterService.getById(task.getFlinkClusterId());
        FlinkBaseJob tmp = (FlinkBaseJob) job;
        tmp.setCluster(cluster);
        tmp.setTaskInstance(taskInstance);
        return buildSubmitter(tmp, taskSchedulerRpcApiBlockingStub);
    }

    @Override
    public void statusHook(String taskName, String status) {
        log.info("statusHook:taskName=" + taskName + "  status=" + status);
        Task task = taskMapper.selectByName(taskName);
        changeStateCode(task, status.toUpperCase());
    }

    @Override
    public Map<String, Object> getChildDependencies(Integer id) {
        Task task = checkExist(id);
        Map<Integer, Boolean> filterMap = new HashMap<>(16);
        Map<String, Object> result = new HashMap<>(2);
        filterMap.put(id, true);

        task.setCanEdit(false);
        isTaskInCurrent(task);

        result.put("id", id);
        result.put("name", task.getName());
        result.put("isCurrent", task.getCanEdit());

        List<Task> eventDependTasks = taskMapper.selectChildrenByEventDependsInGroup(id);
        List<Task> collect = eventDependTasks.stream().distinct().collect(Collectors.toList());

        isTaskInCurrent(collect);

        List<Map<String, Object>> child = getChild(filterMap, collect);
        result.put("children", child);
        return result;
    }

    //TODO 供新元数据系统使用，现返回null
    @Override
    public Map<String, Object> getDependenciesOverview(String tableName) {
        Map<String, Object> result = new HashMap<>(4);
        result.put("taskId", "");
        result.put("taskName", "");
        result.put("upDepCount", 0);
        result.put("downDepCount", 0);
        result.put("newestFinishTime", "");
        return result;
    }

    @Override
    public Map<String, Object> upAndDown(Integer id, Integer upDown) {
        // upDown: 0-上下游  1-上游  2-下游
        Task task = checkExist(id);
        // 特殊处理1038个任务
        Map<String, Object> result = new HashMap<>(2);
        List<Map<String, Object>> resultMap = new ArrayList<>();
        List<Map<String, Integer>> links = new ArrayList<>();

        if (!StringUtils.isEmpty(task.getInputGuids())
                && !StringUtils.isEmpty(task.getOutputGuids())
                && ((task.getInputGuids().equals("test.a@ue1") && upDown != 2) || (task.getOutputGuids().equals("test.a@ue1") && upDown != 1))) {
            // 返回空
            result.put("tasks", resultMap);
            result.put("links", links);
            return result;
        }

        produceUpAndDown(task, links, upDown);

        Set<Integer> set = new HashSet<>();
        for (Map<String, Integer> map : links) {
            set.add(map.get("source"));
            set.add(map.get("target"));
        }
        List<Integer> taskSet = new ArrayList<>(set);
        if (taskSet.size() == 0) {
            result.put("tasks", resultMap);
            result.put("links", links);
            return result;
        }
        List<Task> tasks = taskMapper.selectWithIds(taskSet);

        // 批量获取离线任务对应的最新实例
        long offlineTaskNum = tasks.stream().filter(t -> !isStreaming(t)).count();
        long s1 = System.currentTimeMillis();
        if (offlineTaskNum > 0) {
            produceOfflineTaskInstanceMap(tasks, resultMap);
        }
        long s2 = System.currentTimeMillis();
        log.info("upAndDown调度侧批量获取离线任务总耗时:" + (s2 - s1));

        tasks.forEach(tmp -> assembleRealtimeTask(resultMap, tmp));

        result.put("tasks", resultMap);
        result.put("links", links);
        return result;
    }

    @Override
    public Map<String, Object> getDependencies(Integer id, Integer level, String executionDate, Integer upDown) {
        Task task = checkExist(id);
        Map<String, Object> result = new HashMap<>(2);
        getAllTaskInstanceDependencies(task, level, executionDate, result, upDown);
        return result;
    }

    private void getAllTaskInstanceDependencies(Task task, Integer level, String executionDate, Map<String, Object> result, Integer upDown) {
        Map<String, Object> up = new HashMap<>(2);
        Map<String, Object> down = new HashMap<>(2);
        switch (upDown) {
            // 上下游
            case 0:
                getTaskInstanceDependency(task, level, executionDate, up, false);
                getTaskInstanceDependency(task, level, executionDate, down, true);
                break;
            // 上游
            case 1:
                getTaskInstanceDependency(task, level, executionDate, up, false);
                break;
            // 下游
            case 2:
                getTaskInstanceDependency(task, level, executionDate, down, true);
                break;
        }
        List<Dependency> instanceList = getInstanceList(up, down);
        ArrayList relation = getRelation(up, down);
        String coreTaskId = getCoreTaskId(up, down);

        result.put("instanceList", instanceList);
        result.put("relation", relation);
        result.put("coreTaskId", coreTaskId);
    }

    private String getCoreTaskId(Map<String, Object> up, Map<String, Object> down) {
        String coreTaskId = (String) up.get("core_task_id");
        if (StringUtils.isEmpty(coreTaskId)) {
            coreTaskId = (String) down.get("core_task_id");
        }
        return coreTaskId;
    }

    private ArrayList getRelation(Map<String, Object> up, Map<String, Object> down) {
        ArrayList upRelation = (ArrayList) up.getOrDefault("relation", new ArrayList());
        ArrayList downRelation = (ArrayList) down.getOrDefault("relation", new ArrayList());
        upRelation.addAll(downRelation);
        return upRelation;
    }

    private List<Dependency> getInstanceList(Map<String, Object> up, Map<String, Object> down) {
        List<Dependency> upInstanceList = (List<Dependency>) up.getOrDefault("instance_list", new ArrayList<Dependency>());
        List<Dependency> downInstanceList = (List<Dependency>) down.getOrDefault("instance_list", new ArrayList<Dependency>());
        upInstanceList.addAll(downInstanceList);
        List<Dependency> distinct = distinct(upInstanceList);
        return distinct;
    }

    private List<Dependency> distinct(List<Dependency> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().collect(Collectors.collectingAndThen(Collectors
                .toCollection(() -> new TreeSet<>(Comparator.comparing(Dependency::getId))), ArrayList::new));
    }

    private void getTaskInstanceDependency(Task task, Integer level, String executionDate, Map<String, Object> result, Boolean isDownstream) {
        long s1 = System.currentTimeMillis();
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.getInstanceRelation(
                TaskSchedulerApi.DataRelationRequest
                        .newBuilder()
                        .setCoreTask(task.getName())
                        .setExecutionDate(executionDate)
                        .setLevel(level)
                        .setIsDownstream(isDownstream)
                        .build());

        long s2 = System.currentTimeMillis();
        log.info("task/instance/relation接口耗时:" + (s2 - s1));
        if (taskCommonResponse == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }

        JSONObject data = JSON.parseObject(taskCommonResponse.getData());
        JSONArray instanceList = data.getJSONArray("instance_list");
        String coreTaskId = data.getString("core_task_id");
        result.put("core_task_id", coreTaskId);
        if (instanceList == null) {
            result.put("instance_list", new ArrayList<Dependency>());
            result.put("relation", new ArrayList<>());
            return;
        }
        JSONArray relationJSONArray = data.getJSONArray("relation");
        List<SourceTarget> relation = relationJSONArray.toJavaList(SourceTarget.class);
        List<Dependency> dependencies = instanceList.toJavaList(Dependency.class);
        List<String> taskNames = dependencies.stream().map(Dependency::getDagId).distinct().collect(Collectors.toList());
        List<Task> tasks = taskMapper.selectWithNames(taskNames);
        Map<String, Integer> name2IdMap = new HashMap<>(tasks.size());
        Map<Integer, String> id2TableMap = new HashMap<>(tasks.size());

        tasks.forEach(t -> {
            name2IdMap.put(t.getName(), t.getId());
            String tableName = getQualifiedName(t.getOutputDataset());
            id2TableMap.put(t.getId(), tableName);
        });
        dependencies.forEach(dependency -> {
            String taskName = dependency.getDagId();
            Integer taskId = name2IdMap.get(taskName);
            String tableName = id2TableMap.get(taskId);
            dependency.setTaskId(taskId);
            dependency.setTable(tableName);
        });
        result.put("instance_list", dependencies);
        result.put("relation", relation);
    }

    /**
     * 应用详情
     */
    @Override
    public Task getById(Object id) {
        return getByIdOrName(id, null);
    }

    @Override
    public Task getByName(String name) {
        return getByIdOrName(null, name);
    }

    @Override
    public String getDdl(String guId) {
        try {
            return DdlFactory.getDdl(guId).getDdl(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SQL_DECODE_FAIL);
        }
    }

    @Override
    public String templateRendering(String modelName, String modelType) {
        ActorDefinition actorDefinition = actorDefinitionService.selectOne(new ActorDefinition().setName(modelName).setActorType(modelType));
        return actorDefinition!=null ? actorDefinition.getSpec():"";
    }

    @Override
    public Map<String, Object> getDisplayDdl(String guId, Boolean isSql) {
        try {
            SqlDdl ddl = DdlFactory.getDdl(guId);
            HashMap<String, Object> result = new HashMap<>(2);
            if (ddl instanceof MetisDdl || ddl instanceof KafkaDdl) {
                Object displaySchema = ddl.getDisplaySchema(isSql);
                result.put("columns", displaySchema);
            } else {
                result.put("columns", ddl.getColumns());
            }

            String displayTableName = ddl.getDisplayTableName();
            result.put("table", displayTableName);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SQL_DECODE_FAIL);
        }
    }

    @Override
    public BaseResponse check(Task task) {
        if (isStreaming(task)) {
            Job job = JobFactory.getJob(task, -1, -1, this);
            if (job instanceof FlinkSqlJob) {
                return flinkSqlCheck(task);
            }
        } else {
            return sparkSqlCheck(task);
        }
        return BaseResponse.success("sql校验成功");
    }

    @Override
    @Cacheable(cacheNames = {"saprkSqlCheck"}, key = "#id+'-'+#md5Sql+'-'+#region+'-'+#owner")
    public void cacheCheck(Task task, Integer id, String md5Sql, String region, String owner) throws Exception {
        BaseResponse check = taskService.check(task);
        String checkResult = check.getData().toString();
        if (check.getCode() == 500 || (StringUtils.isNotEmpty(checkResult) &&
                checkResult.contains(BaseResponseCodeEnum.CHECK_FAIL.getMessage()))) {
            throw new ServiceException(BaseResponseCodeEnum.SPARK_SQL_CHECK_FAIL.name(), "SQL语法出错，校验不通过");
        }
    }

    private BaseResponse flinkSqlCheck(Task task) {
        BaseResponse res;
        try {
            String content = task.getContent();
            String sql = URLDecoder.decode(new String(Base64.getDecoder().decode(content.getBytes())), "UTF-8");
            FlinkSqlParseUtil.parse(sql);
        } catch (SqlParseException | UnsupportedOperationException e) {
            res = BaseResponse.success(CommonUtil.printStackTraceToString(e));
            return res;
        } catch (Exception e) {
            res = BaseResponse.error(BaseResponseCodeEnum.TASK_CHECK_FAIL, CommonUtil.printStackTraceToString(e));
            return res;
        }
        return BaseResponse.success("sql校验成功");
    }

    private BaseResponse sparkSqlCheck(Task task) {
        BaseResponse res;
        try {
            String runtimeConfigJson = task.getRuntimeConfig();
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(runtimeConfigJson);
            String region = runtimeConfig.getSourceRegion();
            String owner = runtimeConfig.getAdvancedParameters().getOwner();
            if (region.isEmpty()) {
                return BaseResponse.error(BaseResponseCodeEnum.SPARKSQL_MISSING_PARAM);
            }
            String content = task.getContent();
            if (content == null || content.isEmpty()) {
                res = BaseResponse.success();
            } else {
                String sql = URLDecoder.decode(new String(Base64.getDecoder().decode(content.getBytes())), "UTF-8");
                res = BaseResponse.success(SparkSqlParseUtil.parse(this, sql, owner, region));
            }
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.TASK_CHECK_FAIL, CommonUtil.printStackTraceToString(e));
        }
        return res;
    }

    @Override
    public void onlineAndOffline(Integer id, Integer status, Boolean ifNotify) {
        if (status != null) {
            if (status == 1) {
                AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.ONLINE, "任务上线");
            }
            if (status == 0) {
                AuditlogUtil.auditlog(DsTaskConstant.TASK, id, BaseActionCodeEnum.OFFLINE, "任务下线");
            }
        }

        Task task = checkExist(id);
        boolean isOnline = status != null && status == 1;

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.pauseTask(
                TaskSchedulerApi.PausedParamRequest
                        .newBuilder()
                        .setName(task.getName())
                        .setIsOnline(isOnline)
                        .build()
        );

        if (taskCommonResponse == null) {
            throw new ServiceException(BaseResponseCodeEnum.ONLINE_AND_OFFLINE_FAIL, "上下线任务失败:" + taskCommonResponse.getMessage());
        }

        if (taskCommonResponse.getCode() == 2) {
            throw new ServiceException(BaseResponseCodeEnum.ONLINE_AND_OFFLINE_FAIL, "上下线任务失败，程序内部有Exception:" + taskCommonResponse.getMessage());
        }

        if (taskCommonResponse.getCode() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.ONLINE_AND_OFFLINE_FAIL, "上下线任务失败，业务逻辑方面的判断有错误:" + taskCommonResponse.getMessage());
        }
        Task newTask = checkExist(id);
        newTask.setOnline(status)
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(newTask.getUpdateTime());

        super.update(newTask);

        //任务下线，通知直接下游逻辑
        if (status == 0 && ifNotify) {
            List<Task> tasks = taskMapper.selectChildDependendcies(id, newTask.getOutputGuids());
            List<Task> list = taskMapper.selectChildrenByEventDepends(task.getId());
            tasks.addAll(list);
            Set<String> notifySet = new HashSet<>();
            for (Task perTask : tasks) {
                notifySet.add(perTask.getCreateBy());
                if (StringUtils.isNotEmpty(perTask.getCollaborators())) {
                    notifySet.addAll(Arrays.asList(perTask.getCollaborators().split(",")));
                }
            }
            String message = "任务：" + newTask.getName() + ",被" + InfTraceContextHolder.get().getUserName() + "下线，请确定是否对下游任务有影响。";
            dingDingService.notify(new ArrayList(notifySet), message);
            log.info("下线通知：" + message + " 人数：" + notifySet.size());
        }
    }

    @Override
    public Map<String, List<Task>> listTasks(String name) {
        List<Task> tasks = taskMapper.selectLikeName(name,InfTraceContextHolder.get().getUuid());
        List<FileManager> fileManagers = fileManagerMapper.selectByModule(FileManager.Module.TASK.name());
        Set<Integer> existIdSet = new HashSet<>();
        for (FileManager fm : fileManagers) {
            if(fm.getEntityId()!=null){
                existIdSet.add(fm.getEntityId());
            }
        }
        List<Task> filteredTasks = tasks.stream()
                .filter(task -> !existIdSet.contains(task.getId()))
                .collect(Collectors.toList());

        return filteredTasks.stream().collect(Collectors.groupingBy(Task::getCreateBy));
    }

    @Override
    public List<TaskNameVO> searchByName(String name) {
        Example example = new Example(Task.class);
        if (StringUtils.isNotBlank(name)) {
            example.or()
                    .andLike("name", "%" + name + "%")
                    .andEqualTo("workflowId", 0)
                    .andEqualTo("source", TaskSourceEnum.TASK.getType())
                    .andEqualTo("userGroup",InfTraceContextHolder.get().getUuid())
                    .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        }

        return listByExample(example).stream()
                .map(TaskNameVO::new)
                .sorted(Comparator.comparing(TaskNameVO::getName))
                .collect(Collectors.toList());
    }

    @Override
    public void setCanEdit(Task task) {
        ArrayList<Task> list = new ArrayList<>(1);
        list.add(task);
        setCanEdit(list);
    }

    private void setCanEdit(List<Task> list) {
        list.stream().forEach(task -> {
            JSONObject runtimeConfigObject = JSON.parseObject(task.getRuntimeConfig());
            TriggerParam dependTaskTriggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
            if (dependTaskTriggerParam != null) {
                task.setIsIrregularSheduler(dependTaskTriggerParam.getIsIrregularSheduler());
            }
            String advancedParameters = runtimeConfigObject.getString("advancedParameters");
            List<Integer> groupIds = new ArrayList<>();
            if (advancedParameters != null) {
                JSONObject advancedParametersObject = JSON.parseObject(advancedParameters);
                String dsGroups = advancedParametersObject.getString("dsGroups");
                if (StringUtils.isNotEmpty(dsGroups) && !"[]".equalsIgnoreCase(dsGroups)) {
                    groupIds = JSON.parseArray(dsGroups, Integer.class);
                }
            } else {
                String dsGroups = runtimeConfigObject.getString("dsGroups");
                if (StringUtils.isNotEmpty(dsGroups) && !"[]".equalsIgnoreCase(dsGroups)) {
                    groupIds = JSON.parseArray(dsGroups, Integer.class);
                }
            }
            if (!groupIds.isEmpty()) {
                String userGroups = userGroupService.listByIds(groupIds).stream().map(UserGroup::getName)
                        .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
                task.setUserGroups(userGroups);
            }
        });

        try {
            Boolean admin = InfTraceContextHolder.get().getAdmin();
            if (admin == null) {
                admin = false;
            }

            if (admin) {
                list.forEach(task -> task.setCanEdit(true).setCanDelete(true));
                return;
            }

            isTaskInCurrent(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void isTaskInCurrent(Task task) {
        ArrayList<Task> list = new ArrayList<>(1);
        list.add(task);
        isTaskInCurrent(list);
    }

    private void isTaskInCurrent(List<Task> list) {
        String currentUser = InfTraceContextHolder.get().getUserName();
        String userGroup = InfTraceContextHolder.get().getUuid();
        list.forEach(task -> {
            if (StringUtils.isNotEmpty(task.getUserGroup()) && userGroup.equals(task.getUserGroup())) {
                task.setCanEdit(true).setCanDelete(true);
            }

            if (task.getCreateBy().equalsIgnoreCase(currentUser)) {
                task.setCanEdit(true).setCanDelete(true);
                return;
            }

            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            String owner = runtimeConfig.getAdvancedParameters().getOwner();
            if (!StringUtils.isEmpty(owner) && owner.equalsIgnoreCase(currentUser)) {
                task.setCanEdit(true).setCanDelete(true);
                return;
            }

            String collaborators = task.getCollaborators();
            if (!StringUtils.isEmpty(collaborators)) {
                String[] collaboratorsArr = collaborators.split(",");
                boolean contains = Arrays.stream(collaboratorsArr).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())).contains(currentUser);
                if (contains) {
                    task.setCanEdit(true);
                    return;
                }

            }

            String dsGroups = runtimeConfig.getAdvancedParameters().getDsGroups();
            if (StringUtils.isEmpty(dsGroups) || "[]".equalsIgnoreCase(dsGroups)) {
                return;
            }

            List<Integer> groupIds = JSON.parseArray(dsGroups, Integer.class);
            List<AccessUser> accessUsers = accessUserService.selectByGroupIds(groupIds);
            boolean contains = accessUsers.stream().map(AccessUser::getName).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol())).contains(currentUser);
            if (contains) {
                task.setCanEdit(true);
            }
        });
    }

    private List<Integer> getTaskIdsFromLabel(Map<String, String> paramMap) {
        String labelId = paramMap.computeIfPresent("labelId", (k, v) -> v);

        if (StringUtils.isEmpty(labelId)) {
            return null;
        }
        Label label = labelService.getById(labelId);
        String tasks = label.getTasks();
        if (StringUtils.isEmpty(tasks)) {
            return null;
        }

        return Arrays.stream(tasks.split(SymbolEnum.COMMA.getSymbol())).map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getChild(Map<Integer, Boolean> filterMap, List<Task> initList) {
        //用于保存子节点的list
        List<Map<String, Object>> childList = new ArrayList<>();
        for (Task task : initList) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("id", task.getId());
            UserGroup userGroup = userGroupServiceImpl.selectUserGroupByUuid(task.getUserGroup());
            map.put("name", task.getName()+String.format(" ( %s )",userGroup.getName()));
            map.put("isCurrent", task.getCanEdit());
            if (filterMap.containsKey(task.getId())) {
                map.put("children", new ArrayList<Task>());
                //加入子节点
                childList.add(map);
                continue;
            }
            filterMap.put(task.getId(), true);

            List<Task> eventDependTasks = taskMapper.selectChildrenByEventDepends(task.getId());
            List<Task> collect = eventDependTasks.stream().distinct().collect(Collectors.toList());

            isTaskInCurrent(collect);
            map.put("children", collect);
            //加入子节点
            childList.add(map);
        }
        //遍历子节点，继续递归判断每个子节点是否还含有子节点
        for (Map<String, Object> map : childList) {
            List<Map<String, Object>> list = getChild(filterMap, (List<Task>) map.get("children"));
            map.put("children", list);
        }
        return childList;
    }

    private void assembleBlankResult(Map<String, Object> result) {
        Page<Task> page = taskMapper.listByBlankTasks();
        initMap(result);
        result.put("result", page);
    }

    private void initMap(Map<String, Object> result) {
        result.put("ONLINE", 0);
        result.put("SUCCESS", 0);
        result.put("FAILED", 0);
        result.put("CANCELED", 0);
        result.put("RUNNING", 0);
        result.put("TOTAL", 0);
    }

    private void produceOfflineTaskInstanceMap(List<Task> list, List<Map<String, Object>> resultMap) {
        Map<Integer, Map<String, Integer>> countUpAndDownMap = new HashMap<>(16);
        list.forEach(task -> produceCountUpAndDownMap(task, countUpAndDownMap));
        produceOfflineTaskInstanceMap(list, resultMap, countUpAndDownMap);
    }

    private void produceOfflineTaskInstanceMap(List<Task> list, List<Map<String, Object>> resultMap, Map<Integer, Map<String, Integer>> countUpAndDownMap) {
        List<Task> offlineTasks = list.stream().filter(task -> !task.getIsStreamingTemplateCode()).collect(Collectors.toList());
        if (offlineTasks.size() == 0) {
            return;
        }

        Map<String, TaskInstance> state2TaskInstanceMap = getTaskName2TaskInstanceMap(offlineTasks);
        for (Task task : offlineTasks) {
            Map<String, Object> map = new HashMap<>(16);
            produceReturnedTaskMap(task, map);

            Map<String, Integer> countMap = countUpAndDownMap.get(task.getId());
            Integer up = countMap == null ? 0 : (countMap.get("up") == null ? 0 : countMap.get("up"));
            Integer down = countMap == null ? 0 : (countMap.get("down") == null ? 0 : countMap.get("down"));

            map.put("up", up);
            map.put("down", down);

            TaskInstance taskInstance = state2TaskInstanceMap.computeIfPresent(task.getName(), (k, v) -> v);
            if (taskInstance == null || StringUtils.isEmpty(taskInstance.getState())) {
                map.put("statusCode", task.getStatusCode());
                map.put("taskInstance", taskInstance);
                resultMap.add(map);
                continue;
            }

            map.put("statusCode", taskInstance.getState().toUpperCase());
            map.put("taskInstance", taskInstance);
            resultMap.add(map);
        }
    }

    private void produceReturnedTaskMap(Task task, Map<String, Object> map) {
        map.put("isStreamingTemplateCode", task.getIsStreamingTemplateCode());
        map.put("id", task.getId());
        map.put("name", task.getName());
        String tableName = getQualifiedName(task.getOutputDataset());
        map.put("table", tableName);
    }

    private Map<String, TaskInstance> getTaskName2TaskInstanceMap(List<Task> offlineTasks) {
        String names = getNames(offlineTasks);
        List<TaskInstance> taskInstances = batchGetTaskInstances(names);
        return getTaskName2TaskInstance(taskInstances);
    }

    private Map<String, TaskInstance> getTaskName2TaskInstance(List<TaskInstance> taskInstances) {
        Map<String, TaskInstance> taskStateMap = new HashMap<>(taskInstances.size());
        taskInstances.stream().forEach(taskInstance -> taskStateMap.put(taskInstance.getName(), taskInstance));
        return taskStateMap;
    }

    public List<TaskInstance> batchGetTaskInstances(String names) {
        long s5 = System.currentTimeMillis();

        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.getDagRunLatest(
                TaskSchedulerApi.DagRunLatestRequest
                        .newBuilder()
                        .setNames(names)
                        .build()
        );
        long s6 = System.currentTimeMillis();
        log.info("dagruns/laststatus接口耗时:" + (s6 - s5));

        if (taskCommonResponse == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        return JSON.parseArray(taskCommonResponse.getData(), TaskInstance.class);
    }

    private Map<String, List<TaskInstance>> batchGet7TaskInstances(List<Task> tasks) {
        if (tasks.size() > 0) {
            String names = tasks.stream().map(Task::getName).collect(Collectors.joining(","));
            long s5 = System.currentTimeMillis();
            TaskSchedulerApi.TaskCommonResponse taskCommonResponse = RetryBlockingStub.executeWithRetry(() ->
                    taskSchedulerRpcApiBlockingStub.getLast7Status(TaskSchedulerApi.Last7StatusRquest
                            .newBuilder()
                            .setNames(names)
                            .build()));

            long s6 = System.currentTimeMillis();
            log.info("dagruns/last7instancestatus接口耗时:" + (s6 - s5));
            if (taskCommonResponse == null) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
            }
            JSONObject data = JSON.parseObject(taskCommonResponse.getData());
            Map<String, List<TaskInstance>> result = new HashMap<>(data.keySet().size());
            data.keySet().stream().forEach(taskName -> {
                String name = data.getString(taskName);
                if (name != null && !name.isEmpty()) {
                    List<TaskInstance> taskInstances = JSON.parseArray(name, TaskInstance.class)
                            .stream()
                            .sorted(Comparator.comparing(TaskInstance::getExecutionDate))
                            .collect(Collectors.toList());
                    result.put(taskName, taskInstances);
                }
            });
            return result;
        } else {
            return new HashMap<>();
        }
    }

    private String getNames(List<Task> offlineTasks) {
        List<String> namesList = offlineTasks.stream().map(Task::getName).collect(Collectors.toList());
        return String.join(",", namesList);
    }

    private void produceCountUpAndDownMap(Task task, Map<Integer, Map<String, Integer>> countUpAndDownMap) {
        Integer parent = StringUtils.isEmpty(task.getInputGuids()) ? 0 : taskMapper.countParentTasks(task.getId(), task.getInputGuids());
        Integer child = StringUtils.isEmpty(task.getOutputGuids()) ? 0 : taskMapper.countChildTasks(task.getId(), task.getOutputGuids());

        HashMap<String, Integer> countMap = new HashMap<>(2);
        countMap.put("up", parent);
        countMap.put("down", child);
        countUpAndDownMap.put(task.getId(), countMap);
    }

    private void assembleRealtimeTask(List<Map<String, Object>> resultMap, Task task) {
        Map<Integer, Map<String, Integer>> countUpAndDownMap = new HashMap<>(2);

        produceCountUpAndDownMap(task, countUpAndDownMap);
        assembleRealtimeTask(resultMap, task, countUpAndDownMap);
    }

    private void assembleRealtimeTask(List<Map<String, Object>> resultMap, Task task, Map<Integer, Map<String, Integer>> countUpAndDownMap) {
        if (!isStreaming(task)) {
            return;
        }

        Map<String, Object> map = new HashMap<>(5);
        produceReturnedTaskMap(task, map);

        Map<String, Integer> countMap = countUpAndDownMap.get(task.getId());
        Integer up = countMap == null ? 0 : (countMap.get("up") == null ? 0 : countMap.get("up"));
        Integer down = countMap == null ? 0 : (countMap.get("down") == null ? 0 : countMap.get("down"));

        map.put("up", up);
        map.put("down", down);

        // 实时
        TaskInstance taskInstance = taskInstanceService.getOnlyLatestJobByTaskId(task.getId());
        if (taskInstance == null) {
            map.put("statusCode", task.getStatusCode());
        } else {
            map.put("statusCode", taskInstance.getStatusCode());
        }
        map.put("taskInstance", taskInstance);
        resultMap.add(map);
    }

    private void produceSourceAndTarget(Map<String, Integer> map, Boolean isChild, List<Map<String, Integer>> result, Integer id1, Integer id2) {
        if (isChild) {
            map.put("source", id1);
            map.put("target", id2);
        } else {
            map.put("source", id2);
            map.put("target", id1);
        }
        result.add(map);
    }

    private Long countStatus(Map<String, Long> statusMap, String status) {
        Long result = 0L;

        List<Long> statusNumList = statusMap.entrySet().stream().filter(e -> {
            String key = e.getKey();
            return JobStatus.RUNNING.name().equalsIgnoreCase(key)
                    || JobStatus.FAILING.name().equalsIgnoreCase(key)
                    || JobStatus.CANCELLING.name().equalsIgnoreCase(key)
                    || JobStatus.RESTARTING.name().equalsIgnoreCase(key)
                    || JobStatus.RECONCILING.name().equalsIgnoreCase(key)
                    || DsTaskConstant.JOB_STATUS_INITIALIZING.equalsIgnoreCase(key);
        }).map(Map.Entry::getValue).collect(Collectors.toList());


        if (statusNumList.size() != 0L) {
            result = statusNumList.stream().reduce(Long::sum).get();
        }
        return result;
    }

    private void produceUpAndDown(Task task, List<Map<String, Integer>> result, Integer upDown) {
        switch (upDown) {
            case 0:
                produceUpTasks(task, result);
                produceDownTasks(task, result);
                break;
            case 1:
                produceUpTasks(task, result);
                break;
            case 2:
                produceDownTasks(task, result);
                break;
        }
    }

    private void produceDownTasks(Task task, List<Map<String, Integer>> result) {
        if (StringUtils.isEmpty(task.getOutputGuids())) {
            return;
        }
        List<Task> tasks = taskMapper.selectChildDependendcies(task.getId(), task.getOutputGuids());

        for (Task tmp : tasks) {
            Map<String, Integer> map = new HashMap<>(2);
            produceSourceAndTarget(map, true, result, task.getId(), tmp.getId());
        }
    }

    private void produceUpTasks(Task task, List<Map<String, Integer>> result) {
        if (StringUtils.isEmpty(task.getInputGuids())) {
            return;
        }
        List<Task> tasks = taskMapper.selectParentDependendcies(task.getId(), task.getInputGuids());
        for (Task tmp : tasks) {
            Map<String, Integer> map = new HashMap<>(2);
            produceSourceAndTarget(map, false, result, task.getId(), tmp.getId());
        }
    }

    private Task getByIdOrName(Object id, String name) {
        Task task = null;
        if (id != null) {
            task = super.getById(id);
        } else if (StringUtils.isNoneEmpty(name)) {
            task = super.getByName(name);
        }

        if (task == null || task.getDeleteStatus() == 1) {
            return null;
        }

        String uuid = InfTraceContextHolder.get().getUuid();
        Boolean admin = InfTraceContextHolder.get().getAdmin();
        if (!admin && StringUtils.isNotEmpty(task.getUserGroup()) && !task.getUserGroup().equals(uuid)
                && !uuid.equals("INNER_SCHEDULE")) {
            throw new ServiceException(BaseResponseCodeEnum.SELECT_FAIL);
        }

        compatibleRuntimeConfig(task);
        setCanEdit(task);

        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        if (StringUtils.isNotEmpty(task.getDependArtifacts())) {
            CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
            String region = defaultRegionConfig.getRegionAlias();
            if (StringUtils.isNotEmpty(runtimeConfigObject.getString("sourceRegion"))) {
                region = runtimeConfigObject.getString("sourceRegion");
            }
            if (isStreaming(task) && StringUtils.isNotEmpty(runtimeConfigObject.getString("region"))) {
                region = runtimeConfigObject.getString("region");
            }
            List<ArtifactVersion> list = artifactVersionService.getDisplayArtifact(task.getDependArtifacts(), region);
            task.setDisplayDependJars(list);
        }
        // 回显任务的归属文件夹id
        FileManager fm = fileManagerMapper.selectByEntityId(task.getId(), FileManager.Module.TASK.name());
        if (fm != null) {
            task.setFolderId(fm.getParentId());
        }
        return task;
    }


    public void compatibleRuntimeConfig(Task task){
        if ("Mysql2Hive".equals(task.getTemplateCode())){
            String runtimeConfig = task.getRuntimeConfig();
            JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
            if(!(Boolean) runtimeConfigObject.getOrDefault("isTableList",false)){
                runtimeConfigObject.put("isTableList",true);
                runtimeConfigObject.put("syncType",1);
                runtimeConfigObject.put("table_type","single");
                ArrayList<JSONObject> tables = new ArrayList<>();
                JSONObject table = new JSONObject();
                table.put("sourceTable",runtimeConfigObject.getString("sourceTable"));
                table.put("partitions",runtimeConfigObject.getString("partitions"));
                table.put("targetTable",runtimeConfigObject.getString("targetTable"));
                table.put("existTargetTable",(Boolean)runtimeConfigObject.getOrDefault("existTargetTable",true));
                List<Column> columns = JSON.parseArray(runtimeConfigObject.getString("columns"), Column.class);
                table.put("columns",columns);
                tables.add(table);
                runtimeConfigObject.put("tables",tables);
                task.setRuntimeConfig(JSONObject.toJSONString(runtimeConfigObject));
            }

        }
    }


    private void produceOutputGra(Task task) {
        if (isStreaming(task)) {
            return;
        }
        // 兼容旧的任务生成接口
        if (task.getTriggerParam() == null || task.getTriggerParam().isEmpty()) {
            Stack<SchedulerCycleEnum> inputMinCycleStack = new Stack<>();
            inputMinCycleStack.push(SchedulerCycleEnum.YEARLY);

            TriggerParam triggerParam = new TriggerParam();
            String inputDataset = task.getInputDataset();
            List<Dataset> inputDatasets = JSON.parseArray(inputDataset, Dataset.class);
            if (inputDatasets != null && !inputDatasets.equals("[]")) {
                inputDatasets.forEach(x -> {
                    SchedulerCycleEnum inputCycle = SchedulerCycleEnum.valueOf(x.getGranularity().toUpperCase());
                    SchedulerCycleEnum inputMinCycle = inputMinCycleStack.pop();
                    if (!inputCycle.compare(inputMinCycle)) {
                        inputMinCycleStack.push(inputCycle);
                    } else {
                        inputMinCycleStack.push(inputMinCycle);
                    }
                });
            }

            String eventDepends = task.getEventDepends();
            List<EventDepend> eventDependsList = JSON.parseArray(eventDepends, EventDepend.class);
            if (eventDependsList != null && !eventDependsList.equals("[]") && !eventDependsList.isEmpty()) {
                eventDependsList.forEach(x -> {
                    SchedulerCycleEnum inputCycle = SchedulerCycleEnum.valueOf(x.getGranularity().toUpperCase());
                    SchedulerCycleEnum inputMinCycle = inputMinCycleStack.pop();
                    if (!inputCycle.compare(inputMinCycle)) {
                        inputMinCycleStack.push(inputCycle);
                    } else {
                        inputMinCycleStack.push(inputMinCycle);
                    }
                });
            }
            triggerParam.setType(DsTaskConstant.DATA_TRIGGER);
            triggerParam.setOutputGranularity(inputMinCycleStack.pop().getType());
            task.setTriggerParam(JSON.toJSONString(triggerParam).replace("output_granularity", "outputGranularity"));
            task.setDependTypes("[\"dataset\",\"event\"]");
        }
    }

    private String getQualifiedName(String datasetStr) {
        List<Dataset> datasets = JSON.parseArray(datasetStr, Dataset.class);
        if (datasets.size() == 0) {
            return "";
        }
        return datasets.stream()
                .map(data -> StringUtils.isNotEmpty(data.getId()) ? data.getId() : data.getMetadata().toString())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
    }

    private void preCheckName(Task task) {
        if (isStreaming(task)) {
            if (!match(task.getName(), DsTaskConstant.STREAM_TASK_NAME_PATTERN)) {
                throw new ServiceException(BaseResponseCodeEnum.STREAM_TASK_NAME_NOT_MATCH);
            }
        } else {
            if (!match(task.getName(), DsTaskConstant.SCHEDULED_APPLICATION_NAME_PATTERN)) {
                throw new ServiceException(BaseResponseCodeEnum.OFFLINE_TASK_NAME_NOT_MATCH);
            }
        }
    }

    @Override
    public void batchUpdateRole(String owner, String[] collaborators, String[] taskNames) {
        log.info("batch update role ,owner:{} ,collaborators:{}", owner, StringUtils.join(collaborators, ","));
        List<Task> taskLists = new ArrayList<>();
        Arrays.stream(taskNames).forEach(taskName -> {
            Task task = getByName(taskName);
            String runtimeConfigJson = task.getRuntimeConfig();
            JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
            if (owner != null && !owner.isEmpty()) {
                runtimeConfigObject.put("owner", owner);
                task.setRuntimeConfig(runtimeConfigObject.toJSONString());
                task.setCreateBy(owner);
            }
            if (collaborators != null && collaborators.length > 0) {
                runtimeConfigObject.put("collaborators", collaborators);
                task.setRuntimeConfig(runtimeConfigObject.toJSONString());
                task.setCollaborators(StringUtils.join(collaborators, ","));
            }
            taskLists.add(task);
        });

        super.update(taskLists);
    }

    @Override
    public BaseResponse getIams(String tenancyCode) {
        HashMap<String, Object> map = new HashMap<>(2);
        if (DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
          return  BaseResponse.success(map);
        }
        map.put(DsTaskConstant.HUAWEI, ScmpUtil.getIam(DsTaskConstant.HUAWEI, tenancyCode));
        map.put(DsTaskConstant.AWS, ScmpUtil.getIam(DsTaskConstant.AWS, tenancyCode));
        return BaseResponse.success(map);
    }

    @Override
    public void copy(Integer id, String name) {
        Task task = checkExist(id);
        AuditlogUtil.auditlog(DsTaskConstant.TASK, task.getId(), BaseActionCodeEnum.COPY, "任务复制");
        copyTask(task, name);
    }

    public void copyTask(Task task, String name) {
        copyTask(task, name, null);
    }

    public void copyTask(Task task, String name, String flage) {
        task.setName(name);
        //校验格式
        preCheckName(task);
        //Name不重复校验
        super.checkOnUpdate(super.getByName(task.getName()), task);

        JSONObject jsonObject = JSONObject.parseObject(task.getRuntimeConfig());
        if (StringUtils.isEmpty(flage)) {
            jsonObject.put("owner", InfTraceContextHolder.get().getUserName());
        }
        log.info("copy owner is " + InfTraceContextHolder.get().getUserName());
        String json = JSON.toJSONString(jsonObject);
        task.setRuntimeConfig(json);

        task.setId(null);
        task.setStatusCode(JobStatus.CREATED.name());
        task.setOnline(0);
        if (StringUtils.isEmpty(flage)) {
            task.setCreateBy(InfTraceContextHolder.get().getUserName());
        }
        task.setCreateTime(new Timestamp(System.currentTimeMillis()));
        task.setUpdateBy(null);
        task.setUpdateTime(null);
        task.setCurrentVersion(1);
        if (StringUtils.isNotEmpty(flage)) {
            return;
        }
        Task byName = super.getByName(name);
        super.checkOnUpdate(byName, task);
        super.save(task);
        Task selectTask = getBaseMapper().selectByName(name);
        addTaskVersionAndAudit(selectTask, BaseActionCodeEnum.COPY.name());
    }

    @Override
    public BaseResponse getStatistic() {
        HashMap<String, Integer> map = new HashMap<>(2);
        map.put("newTaskCount", taskMapper.getNewTaskCount());
        map.put("newArtifactCount", artifactVersionService.getNewArtifactVersionCount());
        return BaseResponse.success(map);
    }

    private Task checkAutoScaleModeAndCount(Integer id, Integer count) {
        if (count <= 0) {
            throw new ServiceException(BaseResponseCodeEnum.TASKMANAGER_NUM_GREATER_THAN_ZERO);
        }
        return checkAutoScaleTask(id);
    }

    private Task checkAutoScaleTask(Integer id) {
        Task task = checkExist(id);
        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        if (!runtimeConfig.getAdvancedParameters().getIsAutoScaleMode()) {
            log.error("task: " + id + " not support autoscale mode!");
            throw new ServiceException(BaseResponseCodeEnum.TASK_NOT_SUPPORT_AUROSCALE, "task id is " + id);
        }
        return task;
    }

    private void assembleResult(Map<String, Object> result, List<Map<String, Object>> taskStatusCountList) {
        Map<String,Integer> map = new HashMap<>();
        Integer total = taskStatusCountList.stream().mapToInt(item ->{
            Integer num = Integer.parseInt(item.get("num").toString());
            map.put(item.get("status_code").toString(),num);
            return num;
        }).reduce(Integer::sum).orElse(0);
        int unexecuted = total - map.getOrDefault("SUCCESS",0) - map.getOrDefault("FAILED", 0) -map.getOrDefault("RUNNING", 0);

        // 新版page接口方案
        result.put("SUCCESS", map.getOrDefault("SUCCESS",0));
        result.put("FAILED", map.getOrDefault("FAILED", 0));
        result.put("RUNNING", map.getOrDefault("RUNNING", 0));
        result.put("UNEXECUTED",unexecuted);
        result.put("TOTAL", total);
    }

    @Override
    public Task selectByOutputGuid(String output) {
        return taskMapper.selectOne((Task) new Task().setOutputGuids(output));
    }

    @Override
    public HashMap<String, Integer> getTaskIndicators() {
        HashMap<String, Integer> res = new HashMap<>();
        res.put(DsIndicatorsEnum.NEW_TASK_COUNT.name(), taskMapper.getNewTaskCount());
        res.put(DsIndicatorsEnum.ACC_TASK_COUNT.name(), taskMapper.getAccTaskCount());
        res.putAll(operateLogService.getCumulativeUsers());
        return res;

    }


    @Override
    public String renderContent(String content,String taskName){
        String name = taskName != null ? taskName : "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String renderContent = schedulerService.render(content, df.format(new Date()),name);
        return renderContent;
    }

    public String renderSql(String content, String executionDate){
        String renderContent = "";
        if (StringUtils.isEmpty(executionDate)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            executionDate = df.format(new Date());
        }
        renderContent = schedulerService.render(content, executionDate, "");
        return renderContent;
    }

    @Override
    public Map<String, Object> getDatasetInfo(String type, String region, String source, String db, String tbl, String qualifyname) {
        Map<String, Object> result = new HashMap<>();
        String metadataId = "";

        if (qualifyname != null && !qualifyname.isEmpty()) {
            metadataId = qualifyname;
        } else {
            switch (type) {
                case "mysql":
                case "clickhouse":
                    metadataId = String.format("%s.%s.%s.%s.%s", type, region, source, db.trim(), tbl.trim());
                    break;
                case "kafka":
                    metadataId = String.format("%s.%s.%s.%s", type, region, source, tbl.trim());
                    break;
                case "hive":
                    metadataId = String.format("%s.%s@%s", db, tbl.trim(), region);
                    break;
                case "metis":
                    metadataId = tbl.trim();
                    break;
                default:
                    throw new ServiceException(BaseResponseCodeEnum.DATASET_TYPE_FAULT);
            }
        }
        JSONObject respData = schedulerService.getDatasetInfo(metadataId);

        if (respData.containsKey("is_external") && respData.containsKey("granularity")) {
            Integer isExternal = respData.getInteger("is_external");
            String granularity = respData.get("granularity").toString();

            ArrayList<String> checkPath = new ArrayList<>();
            if (respData.containsKey("check_path")) {
                JSONArray upstreamPaths = respData.getJSONArray("check_path");
                for (int i = 0; i < upstreamPaths.size(); i++) {
                    checkPath.add((String) upstreamPaths.get(i));
                }
            }

            List<String> allowDependGrans = new ArrayList<>();

            if (isExternal == 0) {
                switch (granularity) {
                    case "minutely":
                    case "hourly":
                        // 暂时不在这里提供minutely的选项
                        allowDependGrans.add("hourly");
                        allowDependGrans.add("daily");
                        allowDependGrans.add("weekly");
                        allowDependGrans.add("monthly");
                        break;
                    case "daily":
                        allowDependGrans.add("daily");
                        allowDependGrans.add("weekly");
                        allowDependGrans.add("monthly");
                        break;
                    case "weekly":
                        allowDependGrans.add("weekly");
                        allowDependGrans.add("monthly");
                        break;
                    case "monthly":
                        allowDependGrans.add("monthly");
                        break;
                    default:
                        log.error(String.format("granularity: %s", granularity));
                        throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format(" granularity:%s", granularity));
                }
            } else {
                allowDependGrans.add("hourly");
                allowDependGrans.add("daily");
                allowDependGrans.add("weekly");
                allowDependGrans.add("monthly");
            }

            result.put("checkPath", checkPath);
            result.put("isExternal", isExternal);
            result.put("granularity", granularity);
            result.put("allowDependGran", allowDependGrans);
        } else {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("缺少数据返回值"));
        }

        return result;

    }

    @Override
    public Map<String, Object> getBackFillDateDetail(Integer taskId) {
        Task task = checkExist(taskId);
        String triggerParamString = task.getTriggerParam();
        TriggerParam triggerParam = JSON.parseObject(triggerParamString, TriggerParam.class);
        String outputGranularity = triggerParam.getOutputGranularity();
        List<String> allowDependGrans = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        switch (outputGranularity) {
            case "minutely":
            case "hourly":
                // 暂时不在这里提供minutely的选项
                allowDependGrans.add("hourly");
                allowDependGrans.add("daily");
                allowDependGrans.add("weekly");
                allowDependGrans.add("monthly");
                break;
            case "daily":
                allowDependGrans.add("daily");
                allowDependGrans.add("weekly");
                allowDependGrans.add("monthly");
                break;
            case "weekly":
                allowDependGrans.add("weekly");
                allowDependGrans.add("monthly");
                break;
            case "monthly":
                allowDependGrans.add("monthly");
                break;
            default:
                log.error(String.format("granularity: %s", outputGranularity));
                throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format(" granularity:%s", outputGranularity));
        }
        result.put("granularity", outputGranularity);
        result.put("allowDependGran", allowDependGrans);
        return result;
    }

    @Override
    public Map<Integer, List<TaskInstance>> getLast7State(String ids) {
        Map<Integer, List<TaskInstance>> result = new HashMap<>();
        if (StringUtils.isEmpty(ids)) {
            return result;
        }

        List<String> list = Arrays.asList(ids.split(","));
        List<Task> tasks = taskMapper
                .getLast7State(list);
        List<Task> offline = tasks.stream()
                .filter(task -> !isStreaming(task))
                .collect(Collectors.toList());

        Map<String, List<TaskInstance>> stringListMap = batchGet7TaskInstances(offline);

        offline.forEach(task -> {
            String name = task.getName();
            List<TaskInstance> taskInstances = stringListMap.computeIfPresent(name, (k, v) -> v);
            result.put(task.getId(), taskInstances);
        });
        return result;
    }

    @Override
    public List<Task> queryByIds(List<Integer> ids) {
        return taskMapper.queryByIds(ids);
    }

    @Override
    public Map<String, Object> getEtlSqlTbl(String sql, String region) {
        Map<String, Object> result = new HashMap<>();
        String decodeSql = URLDecoder.decode(new String(Base64.getDecoder().decode(sql.getBytes())));
        log.info("--decode-sql: " + decodeSql);
        decodeSql = SparkSqlParseUtil.filterSqlComments(decodeSql.trim());
        log.info("--filterSqlComments-sql: " + decodeSql);
        String formattedSql = SparkSqlParseUtil.sqlFormat(decodeSql);
        List<String> outputTbls = SparkSqlParseUtil.getOutputs(formattedSql);
        List<String> inputTbls = SparkSqlParseUtil.getInputs(formattedSql);

        List<Map<String, String>> output = outputTbls.stream().map(x -> etlSqlTableSplit(x, region)).distinct().collect(Collectors.toList());
        List<Map<String, String>> intput = inputTbls.stream().map(x -> etlSqlTableSplit(x, region)).distinct().collect(Collectors.toList());

        result.put("inputTbls", intput);
        result.put("outputTbls", output);
        String context = "";
        if (sql.contains("prev_execution_date") || sql.contains("prev_2_execution_date") || sql.contains("prev_2_execution_date")) {
            context = "sql中包含prev_execution_date｜prev_2_execution_date｜prev_2_execution_date特殊变量，请自行设置任务offset";
        }
        result.put("context", context);
        return result;
    }

    public Boolean checkPath(String path) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String renderPath = null;
        if (path.contains("prev_execution_date") || path.contains("prev_2_execution_date") || path.contains("prev_execution_date_utc0") || path.contains("prev_2_execution_date_utc0")
                || path.contains("next_execution_date") || path.contains("next_execution_date_utc0")) {
            return true;
        }
        try {
            renderPath = schedulerService.render(path, df.format(new Date()), "");
        } catch (Exception e) {
            return false;
        }
        log.info(renderPath);
        Pattern compile = Pattern.compile("[\\s{}#*()]");
        Matcher matcher = compile.matcher(renderPath);
        if (matcher.find() || !(renderPath.startsWith("s3://") || renderPath.startsWith("obs://") || renderPath.startsWith("gs://"))) {
            return false;
        }
        if (renderPath.contains("[") || renderPath.contains("]")) {
            Pattern compile1 = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Matcher matcher1 = compile1.matcher(renderPath);
            if (!matcher1.find()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getOffset(String table, String granularity, String sql) {
        String decodeSql = URLDecoder.decode(new String(Base64.getDecoder().decode(sql.getBytes())));
        String formattedSql = SparkSqlParseUtil.sqlFormat(decodeSql);
        System.out.println(formattedSql);
        String[] splitSql = formattedSql.split(table);
        splitSql = Arrays.copyOfRange(splitSql, 1, splitSql.length);
        String timeList = Arrays.stream(splitSql).map(data -> {
            String dateTime = "";
            Pattern compile = Pattern.compile("(\\{\\{.*?}})");
            Matcher matcher = compile.matcher(data);
            if (matcher.find()) {
                dateTime = matcher.group(1);
            }
            String renderContext = dateTime.replaceAll("\\(['\"]%Y-%m-%d['\"]\\)", "('%Y%m%d%H')")
                    .replaceAll("\\{\\{ ds }}|\\{\\{ ds_nodash }}", "{{ execution_date.strftime('%Y%m%d%H') }}")
                    .replaceAll("\\{\\{ yesterday_ds }}|\\{\\{ yesterday_ds_nodash }}", "{{ (execution_date - macros.timedelta(days=1)).strftime('%Y%m%d%H') }}")
                    .replaceAll("\\{\\{ tomorrow_ds }}|\\{\\{ tomorrow_ds_nodash }}", "{{ (execution_date + macros.timedelta(days=1)).strftime('%Y%m%d%H') }}")
                    .replaceAll("\\(['\"]%Y%m%d['\"]\\)", "('%Y%m%d%H')");
            return renderContext;
        }).collect(Collectors.joining(","));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String renderTime = schedulerService.render(timeList, df.format(new Date()), "");

        long offset = Arrays.stream(renderTime.split(SymbolEnum.COMMA.getSymbol())).mapToLong(data ->
            getOffsetByGranularity(data, granularity)).min().orElse(Integer.MAX_VALUE);
        return offset == Integer.MAX_VALUE ? "null" : offset + "";
    }

    private Map<String, String> etlSqlTableSplit(String table, String region) {
        Map<String, String> tblInfo = new HashMap<>();
        String[] splitArr = table.split("\\.");
        if (splitArr.length < 2) {
            return tblInfo;
        }
        if (table.startsWith("iceberg") && splitArr.length == 3) {
            tblInfo.put("db", splitArr[1]);
            tblInfo.put("tbl", splitArr[2]);
            List<Task> tasks = getTaskByMetadataId(String.format("%s.%s@%s", splitArr[1], splitArr[2], region));
            if (tasks.size() > 0) {
                Task task = tasks.get(0);
                if(!isStreaming(task)) {
                    //兼容旧历史任务
                    produceOutputGra(task);
                    TriggerParam triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
                    if (triggerParam != null) {
                        tblInfo.put("granularity", triggerParam.getOutputGranularity());
                    }
                    List<Dataset> datasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
                    tblInfo.put("offset", datasets.get(0).getOffset() + "");
                    tblInfo.put("name", task.getName());
                    tblInfo.put("id", task.getId() + "");
                    tblInfo.put("metadataId", datasets.get(0).getId());
                }
            }
        } else {
            tblInfo.put("db", splitArr[0]);
            tblInfo.put("tbl", splitArr[1]);
            List<Task> tasks = getTaskByMetadataId(String.format("%s.%s@%s", splitArr[0], splitArr[1], region));
            if (tasks.size() > 0) {
                Task task = tasks.get(0);
                if(!isStreaming(task)) {
                    //兼容旧历史任务
                    produceOutputGra(task);
                    TriggerParam triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
                    if (triggerParam != null) {
                        tblInfo.put("granularity", triggerParam.getOutputGranularity());
                    }
                    List<Dataset> datasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
                    tblInfo.put("offset", datasets.get(0).getOffset() + "");
                    tblInfo.put("name", task.getName());
                    tblInfo.put("id", task.getId() + "");
                    tblInfo.put("metadataId", datasets.get(0).getId());
                }
            }
        }
        return tblInfo;
    }

    @Override
    public String getCrontab(CrontabParam crontabParam) {
        if (crontabParam.getConvertUtc0() != null && crontabParam.getConvertUtc0()) {
            return CrontabUtil.getCrontabUtc0(crontabParam);
        } else {
            return CrontabUtil.getCrontab(crontabParam);
        }
    }

    @Override
    public void batchUpdateOwnerOrCollaborator(String taskIds, String owner, String collaborator) {
        if (StringUtils.isEmpty(taskIds)) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }

        String[] split = taskIds.split(SymbolEnum.COMMA.getSymbol());
        List<Integer> ids = Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());
        List<Task> tasks = taskMapper.selectWithIds(ids);

        tasks.forEach(task -> {
            RuntimeConfig runtimeConfig = JSON.parseObject(task.getRuntimeConfig(), RuntimeConfig.class);
            if (StringUtils.isNotEmpty(owner)) {
                // owner为空，表示不修改owner
                runtimeConfig.setOwner(owner);
                String runtimeConfigString = JSON.toJSONString(runtimeConfig);
                task.setRuntimeConfig(runtimeConfigString);
            }

            // 待添加的协作者是空的，那么task的协作者不变
            List<String> collaborators = runtimeConfig.getCollaborators();
            if (StringUtils.isEmpty(collaborator)) {
                return;
            }
            collaborators.add(collaborator);

            String collaboratorString = collaborators.stream()
                    .reduce((x, y) -> x + "," + y)
                    .orElseGet(() -> "");
            runtimeConfig.setCollaborators(collaborators);
            String runtimeConfigString = JSON.toJSONString(runtimeConfig);
            task.setRuntimeConfig(runtimeConfigString);
            task.setCollaborators(collaboratorString);
        });
        update(tasks);
    }

    @Override
    public void dateTransform(Integer id, String airflowCrontab, String newTaskName) throws Exception {
        Task task = super.getById(id);

        String sqlContent = task.getContent();
        try {
            // 需要做参数替换的有四个地方, content,inputdataset,outputdataset,mainClassArgs
            if (sqlContent != null) {
                sqlContent = URLDecoder.decode(new String(Base64.getDecoder().decode(sqlContent.getBytes())), "UTF-8");
            }
            String inputDataset = task.getInputDataset();
            String outputDataset = task.getOutputDataset();
            String mainClassArgs = task.getMainClassArgs();
            String runtimeConfig = task.getRuntimeConfig();
            TriggerParam triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
            boolean isDataTrigger = false;
            String taskGra = "";
            taskGra = triggerParam.getOutputGranularity();
            if (triggerParam.getType().equals(DsTaskConstant.DATA_TRIGGER)) {
                isDataTrigger = true;
            }
            String newSqlContent = schedulerService.dateTransform(sqlContent, isDataTrigger, taskGra, airflowCrontab, true);
            String newInputDataset = schedulerService.dateTransform(inputDataset, isDataTrigger, taskGra, airflowCrontab, false);
            String newOutputDataset = schedulerService.dateTransform(outputDataset, isDataTrigger, taskGra, airflowCrontab, false);
            String newMainClassArgs = schedulerService.dateTransform(mainClassArgs, isDataTrigger, taskGra, airflowCrontab, false);
            String newRuntimeconfig = schedulerService.dateTransform(runtimeConfig, isDataTrigger, taskGra, airflowCrontab, false);

            task.setContent(new String(Base64.getEncoder().encode(UriUtils.encode(newSqlContent, "utf-8").getBytes())));
            task.setInputDataset(newInputDataset);
            task.setOutputDataset(newOutputDataset);
            task.setMainClassArgs(newMainClassArgs);
            task.setRuntimeConfig(newRuntimeconfig);
            copyTask(task, newTaskName);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Boolean updateSeniorParam(Integer id, String sparkConfParam) {
        Task task = checkExist(id);
        Boolean flage = true;
        int maxVersionById = taskVersionService.getMaxVersionById(task.getId());
        if (maxVersionById > task.getCurrentVersion()) {
            flage = false;
            return flage;
        }
        String runtimeConfigJson = task.getRuntimeConfig();
        Map<String, Object> mapTypes = JSON.parseObject(runtimeConfigJson, Map.class);
        mapTypes.put("batchParams", sparkConfParam);
        JSONObject object = new JSONObject(mapTypes);
        task.setRuntimeConfig(object.toString());
        task.setAuditStatus(BaseActionCodeEnum.UPDATEANDSTART.name());
        update(task);
        return flage;
    }

    @Override
    public void updateByid(Task task) {
        getBaseMapper().updateByPrimaryKeySelective(task);
    }

    @Override
    public TaskVersion getTaskVersionInfo(Integer id, Integer version) {
        return taskVersionService.selectByIdAndVersion(id, version);
    }

    @Override
    public Task getTaskByVersion(Integer taskId, Integer version) {
        TaskVersion taskVersion = getTaskVersionInfo(taskId, version);
        Task task = Task.cloneByTaskVersion(taskVersion);

        compatibleRuntimeConfig(task);
        setCanEdit(task);
        return task;
    }

    @Override
    public List<Task> listByNames(List<String> names) {
        return taskMapper.selectWithNames(names);
    }

    @Override
    public List<Task> selectParentTask(String names) {
        return taskMapper.selectParentTask(names);
    }

    @Override
    public JSONObject getDatePreview(String taskGra, String taskCrontab, Dataset dataDepend, EventDepend taskDepend) {
        String dataDependStr = "";
        String taskDependStr = "";
        String taskCrontabStr = "";

        if (dataDepend != null) {
            Dataset.Metadata metadata = dataDepend.getMetadata();
            if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                dataDepend.setId(dataDepend.getId());
            } else {
                dataDepend.setId(metadata.toString());
            }
            dataDependStr = JSONObject.toJSONString(dataDepend);
        }
        if (taskDepend != null) {
            InfTraceContextHolder.get().setUuid("INNER_SCHEDULE");
            Task dependTask = this.getById(taskDepend.getTaskId());
            TriggerParam triParam = JSONObject.parseObject(dependTask.getTriggerParam(), TriggerParam.class);
            taskDepend.setCrontab(triParam.getCrontab());
            taskDependStr = JSONObject.toJSONString(taskDepend);
        }
        if (taskCrontab != null) {
            taskCrontabStr = taskCrontab;
        }
        JSONObject datePreview = schedulerService.getDatePreview(taskGra, taskCrontabStr, dataDependStr, taskDependStr);
        return datePreview;
    }

    @Override
    public void realtimeExecute(Integer taskId, String newArgs, String callbackUrl) {
        Task task = checkExist(taskId);
        if (isStreaming(task) || TemplateEnum.valueOf(task.getTemplateCode()) != TemplateEnum.SPARKJAR) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        // update args
        task.setMainClassArgs(newArgs);
        task.setUpdateBy("Push");
        task.setCallbackUrl(callbackUrl);
        task.setRealtimeExecute(true);
        super.update(task);

        //update and start scheduler
        try {
            start(task, null, null);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR);
        }
    }

    @Override
    public List<ScheduleJobOuterClass.ScheduleJob> toScheduleJobByIds(Collection<Pair<Integer, Integer>> taskVersionIds,
                                                                      boolean preExecute) {
        return CollectionUtils.emptyIfNull(taskVersionIds).stream()
                .map(item -> toScheduleJob(getTaskByVersion(item.getKey(), item.getValue()), preExecute))
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleJobOuterClass.ScheduleJob toScheduleJob(Task task, boolean preExecute) {
        try {
            Job job = JobFactory.getJob(task, null, null, this);
            if (!(job instanceof ScheduledJob)) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(),
                        String.format("任务【%s】不是离线任务，而是一个 %s 任务", task.getId(), task.getTemplateCode()));
            }

            if (preExecute) {
                job.beforeExec();
            }

            ScheduleJobOuterClass.TaskCode taskCode = ((ScheduledJob) job).toPbTaskCode();

            return ScheduleJobOuterClass.ScheduleJob.newBuilder()
                    .setTaskId(task.getId())
                    .setTaskName(task.getName())
                    .setTaskCode(taskCode)
                    .build();
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(),
                    String.format("任务 %s 转换为调度所需格式失败，请核实", task.getId()));
        }
    }

    @Override
    public Map<Integer, Task> getScheduleTaskListByUser(String userName) {
        Example example = new Example(Task.class);

        if (!InfTraceContextHolder.get().getUserInfo().isAdmin()){
            Example.Criteria userCriteria = example.createCriteria();
            userCriteria.orEqualTo("userGroup", InfTraceContextHolder.get().getUuid());
            example.and(userCriteria);
        }

        Example.Criteria commonCriteria = example.createCriteria();
        commonCriteria.andNotIn("templateCode", Arrays.asList("StreamingSQL", "StreamingJAR", "Metis2Hive", "MysqlCDC2Hive","QueryEdit"))
                .andEqualTo("workflowId", NumberUtils.INTEGER_ZERO)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        example.and(commonCriteria);

        return taskMapper.selectByExample(example).stream()
             .collect(HashMap::new, (m, t) -> m.put(t.getId(), t), HashMap::putAll);
    }

    @Override
    public void swapTaskKeys(List<Pair<Optional<String>, Integer>> taskKeyIdPairs,
                             BiFunction<String, Map<String, Integer>, List<EventDepend>> eventDependsParser) {
        Map<String, Integer> keyIdMap = taskKeyIdPairs.stream()
                .filter(item -> item.getKey().isPresent())
                .collect(HashMap::new, (m, pair) -> m.put(pair.getKey().get(), pair.getValue()), HashMap::putAll);

        if (MapUtils.isEmpty(keyIdMap)) {
            return;
        }

        Example example = new Example(Task.class);
        example.or().andIn("id", taskKeyIdPairs.stream().map(Pair::getValue).collect(Collectors.toSet()));
        List<Task> taskList = taskMapper.selectByExample(example);

        taskList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getEventDepends()))
                .forEach(item -> {
                    List<EventDepend> eventDependList = eventDependsParser.apply(item.getEventDepends(), keyIdMap);

                    Task toUpdateParam = new Task();
                    toUpdateParam.setId(item.getId());
                    toUpdateParam.setEventDepends(Jsons.serialize(eventDependList));
                    taskMapper.updateByPrimaryKeySelective(toUpdateParam);
                });
    }

    @Override
    public void checkOutputDataset(Integer taskId, String outputDataset) {
        List<Dataset> outputDatasetList = JSON.parseArray(outputDataset, Dataset.class);
        if (CollectionUtils.isEmpty(outputDatasetList)) {
            return;
        }

        outputDatasetList.forEach(dataset -> {
            Dataset.Metadata metadata = dataset.getMetadata();
            String outputDatasetId = "";
            if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                outputDatasetId = dataset.getId();
            } else {
                if (metadata.getType() == null) {
                    // 之前存在一部分dataset没有type传过来,这部分都是hive任务,这里给一个默认值
                    metadata.setType("hive");
                }
                outputDatasetId = metadata.toString();
            }

            List<Task> sameOutputTaskList = taskMapper.selectOfflineTaskWithSameOutputId(outputDatasetId);
            for (Task offline : sameOutputTaskList) {
                if (offline.getId().equals(taskId)) {
                    log.info("task id from web: " + taskId);
                    continue;
                }

                String offlineOutputDataset = offline.getOutputDataset();
                List<Dataset> outputs = JSON.parseArray(offlineOutputDataset, Dataset.class);
                if (outputs.stream().map(Dataset::getId).anyMatch(outputDatasetId::equalsIgnoreCase)) {
                    throw new ServiceException(BaseResponseCodeEnum.TASK_OUTPUT_DATASET_ERROR.name(),
                            String.format(BaseResponseCodeEnum.TASK_OUTPUT_DATASET_ERROR.getMessage(), offline.getName()));

                }
            }
        });
    }

    @Override
    public void superUpdate(Task data) {
        super.update(data);
    }

    @Override
    public List<String> getResultSet() {
        return Arrays.stream(TASK_RESULT).collect(Collectors.toList());
    }

    @Override
    public void fastBackfill(Integer taskd, String args, String callbackUrl,String startDate,String endDate) {
        Task task = taskMapper.getTaskNameById(taskd);
        TaskSchedulerApi.TaskCommonResponse response = taskSchedulerRpcApiBlockingStub.fastBackfill(
                TaskSchedulerApi.fastBackfillRequest
                        .newBuilder()
                        .setTaskId(taskd)
                        .setTaskName(task.getName())
                        .setArgs(args)
                        .setCallbackUrl(callbackUrl)
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build()
        );

        Integer code = response.getCode();
        if (code != 0) {
            String msg = response.getMessage();
            log.error(String.format("code: %d, message: %s", code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
        }
    }

    @Override
    public String eventTrigger(EventTrigger et){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("taskId", et.getTaskId().toString());
        if (et.getCallbackUrl() != null){
            paramMap.put("callbackUrl", et.getCallbackUrl());
        }
        if (et.getTemplateParams() != null){
            paramMap.put("templateParams", JSON.toJSONString(et.getTemplateParams()));
        }
        Map<String,String> headers = new HashMap<>();
        headers.put(CommonConstant.CURRENT_LOGIN_USER, JSONObject.toJSONString(InfTraceContextHolder.get().getUserInfo()));
        BaseResponse response = HttpUtil.doPost(DataCakeConfigUtil.getDataCakeServiceConfig().getPipelineHost() + "/pipeline/task/eventTrigger", paramMap, headers);
        if (response.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", response.getData()));
        }
        JSONObject jsonObject = JSON.parseObject((String) response.getData());
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            String msg = jsonObject.getString("msg");
            log.error(String.format("[%s]事件触发任务失败，code: %d, message: %s", et.getTaskId(),code, msg));
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR,  String.format("事件触发任务失败,%s", msg));
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            return data.getString("uuid");
        }
    }

    @Override
    public String getCommand(Integer taskId) {
        Task task = getById(taskId);
        if(isStreaming(task)){
            throw new ServiceException(BaseResponseCodeEnum.CANT_GET_STREAMING_CMD);
        }
        Job job = JobFactory.getJob(task, null, null, this);
        ScheduledJob scheduleJob = (ScheduledJob) job; // 强制类型转换
        return scheduleJob.getCommand();
    }

    @Override
    public void changeName(Integer taskId, String newName) {
        Task task = getById(taskId);
        String oldName = task.getName();
        task.setName(newName);
        preCheckName(task);
        if (!isStreaming(task) && task.getOnline() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.ONLINE_JOB_CANNOT_UPDATE_NAME);
        }
        TaskSchedulerApi.TaskCommonResponse taskCommonResponse = taskSchedulerRpcApiBlockingStub.taskUpdatename(
                TaskSchedulerApi.UpdateTaskNameRequest
                        .newBuilder()
                        .setNewName(newName)
                        .setOldName(oldName)
                        .build());
        if (taskCommonResponse.getCode() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", taskCommonResponse.getData()));
        }
        super.update(task);
    }

    @Override
    @Transactional
    public void batchUpdateAlert(BatchUpdateAlert batchUpdateAlert) {
        JSONObject alertModel = JSONObject.parseObject(batchUpdateAlert.getAlertModel());
        JSONObject alertModelInner = JSONObject.parseObject(batchUpdateAlert.getAlertModel());
        JSONObject regularAlert = JSONObject.parseObject(batchUpdateAlert.getRegularAlert());
        JSONObject regularAlertInner = JSONObject.parseObject(batchUpdateAlert.getRegularAlert());
        Integer[] ids = batchUpdateAlert.getIds();
        for (Integer id : ids) {
            Task task = taskService.getById(id);
            String runtimeConfig = task.getRuntimeConfig();
            JSONObject jsonObject = JSONObject.parseObject(runtimeConfig);
            JSONObject advancedParameters = jsonObject.getJSONObject("advancedParameters");
            advancedParameters.put("alertModel",alertModelInner);
            advancedParameters.put("regularAlert",regularAlertInner);
            jsonObject.put("alertModel",alertModel);
            jsonObject.put("regularAlert",regularAlert);
            System.out.println(jsonObject.toJSONString());
            Task newTask = new Task();
            newTask.setRuntimeConfig(jsonObject.toJSONString());
            Example taskExample = new Example(Task.class);
            taskExample.or()
                    .andEqualTo("id", id);
            taskMapper.updateByExampleSelective(newTask,taskExample);
        }
    }


}


