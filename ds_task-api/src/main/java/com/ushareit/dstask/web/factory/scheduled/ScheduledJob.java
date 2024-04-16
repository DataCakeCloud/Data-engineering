package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.controller.TableauDataSource;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.scheduled.param.*;
import com.ushareit.dstask.web.metadata.MetaDataManager;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.dstask.web.utils.ItUtil;
import com.ushareit.dstask.web.utils.ParseParamUtil;
import com.ushareit.engine.Context;
import com.ushareit.engine.constant.SourceEnum;
import com.ushareit.engine.datax.adapter.DataxAdapter;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public abstract class ScheduledJob implements Job {
    final Map<String, String> extraParam = new HashMap<>();
    TaskServiceImpl taskServiceImpl;
    Context context;
    String executeShell;
    String dataxExecuteShell;
    String notifiedOwner;
    String env;
    String group;
    Task task;
    String sparkConfig;
    String templateCode;
    ArrayList<Map<String, Object>> taskItems = new ArrayList<>();
    String name;
    String region;
    String provider = "huawei";
    String clusterSLA;
    String mainClass;
    String jarUrl;
    String commandTags;
    String clusterTags;
    String dependencies;
    String owner;
    String emails;
    int retries;
    int maxActiveRuns;
    int retryInterval;
    int executionTimeout;
    boolean emailOnRetry;
    boolean emailOnStart;
    boolean emailOnSuccess;
    boolean emailOnFailure;
    List<Dataset> inputDatasets;
    List<Dataset> outputDatasets;
    String startDate;
    String endDate;
    List<EventDepend> eventDepends;
    TriggerParam triggerParam;
    String dependTypes;
    Integer version;
    String lifecycle;
    boolean dingAlert = false;
    boolean phoneAlert = false;
    Integer groupId = 0;
    Integer tenantId = 1;
    String tenantName = "";
    String acrossCloud;
    String defaultDb = "defaultDb";
    String sqlEngine;
    Integer checkExpirationTime;
    Boolean dynamicsCmd = false;


    public ScheduledJob(Task task, TaskServiceImpl taskServiceImpl) {
        this.task = task;
        this.name = task.getName();
        this.taskServiceImpl = taskServiceImpl;
        String env = InfTraceContextHolder.get().getEnv();
        this.env = env.equals("dev") ? "test" : "prod";
        templateCode = task.getTemplateCode();
        this.mainClass = task.getMainClass();
        jarUrl = task.getJarUrl();
        this.version = task.getCurrentVersion();
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        String executeMode = runtimeConfigObject.getString("executeMode");
        setGroupIdAndTenantId(this.taskServiceImpl);

        if ("local".equals(executeMode)) {
            setRuntimeConfigNew(task);
        } else {
            setRuntimeConfig(task);
        }
        setInputDatasets(task);

        setOutputDatasets(task);

        setEventDepends();

        setDefaultDb(task);

        setTriggerParam();

        setDependTypes();

        //设置上下文
        setContext(task);

        // owner会用来发送报警,协作人也需要收到报警,追加到owner当中
        // 拼接sparkconfig时,只需要任务创建人. 调度模块需要协作人,所以在设置完sparkconfig后增加协作人
        if (task.getCollaborators() != null && task.getCollaborators().length() > 0) {
            this.notifiedOwner = this.owner + "," + task.getCollaborators();
        }
    }

    //设置上下文
    private void setContext(Task task) {
//        TemplateRegionImp sparkJar = taskServiceImpl.templateRegionImpService
//                        .selectOne(new TemplateRegionImp().setTemplateCode("SEATUNNEL-SPARK-3").setRegionCode(region));
        //目前只涉及出入仓模板
        String templateCode = task.getTemplateCode();
        if (!isDataExportAndIntoTem(templateCode)) {
            return;
        }

        Context context = null;
        JSONObject runtimeConfigObject = JSON.parseObject(task.getRuntimeConfig());
        String executeMode = runtimeConfigObject.getString("executeMode");
        if (executeMode != null) {
            com.ushareit.engine.param.RuntimeConfig newConfig = JSON.parseObject(task.getRuntimeConfig(), com.ushareit.engine.param.RuntimeConfig.class);
            Boolean autoCreateTable = newConfig.getCatalog().getTables().get(0).getAutoCreateTable();
            newConfig.getAdvancedParameters().setUserGroupName(InfTraceContextHolder.get().getCurrentGroup());
            if(!autoCreateTable){
                setTableDetail(newConfig);
            }
            String destinationId = newConfig.getDestinationId();
            String sourceId = newConfig.getSourceId();
            context = Context.builder().runtimeConfigStr(JSONObject.toJSONString(newConfig))
                    .sourceType(newConfig.getSourceType()).sourceConfigStr(getDataSourceConfig(sourceId))
                    .sinkType(newConfig.getDestinationType()).sinkConfigStr(getDataSourceConfig(destinationId))
                    .executeMode(newConfig.getExecuteMode()).build();
//                    .jarPath(sparkJar.getUrl()).build();
            String jobInfo = DataxAdapter.getJobInfo(context);
            log.info("-------------jobInfo------------");
            log.info(jobInfo);
            this.context = context;
            Integer bandwidth = getBandwidth(newConfig.getTaskParam().getBandwidth());
            try {
//                this.executeShell = SeaTunnelParser.getJobShell(context);
                this.dataxExecuteShell = DataxAdapter.getJobShell(jobInfo, bandwidth);
            } catch (Exception e) {
                throw new ServiceException(BaseResponseCodeEnum.SEATUNNEL_CONFIG_ANALYZE_FAIL, e);
            }
        }
    }

    private Integer getBandwidth(Integer bandwidth) {
        if (bandwidth >= 1) {
            return bandwidth;
        }

        return 1;
    }

    public Boolean isDataExportAndIntoTem(String templateCode) {
        switch (TemplateEnum.valueOf(templateCode)) {
            case Mysql2Hive:
            case Hive2Doris:
            case Hive2Mysql:
            case Oracle2Hive:
            case SqlServerHive:
            case Doris2Hive:
            case File2Lakehouse:
                return true;
            default:
                return false;
        }
    }

    public String getDataSourceConfig(String actorId) {
        if (StringUtils.isEmpty(actorId)) {
            throw new ServiceException(BaseResponseCodeEnum.SEATUNNEL_DATASOURCE_NULL);
        }
        return taskServiceImpl.actorService.getById(actorId).getConfiguration();
    }

    public void setTableDetail(RuntimeConfig runTimeConfig) {
        String db = null;
        String table = null;
        String sourceRegion = runTimeConfig.getSourceRegion();
        String owner = runTimeConfig.getAdvancedParameters().getOwner();
        Table tab = runTimeConfig.getCatalog().getTables().get(0);
        String sourceType = runTimeConfig.getSourceType().toLowerCase();
        String destinationType = runTimeConfig.getDestinationType().toLowerCase();
        if (sourceType.equals(SourceEnum.Iceberg.name().toLowerCase())) {
            db = runTimeConfig.getCatalog().getSourceDb();
            table = tab.getSourceTable();
        }
        if(destinationType.equals(SourceEnum.Iceberg.name().toLowerCase())){
            db = runTimeConfig.getCatalog().getTargetDb();
            table = tab.getTargetTable();
        }

//        if (destinationType.equals(SourceEnum.Iceberg.name().toLowerCase())) {
//            db = runTimeConfig.getCatalog().getTargetDb();
//            table = tab.getTargetTable();
//        }

        if (StringUtils.isEmpty(db) || StringUtils.isEmpty(table)) {
            return;
        }
        MetaDataParam metaDataParam = new MetaDataParam();
        metaDataParam.setRegion(sourceRegion).setDb(db).setTable(table).setMetaFlag(MetaDataManager.AIRBYTE).setUserName(owner)
                .setType("hive").setJudgeTable(false);
        String fileFormat = taskServiceImpl.lakecatutil.getFileFormat( tenantName, region, db, table);
        Map<String, String> tableParameters = taskServiceImpl.lakecatutil.getTableParameters(tenantName, region, db, table);
        if(tableParameters.containsValue("SNAPPY") || tableParameters.containsValue("SNAPPY")){
            tab.setCompress("SNAPPY");
        }else if(tableParameters.containsValue("gzip") || tableParameters.containsValue("GZIP")){
            tab.setCompress("GZIP");
        }else if(tableParameters.containsValue("bzip2") || tableParameters.containsValue("BZIP2")){
            tab.setCompress("BZIP2");
        }else{
            tab.setCompress("NONE");
        }
        tab.setFileFormat(fileFormat);
        String tableDelmiter = taskServiceImpl.lakecatutil.getTableDelmiter(tenantName, region, db, table);
        tab.setDelimiter(tableDelmiter);
        List<com.ushareit.dstask.web.ddl.metadata.Table> search = taskServiceImpl.metaDataService.search(metaDataParam, metaDataParam.getType(),metaDataParam.getActorId(), metaDataParam.getDb(), metaDataParam.getTable(), metaDataParam.getSourceParam());
        String location = search.stream().findFirst().orElse(null).getLocation();
        tab.setLocation(location);
        List<Column> columns = search.get(0).getColumns();  // List<com.ushareit.dstask.web.ddl.metadata.Column>

        List<com.ushareit.engine.param.Column> sourceTableColumn = new ArrayList<com.ushareit.engine.param.Column>();
        for (int i = 0; i < columns.size(); i++) {
            com.ushareit.engine.param.Column newColumn = new com.ushareit.engine.param.Column();
            newColumn.setColumnType(columns.get(i).getType());
            newColumn.setName(columns.get(i).getName());
            newColumn.setIndex(i);
            sourceTableColumn.add(newColumn);
        }
        tab.setSourceTableColumn(sourceTableColumn);  // List<com.ushareit.engine.param.Column>
    }

    public ScheduledJob() {

    }

    public static String OldCheckPathJoin(List<Dataset> inputDatasets) {
        inputDatasets.forEach(
                dataset -> {
                    String location = dataset.getLocation();
                    if (dataset.getCheckPath() == null || "".equals(dataset.getCheckPath()) || "null".equals(dataset.getCheckPath())) {
                        if (location == null || "null".equals(location)) {
                            location = "";
                        }
                        List<String> partitions = dataset.getPartitions();
                        String fileName = dataset.getFileName();
                        if (fileName == null || "null".equals(fileName)) {
                            fileName = "";
                        }
                        String partitionPath = "";
                        if (partitions != null) {
                            List<String> resultPartitions = new ArrayList<>();
                            partitions.stream().forEach(partition -> {
                                String[] partitionKV = partition.trim().split("=");
                                String value;
                                if (partitionKV.length == 2) {
                                    value = partitionKV[1];
                                    value = StringUtils.stripStart(value, "{{");
                                    value = StringUtils.stripEnd(value, "}}");
                                    if (value.contains("%")) {
                                        // 时间类型的数据拼接成[[%Y-%m-%d]]的格式
                                        resultPartitions.add(partitionKV[0] + "=[[" + value + "]]");
                                    } else {
                                        resultPartitions.add(partitionKV[0] + "=" + value);
                                    }
                                }
                            });
                            partitionPath = StringUtils.join(resultPartitions, "/");
                        }
                        if (!location.isEmpty() && !location.endsWith("/")) {
                            location = location + "/";
                        }
                        if (!partitionPath.isEmpty() && !partitionPath.endsWith("/")) {
                            partitionPath = partitionPath + "/";
                        }
                        dataset.setCheckPath(location + partitionPath + fileName);
                    }
                }
        );
        return JSON.toJSONString(inputDatasets);
    }


    void setGroupIdAndTenantId(TaskServiceImpl taskServiceImpl) {
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        String owner = DataCakeTaskConfig.getStringConfigValue(runtimeConfigJson, "owner");
        Integer tentanId = 1;
        Integer groupId = 0;
        AccessTenant accessTenant = new AccessTenant();

        List<AccessGroup> accessGroupList = taskServiceImpl.accessGroupService.selectByName(owner);
        List<Integer> collect = accessGroupList.stream().map(AccessGroup::getParentId).collect(Collectors.toList());

        List<AccessGroup> groupList = taskServiceImpl.accessGroupService.listByIds(collect);
        for (AccessGroup accessGroup : groupList) {
//            tentanId = accessGroup.getTenantId();
//            accessTenant = taskServiceImpl.accessTenantService.checkExist(tentanId);
            Integer id = accessGroup.getId();
            String eName = taskServiceImpl.accessGroupService.getRootGroup(id).getEName();

            if (StringUtils.isNotEmpty(eName) && eName.equals(accessTenant.getName())) {
                groupId = id;
                if(accessGroup.getName().contains("default")){
                    break;
                }
            }
        }

        this.tenantId = InfTraceContextHolder.get().getTenantId();
        this.tenantName = InfTraceContextHolder.get().getTenantName();
        this.groupId = groupId;
    }

    void setInputDatasets(Task task) {
        this.inputDatasets = JSON.parseArray(task.getInputDataset(), Dataset.class);
        this.inputDatasets.forEach(dataset -> {

            // 没有传metadata时使用前端传来的ID, 否则全部自己拼接ID
            Dataset.Metadata metadata = dataset.getMetadata();
            if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                dataset.setId(dataset.getId());
            } else {
                dataset.setId(metadata.toString());
            }

            String location = dataset.getLocation();
            if (dataset.getDetailedGra() != null && !dataset.getDetailedGra().isEmpty() && dataset.getDetailedDependency().size() == 0) {
                dataset.getDetailedDependency().add("ALL");
            }
            // 当DetailedGra不存在，DetailedDependency存在时，将DetailedDependency置为空
            if (dataset.getDetailedDependency() != null && (dataset.getDetailedGra() == null || dataset.getDetailedGra().isEmpty())) {
                dataset.setDetailedDependency(new ArrayList<>());
            }

            // 旧逻辑，待之后删除
            if (dataset.getCheckPath() == null || "".equals(dataset.getCheckPath()) || "null".equals(dataset.getCheckPath())) {
                if (location == null || "null".equals(location)) {
                    location = "";
                }
                List<String> partitions = dataset.getPartitions();
                String fileName = dataset.getFileName();
                if (fileName == null || "null".equals(fileName)) {
                    fileName = "";
                }
                int offset = dataset.getOffset();
                String partitionPath = "";
                if (partitions != null) {
                    partitionPath = partitions.stream().map(partition -> {
                        String[] partitionKV = partition.split("=");
                        String value;
                        if (partitionKV.length == 2) {
                            value = partitionKV[1];
                            if (!(value.startsWith("{{") && value.endsWith("}}"))) {
                                HashMap<String, String> graMap = new HashMap<>();
                                graMap.put("daily", "days");
                                graMap.put("hourly", "hours");
                                graMap.put("weekly", "weeks");
                                String granularity = graMap.get(dataset.getGranularity());
                                if (granularity == null) {
                                    throw new ServiceException("-1", String.format("不支持的时间粒度：%s", dataset.getGranularity()));
                                }
                                if (!(value.startsWith("'") && value.endsWith("'")) && !(value.startsWith("\"") && value.endsWith("\""))) {
                                    value = "'" + value + "'";
                                }
                                value = String.format("{{(execution_date + macros.timedelta(%s=%d)).strftime(%s)}}", granularity, offset, value);
                            } else {
                                value = StringUtils.stripStart(value, "{{");
                                value = StringUtils.stripEnd(value, "}}");
                            }
                            return partitionKV[0] + "=" + value;
                        }
                        return partition;
                    }).collect(Collectors.joining("/"));
                }

                if (!location.isEmpty() && !location.endsWith("/")) {
                    location = location + "/";
                }
                if (!partitionPath.isEmpty() && !partitionPath.endsWith("/")) {
                    partitionPath = partitionPath + "/";
                }
                dataset.setCheckPath(location + partitionPath + fileName);
            }

        });
    }

    void setOutputDatasets(Task task) {
        // todo: 暂时保留，后期可删除，该功能已经在service.impl.TaskServiceImpl.produceDependDataSets()中定义
        JSONArray outputDateSet = JSON.parseArray(task.getOutputDataset());
//        System.out.println("=="+ JSON.parseObject(outputDateSet.get(0).toString()).getString("metadata"));
        if (outputDateSet == null || outputDateSet.size() == 0 ||
                (JSON.parseObject(outputDateSet.get(0).toString()).getString("metadata") == null &&
                        JSON.parseObject(outputDateSet.get(0).toString()).getString("id").equals(""))) {
            HashMap<String, Object> temp = new HashMap<>();
            ArrayList<Map<String, Object>> output = new ArrayList<Map<String, Object>>();
            temp.put("id", templateCode + "_" + task.getName());
            temp.put("placeholder", true);
            temp.put("offset", -1);
            output.add(temp);
            task.setOutputDataset(JSONObject.toJSONString(output));
        }
        this.outputDatasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
        this.outputDatasets.forEach(dataset -> {
            // 没有传metadata时使用前端传来的ID, 否则全部自己拼接ID
            Dataset.Metadata metadata = dataset.getMetadata();
            if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                dataset.setId(dataset.getId());
            } else {
                dataset.setId(metadata.toString());
            }
        });
    }

    void setRuntimeConfigNew(Task task){
        RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        this.owner = runtimeConfig.getAdvancedParameters().getOwner();
        this.notifiedOwner = this.owner;
        this.startDate =runtimeConfig.getAdvancedParameters().getStartDate();
        this.endDate =runtimeConfig.getAdvancedParameters().getEndDate();
        this.maxActiveRuns = runtimeConfig.getAdvancedParameters().getMaxActiveRuns();
        this.retryInterval = runtimeConfig.getAdvancedParameters().getRetryInterval();
        this.executionTimeout = runtimeConfig.getAdvancedParameters().getExecutionTimeout();
        this.retries = runtimeConfig.getAdvancedParameters().getRetries();
        this.emails = runtimeConfig.getAdvancedParameters().getEmails();
        this.region = runtimeConfig.getSourceRegion();
        this.acrossCloud = runtimeConfig.getAdvancedParameters().getAcrossCloud();
        this.checkExpirationTime = runtimeConfig.getAdvancedParameters().getCheckExpirationTime();
        String regularAlertString = runtimeConfig.getRegularAlert();
        RegularAlert regularAlert = JSON.parseObject(regularAlertString, RegularAlert.class);
        if (regularAlert != null) {
            extraParam.put("regularAlert", JSON.toJSONString(regularAlert));
        }
        String alertModel = runtimeConfig.getAlertModel();
        Map<String, JSONObject> alertMap = JSON.parseObject(alertModel, Map.class);
        if (alertMap!=null){
            Iterator<Map.Entry<String, JSONObject>> iterator = alertMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, JSONObject> alertNode = iterator.next();
                String state = alertNode.getKey();
                String value = alertNode.getValue().toJSONString();
                AlertModel detail = JSON.parseObject(value, AlertModel.class);
                boolean isNotify = detail.getAlertType().size() > 0;
                switch (state) {
                    case "success":
                        if (isNotify){
                            this.emailOnSuccess = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                        break;
                    case "failure":
                        if(isNotify){
                            this.emailOnFailure = true;
                        }
                        extraParam.put("failed",JSON.toJSONString(detail));
                        break;
                    case "retry":
                        if(isNotify){
                            this.emailOnRetry = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                        break;
                    case "start":
                        if(isNotify){
                            this.emailOnStart = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                }
            }
        }

        clusterSLA = runtimeConfig.getAdvancedParameters().getClusterSla();
        String resourceLevel =runtimeConfig.getAdvancedParameters().getResourceLevel();
        String batchParams = runtimeConfig.getAdvancedParameters().getBatchParams();
        if (!"PythonShell".equals(templateCode) && !"Hive2Redshift".equals(templateCode) && !"tfJob".equals(templateCode) && !"TrinoJob".equals(templateCode)
                && !runtimeConfig.getExecuteMode().equals("local")) {
            this.lifecycle = runtimeConfig.getAdvancedParameters().getLifecycle();
            setSparkConfig(resourceLevel, null, batchParams);
        }
        log.info("Task:" + task.getId() + " setRuntimeConfig:group = " + group + " ,clusterSLA = " + clusterSLA);
    }

    void setRuntimeConfig(Task task) {
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        this.owner = runtimeConfigObject.getString("owner");
        this.notifiedOwner = this.owner;
        this.commandTags = runtimeConfigObject.getString("commandTags");
        this.clusterTags = runtimeConfigObject.getString("clusterTags");
        this.dependencies = runtimeConfigObject.getString("dependencies");
        this.startDate = runtimeConfigObject.getString("startDate");
        this.endDate = runtimeConfigObject.getString("endDate");
        this.maxActiveRuns = runtimeConfigObject.getInteger("maxActiveRuns");
        this.retryInterval = (Integer) runtimeConfigObject.getOrDefault("retryInterval", 0);
        this.executionTimeout = (Integer) runtimeConfigObject.getOrDefault("executionTimeout", 0);
        this.acrossCloud = (String) runtimeConfigObject.getOrDefault("acrossCloud","common");
        this.sqlEngine = (String)runtimeConfigObject.getOrDefault("engine","spark");
        this.checkExpirationTime = (Integer) runtimeConfigObject.getOrDefault("checkExpirationTime",0);
        region = runtimeConfigObject.getString("sourceRegion");
        // 默认使用钉钉报警的方式
//        String alertMethod = (String) runtimeConfigObject.getOrDefault("alertMethod","[\""+ALERT_DINGTALK+"\"]");
        String regularAlertString = runtimeConfigObject.getString("regularAlert");
        RegularAlert regularAlert = JSON.parseObject(regularAlertString, RegularAlert.class);
        if (regularAlert != null){
            extraParam.put("regularAlert",JSON.toJSONString(regularAlert));
        }
        String alertModel = runtimeConfigObject.getString("alertModel");
        Map<String, JSONObject> alertMap = JSON.parseObject(alertModel, Map.class);
        if (alertMap!=null){
            Iterator<Map.Entry<String, JSONObject>> iterator = alertMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, JSONObject> alertNode = iterator.next();
                String state = alertNode.getKey();
                String value = alertNode.getValue().toJSONString();
                AlertModel detail = JSON.parseObject(value, AlertModel.class);
                boolean isNotify = detail.getAlertType().size() > 0;
                switch (state) {
                    case "success":
                        if (isNotify){
                            this.emailOnSuccess = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                        break;
                    case "failure":
                        if(isNotify){
                            this.emailOnFailure = true;
                        }
                        extraParam.put("failed",JSON.toJSONString(detail));
                        break;
                    case "retry":
                        if(isNotify){
                            this.emailOnRetry = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                        break;
                    case "start":
                        if(isNotify){
                            this.emailOnStart = true;
                        }
                        extraParam.put(state,JSON.toJSONString(detail));
                }
            }
        }
        AccessUser build = AccessUser.builder().name(task.getCreateBy()).build();
        build.setDeleteStatus(DeleteEntity.NOT_DELETE);
        AccessUser accessUser = taskServiceImpl.accessUserService.selectOne(build);
        String emails = runtimeConfigObject.getString("emails");
        if (emails == "" || emails == null || "null".equals(emails)) {
            if (!"QueryEdit".equals(templateCode)){
                emails = accessUser.getEmail();
            }
        }
        this.emails = emails;
        this.retries = (Integer) runtimeConfigObject.getOrDefault("retries", 3);
        region = runtimeConfigObject.getString("sourceRegion");

//        group = getGroupByOwner(owner).toLowerCase();
        clusterSLA = (String) runtimeConfigObject.getOrDefault("clusterSla", "normal");

        String resourceLevel = runtimeConfigObject.getString("resourceLevel");
        JSONArray paramArray = runtimeConfigObject.getJSONArray("params");
        String batchParams = runtimeConfigObject.getString("batchParams");
        if (commandTags != null && !commandTags.isEmpty() && !commandTags.contains("spark")) {
            sparkConfig = "";
            return;
        }
        if (!"PythonShell".equals(templateCode) && !"Hive2Redshift".equals(templateCode) && !"tfJob".equals(templateCode) && !"TrinoJob".equals(templateCode)) {
            this.lifecycle = runtimeConfigObject.getString("lifecycle");
            setSparkConfig(resourceLevel, paramArray, batchParams);
        }
        log.info("Task:" + task.getId() + " setRuntimeConfig:group = " + group + " ,clusterSLA = " + clusterSLA);
    }

    private String getGroupByOwner(String owner) {
        ItUtil itUtil = new ItUtil();
        List<DeptInfo> dpInfoList = itUtil.getDeptInfo(owner);
        for (DeptInfo deptInfo : dpInfoList) {
            if (StringUtils.isNotEmpty(deptInfo.getOrganizationPath()) &&
                    deptInfo.getOrganizationPath().contains(DsTaskConstant.ADS_GROUP_KEYWORDS)) {
                return DsTaskConstant.ADS;
            }
        }
        return DsTaskConstant.DEFAULT_GROUP;
    }

    void setSparkConfig(String resourceLevel, JSONArray params, String batchParams) {
        String middleResource = DataCakeConfigUtil.getDataCakeConfig().getMiddleResource();
        String largeResource = DataCakeConfigUtil.getDataCakeConfig().getLargeResource();
        String extraLargeResource = DataCakeConfigUtil.getDataCakeConfig().getExtraLargeResource();
        String nodeSelectorLifecycle = DataCakeConfigUtil.getDataCakeConfig().getNodeSelectorLifecycle();
        StringBuilder config = new StringBuilder("--deploy-mode cluster ");
        String sparkResourceConfig;
        if ("middle".equals(resourceLevel)) {
            sparkResourceConfig = middleResource;
        }else if ("large".equals(resourceLevel)) {
            sparkResourceConfig = largeResource;
        } else if ("extra_large".equals(resourceLevel)) {
            sparkResourceConfig = extraLargeResource;
        } else {
            sparkResourceConfig = "";
        }
        if (StringUtils.isNotEmpty(lifecycle) && lifecycle.equals("OnDemand")) {
            config.append(nodeSelectorLifecycle).append(" ");
        }
        config.append(sparkResourceConfig).append(" ");
        //旧参数逻辑,暂不删除,兼容旧任务
        if ((batchParams == null || batchParams.isEmpty()) && (params != null && !params.isEmpty())) {
            params.forEach(param -> {
                JSONObject paramObj = (JSONObject) param;
                config.append("--conf ").append(paramObj.getString("key")).append("=").append(paramObj.getString("value")).append(" ");
            });
        }
        //校验参数的合法性,不合法抛出错误,合法则追加到config中
        if (batchParams != null && !batchParams.isEmpty()) {
            StringBuilder illegelOpt = new StringBuilder("");
            String formatBatchParams = ParseParamUtil.formatParam(batchParams);
            Set<String> opt = ParseParamUtil.getOpt(formatBatchParams);
            opt.forEach(p -> {
                if (!SparkSubmitOptionEnum.isValid(p)) {
                    illegelOpt.append(p).append(";");
                }
            });
            if (illegelOpt.length() > 0) {
                throw new ServiceException(BaseResponseCodeEnum.TASK_ILLEGEL_PARAMS, "非法参数:" + illegelOpt);
            }
            config.append(" ").append(formatBatchParams).append(" ");
        }
        config.append(String.format("--conf spark.app.name=%s ", task.getName()));
        config.append(String.format("--conf spark.app.taskId=%s ", task.getId()));
        config.append(String.format("--conf spark.app.workflowId=%s ", task.getWorkflowId()));
        config.append(String.format("--conf spark.kubernetes.driver.label.owner=%s --conf spark.kubernetes.executor.label.owner=%s ", owner, owner));
        config.append("--conf spark.kubernetes.driver.label.entry=DataStudio ");
        config.append("--conf spark.kubernetes.executor.label.entry=DataStudio ");

        //添加owner再组标签
        config.append(String.format("--conf spark.kubernetes.driver.label.tenantId=%s --conf spark.kubernetes.executor.label.tenantId=%s ", tenantId, tenantId));
        config.append(String.format("--conf spark.kubernetes.driver.label.groupId=%s --conf spark.kubernetes.executor.label.groupId=%s ", groupId, groupId));

        //利用region tentanId获取云资源名称
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        String ownerStr = owner;
//        if (isPrivate) {
//            config.append(String.format("--conf spark.kubernetes.driverEnv.HADOOP_USER_NAME=%s --conf spark.hadoop.client.projectId=%s ", tenantName + "#" + owner, tenantName));
////            ownerStr= getCloudResource(tenantId) + ":" + ownerStr;
//            config.append("--conf spark.hadoop.hive.metastore.uris=thrift://a73a2c9c7c82a4a3c9c16c4687f573ff-1323847595.us-east-1.elb.amazonaws.com:9084 ");
//        }else{
//            TableauDataSource tableauDataSource = new TableauDataSource();
//            if (tableauDataSource.isAdsOwener(owner) && !"sg2".equals(region)) {
//                config.append("--conf spark.kubernetes.namespace=ads-prod ");
//            }
//        }
        config.append(String.format("--conf spark.kubernetes.driverEnv.HADOOP_USER_NAME=%s --conf spark.hadoop.client.projectId=%s ", tenantName + "#" + owner, tenantName));
        config.append(String.format("--conf spark.hadoop.lakecat.client.projectId=%s ", tenantName));
        config.append(String.format("--conf spark.hadoop.lakecat.client.userName=%s ", InfTraceContextHolder.get().getUuid()));

        if(isPrivate){
            config.append(String.format("--conf spark.hadoop.lakecat.lineage.conf.cluster=%s ", "externalCluster"));
        }
        config.append(String.format("--conf spark.hadoop.lakecat.lineage.conf.jobName=%s ", task.getName()));
        config.append(String.format("--conf spark.hadoop.lakecat.lineage.conf.jobId=%s ", task.getId()));
        config.append(String.format("--conf spark.hadoop.lakecat.lineage.conf.executeUser=%s ", owner));
        config.append(String.format("--conf spark.hadoop.lakecat.lineage.conf.executeUserGrroup=%s ", InfTraceContextHolder.get().getUuid()));


        if (!isPrivate) {
            TableauDataSource tableauDataSource = new TableauDataSource();
            if (tableauDataSource.isAdsOwener(owner) && !"sg2".equals(region) && !"ue1".equals(region)) {
                config.append("--conf spark.kubernetes.namespace=ads-prod ");
            }
        }
        setTemplateRegionImp(config);
        sparkConfig = config.toString().replaceAll("--partitions_num=[0-9]+", "");
        System.out.println(sparkConfig);
        log.info("spark config: " + sparkConfig);
    }


    void setDefaultDb(Task task) {
        RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        String dsGroups = runtimeConfig.getAdvancedParameters().getDsGroups();
        if (StringUtils.isNotEmpty(dsGroups) && !"[]".equalsIgnoreCase(dsGroups)) {
            List<Integer> groupIds = JSON.parseArray(dsGroups, Integer.class);
            Integer integer = groupIds.stream().findFirst().get();
            UserGroup byId = taskServiceImpl.userGroupService.getById(integer);
            this.defaultDb = byId.getDefaultHiveDbName();
        }
    }



    StringBuilder setTemplateRegionImp(StringBuilder config) {
        TemplateRegionImp templateRegionImp =
                taskServiceImpl.templateRegionImpService.selectOne(new TemplateRegionImp().setTemplateCode(templateCode).setRegionCode(region));
        if (templateRegionImp != null) {
            this.mainClass = templateRegionImp.getMainClass();
            this.jarUrl = templateRegionImp.getUrl();
        }
        if (mainClass != null && !mainClass.isEmpty() && !"null".equals(mainClass)) {
            config.append(String.format("--class %s ", mainClass));
        }
        if (jarUrl != null && !jarUrl.isEmpty() && !"null".equals(jarUrl)) {
            config.append(jarUrl).append(" ");
        }
        return config;
    }

    void setEventDepends() {
        this.eventDepends = JSON.parseArray(task.getEventDepends(), EventDepend.class);
        return;
    }

    void setTriggerParam() {
        this.triggerParam = JSON.parseObject(task.getTriggerParam(), TriggerParam.class);
    }

//    @Override
//    public void cancel() {
//
//    }

    void setDependTypes() {
        if (task.getDependTypes() != null && !task.getDependTypes().equals("")) {
            this.dependTypes = task.getDependTypes().trim().replaceAll("\\[|\\]|\"", "");
        } else {
            this.dependTypes = "";
        }

    }

    @Override
    public void beforeCheck() throws Exception {

    }

    @Override
    public void afterCheck() throws Exception {

    }

    protected abstract List<Map<String, Object>> buildTaskItems();

//
//    @Override
//    public void exec() throws Exception {
//        beforeExec();
//        ScheduledJobParam scheduledJobParam =
//                new ScheduledJobParam(name, emails, owner, retries, maxActiveRuns, emailOnSuccess, emailOnFailure, emailOnRetry, inputDatasets, outputDatasets, startDate, endDate, executionTimeout, extraParam, buildTaskItems());
//        PropertyPreFilters.MySimplePropertyPreFilter filterField = scheduledJobParam.getFilterField();
//        String jobParamJson = JSON.toJSONString(scheduledJobParam,filterField);
//        log.info("scheduled job param: " + jobParamJson);
//        Map<String, String> params = new HashMap<>();
//        params.put("name", name);
//        params.put("task_code", jobParamJson);
//        BaseResponse response = HttpUtil.doPost(UrlUtil.getSchedulerUrl() + "/pipeline/task/update", params);
//        if (response.getCode() != 0) {
//            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESQUEST_FAIL, String.format("Scheduler service request failed: %s", response.getData()));
//        }
//        JSONObject jsonObject = JSON.parseObject((String) response.getData());
//        Integer code = jsonObject.getInteger("code");
//        if (code != 0) {
//            String msg = jsonObject.getString("msg");
//            log.error(String.format("code: %d, message: %s", code, msg));
//            throw new ServiceException(BaseResponseCodeEnum.SCHEDULER_RESPONSE_ERR, String.format("code: %d, message: %s", code, msg));
//        }
////        afterExec();
//    }

    protected abstract String buildCommand();

    public String encodeFlag(String str) {
        return "/*zheshiyigebianmabiaoshi*/" + str + "/*zheshiyigebianmabiaoshi*/";
    }

    public String encodeFlagDatacake(String str){
        return "/*datacakebianma*/" + str + "/*datacakebianma*/";
    }

    public String getCommand() {
        return buildCommand();
    }
    /**
     * 转换为与调度约定的 PB 数据结构
     */
    public ScheduleJobOuterClass.TaskCode toPbTaskCode() {

        if(this.triggerParam.getType().equals("data")){
            this.triggerParam.setType("cron");
            switch (this.triggerParam.getOutputGranularity()){
                case "daily":
                    this.triggerParam.setCrontab("0 0 * * *");
                    break;
                case "hourly":
                    this.triggerParam.setCrontab("0 * * * *");
                    break;
                case "minutely":
                    this.triggerParam.setCrontab("*/5 * * * *");
                    break;
                case "weekly":
                    this.triggerParam.setCrontab("0 0 * * 1");
                    break;
                case "monthly":
                    this.triggerParam.setCrontab("0 0 1 * *");
                    break;
            }
        }


        for (Dataset dataset: this.getInputDatasets()){
            if(dataset.getGranularity().equals("") || dataset.getGranularity().isEmpty()){
                dataset.setGranularity(this.triggerParam.getOutputGranularity());
            }
        }

        List<ScheduleJobOuterClass.Dataset> pbInputDatasetList = CollectionUtils.isEmpty(this.getInputDatasets()) ?
                Collections.emptyList() : this.getInputDatasets().stream().map(Dataset::toPbDataset)
                .collect(Collectors.toList());

        List<ScheduleJobOuterClass.Dataset> pbOutputDatasetList = CollectionUtils.isEmpty(this.getOutputDatasets()) ?
                Collections.emptyList() : this.getOutputDatasets().stream().map(Dataset::toPbDataset)
                .collect(Collectors.toList());

        List<ScheduleJobOuterClass.EventDepend> pbEventDepends = CollectionUtils.isEmpty(this.getEventDepends()) ?
                Collections.emptyList() : this.getEventDepends().stream().map(EventDepend::toPbEventDepend)
                .collect(Collectors.toList());

        // 去除已经删除的 eventDepends
        pbEventDepends = pbEventDepends.stream()
                .filter(item -> {
                    if (StringUtils.isBlank(item.getTaskId())) {
                        return true;
                    }

                    Task dependTask = taskServiceImpl.taskMapper.selectByPrimaryKey(item.getTaskId());
                    return dependTask != null && dependTask.getDeleteStatus() == DeleteEntity.NOT_DELETE.intValue();
                }).collect(Collectors.toList());

        List<Map<String, Object>> taskItems = buildTaskItems();
        Boolean isPrivate = InfTraceContextHolder.get().getIsPrivate();
        if(!isPrivate){
            TableauDataSource tableauDataSource = new TableauDataSource();
            if (tableauDataSource.isAdsOwener(owner) && !"sg2".equals(region)) {
                taskItems.forEach(data->{
                    data.put("cluster_tags",data.get("cluster_tags").toString()+",rbac.cluster:ads-prod");
                });
            }else{
                taskItems.forEach(data->{
                    data.put("cluster_tags",data.get("cluster_tags").toString()+",rbac.cluster:bdp-prod");
                });
            }
        }

        Map<String, String> userGroupInfo = new HashMap<>();
        try {
            // 组装用户组信息
            userGroupInfo.put("groupId", InfTraceContextHolder.get().getGroupId());
            userGroupInfo.put("currentGroup", InfTraceContextHolder.get().getCurrentGroup());
            userGroupInfo.put("uuid", InfTraceContextHolder.get().getUuid());
            userGroupInfo.put("tenantId", InfTraceContextHolder.get().getTenantId().toString());
            userGroupInfo.put("tenantName", InfTraceContextHolder.get().getTenantName());
        } catch (Exception  e){
            // grpc接口也会走到这里，但没有这个拦截器，这里catch住不影响
            log.error(e.getMessage(), e);
        }




        ScheduleJobOuterClass.TaskCode.Builder builder = ScheduleJobOuterClass.TaskCode.newBuilder()
                .setName(this.getName())
                .setEmails(this.getEmails())
                .setOwner(this.getOwner())
                .setRetries(this.getRetries())
                .setMaxActiveRuns(this.getMaxActiveRuns())
                .setEmailOnSuccess(this.isEmailOnSuccess())
                .setEmailOnRetry(this.isEmailOnRetry())
                .setEmailOnFailure(this.isEmailOnFailure())
                .setEmailOnStart(this.isEmailOnStart())
                .addAllInputDatasets(pbInputDatasetList)
                .addAllOutputDatasets(pbOutputDatasetList)
                .setStartDate(this.getStartDate())
                .setEndDate(this.getEndDate())
                .setExecutionTimeout(this.getExecutionTimeout())
                .setExtraParam(JSONObject.toJSONString(this.getExtraParam()))
                .addAllEventDepend(pbEventDepends)
                .setDependTypes(this.getDependTypes())
                .setVersion(this.getVersion())
                .setNotifiedOwner(this.getNotifiedOwner())
                .setTaskItems(JSONArray.toJSONString(ListUtils.emptyIfNull(taskItems)))
                .setUserGroupInfo(JSON.toJSONString(userGroupInfo))
                .setCheckExpirationTime(this.getCheckExpirationTime())
                .setDynamicsCmd(this.getDynamicsCmd())
                .setTemplateCode(this.getTemplateCode())
                .setRetryInterval(this.getRetryInterval());

        if (this.getTriggerParam() != null) {
            builder.setTriggerParam(this.getTriggerParam().toPbTriggerParam());
        }
        return builder.build();
    }

    String getEngineCommand(Boolean isSsql) {
        com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
        CloudResouce.DataResource dataResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if (StringUtils.isNotEmpty(runtimeConfig.getAdvancedParameters().getEngineConfig())) {
            String[] str = runtimeConfig.getAdvancedParameters().getEngineConfig().split("_");
            return String.format("version:%s,region:%s,cloud:%s,sparksql:%s", str[1], dataResource.getRegion(),
                    dataResource.getProvider(), isSsql);
        }
        if (isSsql) {
            return "type:spark-submit-sql-ds";
        }
        return "type:spark-submit-ds";
    }

}
