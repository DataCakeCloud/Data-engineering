package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.bean.SysDict;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.meta.DeTask;
import com.ushareit.dstask.bean.meta.TaskUsage;
import com.ushareit.dstask.bean.qe.PersonQuery;
import com.ushareit.dstask.bean.qe.QueryTop;
import com.ushareit.dstask.bean.qe.TaskAndScan;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.MateMapper;
import com.ushareit.dstask.mapper.QueryMapper;
import com.ushareit.dstask.service.FeedbackService;
import com.ushareit.dstask.service.HomeService;
import com.ushareit.dstask.service.SysDictService;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.DateUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.ushareit.dstask.constant.HomeConstant.*;

/**
 * @author wuyan
 * @date 2022/9/2
 */
@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    public TaskService taskService;
    @Resource
    public FeedbackService feedbackService;
    @Resource
    public SysDictService sysDictService;
    @Resource
    private QueryMapper queryMapper;
    @Resource
    private MateMapper mateMapper;
    @Resource
    private HomeService queryHistoryService;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron="0 0/2 9-21 * * ?")
    public void ScheduledMonitorCache() {
        queryHistoryService.monitorCache();
    }

    @Override
    @MultiTenant
    @DisLock(key = "monitorCache", expiredSeconds = 4, isRelease = false)
    public void monitorCache() {

        // 平台现状-访问热度：当前人近30天的查询次数
        // 平台现状-数据完善度：当前人近30天的查询次数
        // 由于个人和平台在一个接口中，没有缓存的必要

        // TODO 如果接口慢，可以放到缓存全局高频访问表TOP10
//        JSONArray data = getDataForJsonArray("metadata/owner/frequency", 2);
//        CACHE.put("globalTop", data);


        // TODO 解决服务启动接口慢的问题，每天凌晨0点1分定时写入sys_dic表
        Double overallDsScore = getOverallQueryDsScore();
        SysDict dsQuery = sysDictService.getByCode(DS_QUERY);
        if (dsQuery == null) {
            log.info("定时新增执行，当前时间:" + System.currentTimeMillis() + "DS_QUERY:" + overallDsScore);
            dsQuery = SysDict.builder().value(overallDsScore.toString()).code(DS_QUERY).parentCode("HOME").status(1).build();
            dsQuery.setCreateBy("system").setUpdateBy("system")
                    .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
            sysDictService.save(dsQuery);
            return;
        }

        log.info("定时更新执行，当前时间:" + System.currentTimeMillis() + "DS_QUERY:" + overallDsScore);
        dsQuery.setValue(overallDsScore.toString())
                .setStatus(1)
                .setCreateTime(new Timestamp(System.currentTimeMillis()))
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        sysDictService.update(dsQuery);

    }

    @Override
    public Map<String, Object> queryTaskStatics(Integer recent,String userGroup) {
        Map<String, Object> queryTaskStatics = getQueryTaskStatics(recent,userGroup);
        return queryTaskStatics;
    }

    @Override
    public List<QueryTop> queryScanTop(String userGroup) {
        String currentDate = DateUtil.getNowDateStr();
        List<String> days = getRecent(0, currentDate);
        String createBy = InfTraceContextHolder.get().getUserName();
        String currentGroup = InfTraceContextHolder.get().getUuid();
        userGroup = StringUtils.isBlank(userGroup) ? currentGroup : userGroup;
        List<QueryTop> queryTops = queryMapper.selectQueryTop(currentDate, days.get(0), userGroup, "processed_bytes");

        queryTops.forEach(queryTop -> {
                    queryTop.setDisplayProcessedBytes(CommonUtil.convertSize(queryTop.getProcessedBytes()));
                    String createTime = queryTop.getCreateTime();
                    if (createTime.contains(".")) {
                        createTime = createTime.substring(0, createTime.indexOf("."));
                    }
                    queryTop.setCreateTime(createTime);
                }
        );
        return queryTops;
    }

    @Override
    public List<QueryTop> queryExecutionTop() {
        String currentDate = DateUtil.getNowDateStr();
        List<String> days = getRecent(0, currentDate);
        String createBy = InfTraceContextHolder.get().getUserName();

        List<QueryTop> queryTops = queryMapper.selectQueryTop(currentDate, days.get(0), createBy, "execute_duration");

        queryTops.forEach(queryTop -> {
            queryTop.setExecuteDuration(scale(queryTop.getExecuteDuration(), 100));
            String createTime = queryTop.getCreateTime();
            if (createTime.contains(".")) {
                createTime = createTime.substring(0, createTime.indexOf("."));
            }
            queryTop.setCreateTime(createTime);
        });
        return queryTops;
    }

    @Override
    public Map<String, Object> overallScore() {
        Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
        if (dcRole) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        ImmutableMap<String, ? extends Serializable> visit = ImmutableMap.of(NAME, "访问热度", MAX, 1);
        ImmutableMap<String, ? extends Serializable> query = ImmutableMap.of(NAME, "查询频率", MAX, 1);
        ImmutableMap<String, ? extends Serializable> feedback = ImmutableMap.of(NAME, "问题反馈", MAX, 1);
        ImmutableMap<String, ? extends Serializable> task = ImmutableMap.of(NAME, "任务质量", MAX, 1);
        ImmutableMap<String, ? extends Serializable> dataMap = ImmutableMap.of(NAME, "数据完善度", MAX, 1);
        List<ImmutableMap<String, ? extends Serializable>> indicator = Arrays.asList(visit, query, feedback, task, dataMap);
        result.put("indicator", indicator);

        String createBy = InfTraceContextHolder.get().getUserName();
        // 能力现状-查询频率：当前人近30天的查询次数
        Double personQuery = getOverallQueryPersonScore();

        // 能力现状-任务质量：当前人近30天的查询次数
        String current = DateUtil.getNowDateStr();
        List<String> days = getRecent(1, current);
        Double metaPersonScore = mateMapper.getMetaPersonScore(current, days.get(0), createBy);

        // 能力现状-问题反馈：当前人近30天的查询次数
        List<TaskUsage> personFeedbacks = feedbackService.selectDayFeedbacks(createBy, days.get(0), current);
        Double personFeedbackAvg = get30Avg(personFeedbacks);

        // TODO 等元数据好了放开
        // 能力现状-访问热度：当前人近30天的查询次数
        // 能力现状-数据完善度：当前人近30天的查询次数
        // 平台现状-访问热度：当前人近30天的查询次数
        // 平台现状-数据完善度：当前人近30天的查询次数
        JSONObject data = getDataForJsonObject(DS_HOME_PAGE_DATA_URL);
        JSONObject tableUsageProfile = data.getJSONObject("tableUsageProfile");
        // 能力现状-访问热度：当前人近30天的查询次数
        String ownerVisitActivity = tableUsageProfile.getString("ownerVisitActivity");
        // 平台现状-访问热度：当前人近30天的查询次数
        String platformVisitActivity = tableUsageProfile.getString("platformVisitActivity");

        JSONObject metadataIntegrity = data.getJSONObject("metadataIntegrity");
        // 能力现状-数据完善度：当前人近30天的查询次数
        String ownerIntegrity = metadataIntegrity.getString("ownerIntegrity");
        // 平台现状-数据完善度：当前人近30天的查询次数
        String platformIntegrity = metadataIntegrity.getString("platformIntegrity");


        // 平台现状-查询频率：所有人近30天的查询次数 必须存缓存
        Double dsQuery = Double.valueOf(sysDictService.getValueByCode(DS_QUERY));

        // 平台现状-任务质量：所有人近30天的查询次数
        List<TaskUsage> personQueries = mateMapper.getMetaAvgScore(current, days.get(0));
        Double metaDsScore = get30Avg(personQueries);

        // 平台现状-问题反馈：近30天的查询次数
        List<TaskUsage> dsFeedbacks = feedbackService.selectDayFeedbacks(null, days.get(0), current);
        Double dsFeedbackAvg = get30Avg(dsFeedbacks);

        List<Double> person = Arrays.asList(scale(new Double(ownerVisitActivity), 1000), personQuery, personFeedbackAvg, metaPersonScore, scale(new Double(ownerIntegrity), 1000));
        ImmutableMap<String, Object> personMap = ImmutableMap.of(NAME, "能力现状", VALUE, person);

        List<Double> ds = Arrays.asList(scale(new Double(platformVisitActivity), 1000), dsQuery, dsFeedbackAvg, metaDsScore, scale(new Double(platformIntegrity), 1000));
        ImmutableMap<String, Object> dsMap = ImmutableMap.of(NAME, "平台现状", VALUE, ds);

        List<ImmutableMap<String, Object>> series = Arrays.asList(personMap, dsMap);
        result.put("series", series);

        return result;
    }

    @Override
    public Map<String, Object> metaTaskStatics(Integer recent,String userGroup) {
        Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
//        if(dcRole){
//            return new HashMap<>();
//        }
        String currentDate = DateUtil.getNowDateStr();
        List<String> days = getRecent(recent, currentDate);
        String createBy = InfTraceContextHolder.get().getUserName();
        // 当前周期的任务平均分
//        Double taskAvgScore = mateMapper.getTaskAvgScore(currentDate, days.get(0), createBy);
//        taskAvgScore = taskAvgScore == null ? 0.0 : taskAvgScore;
//        taskAvgScore = scale(taskAvgScore, 1);

        // 上一个周期的任务平均分
//        Double lastTaskAvgScore = mateMapper.getTaskAvgScore(currentDate, days.get(1), createBy);
//        lastTaskAvgScore = lastTaskAvgScore == null ? 0.0 : lastTaskAvgScore;

        // 平均分环比
        String taskAvgScoreRatio = "0%";
//        if (lastTaskAvgScore != 0.0) {
//            taskAvgScoreRatio = division(taskAvgScore - lastTaskAvgScore, lastTaskAvgScore);
//        }

        // 折线图-任务用量
//        List<TaskUsage> metaTaskUsages = mateMapper.getMetaTaskUsages(currentDate, days.get(0), createBy);
        // 保留两位小数
//        metaTaskUsages.forEach(taskUsage -> taskUsage.setNum(scale(taskUsage.getNum(), 100)));
//        List<TaskUsage> sortedTaskUsages = metaTaskUsages.stream().sorted(Comparator.comparing(TaskUsage::getDt)).collect(Collectors.toList());
        List<TaskUsage> sortedTaskUsages = new ArrayList<>();
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put(NAME, "任务用量");
        metaMap.put(DATA, sortedTaskUsages);

        // 折线图-任务总数
        List<Task> tasks = taskService.selectDayOnlinedTasks(days.get(0), currentDate, userGroup);
        List<DeTask> deTasks = tasks.stream().map(task -> new DeTask(task.getStatusCode(), DateUtil.getDateStr(task.getCreateTime().getTime()))).collect(Collectors.toList());
        Map<String, List<DeTask>> createTimeGroup = deTasks.stream().collect(Collectors.groupingBy(DeTask::getCreateTime));
        List<TaskUsage> deTasksUsage = createTimeGroup.entrySet().stream().map(e -> {
            String key = e.getKey();
            int size = e.getValue().size();
            return new TaskUsage(key, size + 0.0);
        }).collect(Collectors.toList());
        List<TaskUsage> sortedTasks = deTasksUsage.stream().sorted(Comparator.comparing(TaskUsage::getDt)).collect(Collectors.toList());
        Map<String, Object> deMap = new HashMap<>();
        deMap.put(NAME, "任务总数");
        deMap.put(DATA, sortedTasks);

        // 上线任务总数
        List<Task> onlinedTasks = taskService.sumAllOnlinedTasks(userGroup);
        int all = onlinedTasks.size();

        // 失败数
        long fail = onlinedTasks.stream().filter(task -> "FAILED".equalsIgnoreCase(task.getStatusCode())).count();

        // 成功率
        String successRatio = division((all + 0.0f) - (fail + 0.0), all);

        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        //        result.put("score", taskAvgScore);
        result.put(RATIO, taskAvgScoreRatio);
        result.put("totalOnline", all);
        result.put(SUCCESS, successRatio);
        result.put(FAIL, fail);
        result.put("list", Arrays.asList(metaMap, deMap));

        return result;
    }

    @Override
    public List<ImmutableMap<String, String>> keyIndex() {
        Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
        if(dcRole){
            return new ArrayList<>();
        }

        // 活跃表数量占比、僵尸数据存储量占比、任务平均CPU使用率、任务平均MEM使用率
        JSONObject data = getDataForJsonObject(DS_HOME_PAGE_DATA_URL);
        // 活跃表数量占比
        JSONObject activeTable = data.getJSONObject("activeTable");
        Double ownerTableRatio = activeTable.getDouble("ownerTableRatio");

        // 僵尸数据存储量占比
        JSONObject tableStorage = data.getJSONObject("tableStorage");
        Double ownerZombieDataSizeRatio = tableStorage.getDouble("ownerZombieDataSizeRatio");

        // cpu占比 mem
        JSONObject resourceUsage = data.getJSONObject("resourceUsage");
        Double avgCpuUsed = resourceUsage.getDouble("avgCpuUsed");
        Double avgMemoryUsed = resourceUsage.getDouble("avgMemoryUsed");

        ImmutableMap<String, String> ownerTableRatioMap = ImmutableMap.of(NAME, "活跃表数量占比", VALUE, getPercent(ownerTableRatio, 0));
        ImmutableMap<String, String> ownerZombieDataSizeRatioMap = ImmutableMap.of(NAME, "僵尸数据存储量占比", VALUE, getPercent(ownerZombieDataSizeRatio, 0));
        ImmutableMap<String, String> avgCpuUsedMap = ImmutableMap.of(NAME, "任务平均CPU使用率", VALUE, getPercent(avgCpuUsed, 0));
        ImmutableMap<String, String> avgMemoryUsedMap = ImmutableMap.of(NAME, "任务平均Mem使用率", VALUE, getPercent(avgMemoryUsed, 0));

        List<ImmutableMap<String, String>> result = Arrays.asList(ownerTableRatioMap, ownerZombieDataSizeRatioMap, avgCpuUsedMap, avgMemoryUsedMap);

        return result;
    }

    @Override
    public List<ImmutableMap<String, Object>> dataResource() {
//        Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
//        if(dcRole){
//            return new ArrayList<>();
//        }
        JSONObject data = getDataForJsonObject(OWNER_TABLE_URL);

        // Owner表-num：tableNum、Owner表-size：storageSize、Owner表-user：privilegeUser、有权限表数量-num：privilegeNum
        ImmutableMap<String, ? extends Serializable> numMap = generateDataResourceMap("tableNum", NUM, data);
        ImmutableMap<String, ? extends Serializable> sizeMap = generateDataResourceMap("storageSize", "size", data);
        ImmutableMap<String, ? extends Serializable> userMap = generateDataResourceMap("privilegeUser", "user", data);
        ImmutableMap<String, ? extends Serializable> ownerNumMap = generateDataResourceMap("privilegeNum", NUM, data);

        List<ImmutableMap<String, ? extends Serializable>> owner = Arrays.asList(numMap, sizeMap, userMap);
        List<ImmutableMap<String, ? extends Serializable>> privilege = Arrays.asList(ownerNumMap);

        ImmutableMap<String, Object> ownerMap = ImmutableMap.of(NAME, "Owner表", VALUE, owner);
        ImmutableMap<String, Object> privilegeMap = ImmutableMap.of(NAME, "有权限表数量", VALUE, privilege);

        List<ImmutableMap<String, Object>> result = Arrays.asList(ownerMap, privilegeMap);

        return result;
    }


    @Override
    public JSONArray metaTop(Integer type) {
       /* Boolean dcRole = DataCakeConfigUtil.getDataCakeConfig().getDcRole();
        if (dcRole) {
            return new JSONArray();
        }*/
        return getDataForJsonArray(OWNER_FREQUENCY_URL, type);
    }

    private JSONObject getBaseParam() {
        JSONObject o = new JSONObject();
        o.put(OWNER, InfTraceContextHolder.get().getUserName());
        return o;
    }

    private JSONArray getDataForJsonArray(String requestPath, Integer type) {
        JSONObject o = getBaseParam();
        o.put(TYPE, type);
        BaseResponse response = getResponse(requestPath, o);
        JSONArray objects;
        try {
             objects = JSON.parseArray(response.getData().toString());
        }catch (Exception exception){
            objects = new JSONArray();
        }
        return objects;
    }

    private JSONObject getDataForJsonObject(String requestPath) {
        JSONObject o = getBaseParam();
        return getDataForJsonObject(requestPath, o);
    }

    private JSONObject getDataForJsonObject(String requestPath, JSONObject param) {
        BaseResponse response = getResponse(requestPath, param);
        Object data = response.getData();
        if (data == null) {
            data = "";
        }
        return JSONObject.parseObject(data.toString());
    }

    private BaseResponse getResponse(String requestPath, JSONObject param) {
        String url = gatewayUrl + requestPath;
        log.info("full url path is {}", url);
        Map<String, String> headers = new HashMap(1);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
        Long start = System.currentTimeMillis();
        BaseResponse response = HttpUtil.postWithJson(url, param.toString(), headers);
        Long end = System.currentTimeMillis();
        if (end - start > 1000) {
            log.info("接口请求" + url + "时间:" + (end - start));
        }
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }

        return response;
    }

    private ImmutableMap<String, ? extends Serializable> generateDataResourceMap(String name, String key, JSONObject data) {
        Double num = data != null ? getFromResponseData(data, name) : 0;
        Double total = data != null ? getFromResponseData(data, name + ALL) : 0;
        String ratio = generateDataResourceProportion(num, total);

        String storageSizeStr = "";
        if ("storageSize".equalsIgnoreCase(name)) {
            storageSizeStr = CommonUtil.convertSize(num);
            ImmutableMap<String, ? extends Serializable> map = ImmutableMap.of(NAME, key, VALUE, storageSizeStr, PROPORTION, ratio);
            return map;
        }

        ImmutableMap<String, ? extends Serializable> map = ImmutableMap.of(NAME, key, VALUE, num, PROPORTION, ratio);
        return map;
    }

    private String generateDataResourceProportion(Double num, Double total) {
        String ratio = "0%";
        if (total != null || total != 0) {
            ratio = division(num, total);
        }
        return ratio;
    }

    private Double getFromResponseData(JSONObject data, String name) {
        Double num = 0.0;
        if (data.getDouble(name) != null) {
            num = data.getDouble(name);
        }
        return num;
    }

    private Double getOverallQueryPersonScore() {
        String currentDate = DateUtil.getNowDateStr();
        List<String> days = getRecent(1, currentDate);
        String createBy = InfTraceContextHolder.get().getUserName();
        List<PersonQuery> queryPerson = queryMapper.selectQueryPerson(currentDate, days.get(0), createBy);

        Double query = getOverallQueryScore(queryPerson);
        return query;
    }

    private Double getOverallQueryDsScore() {
        String currentDate = DateUtil.getNowDateStr();
        List<String> days = getRecent(1, currentDate);
        List<PersonQuery> queryPerson = queryMapper.selectQueryPerson(currentDate, days.get(0), null);

        Double query = getOverallQueryScore(queryPerson);
        return query;
    }

    private String getPercent(Double data, int digit) {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(digit);
        return numberFormat.format(data);
    }

    private Double getOverallQueryScore(List<PersonQuery> personQueries) {
        Double avgPerson = 0.0;
        if (personQueries.size() == 0) {
            return avgPerson;
        }

        PersonQuery max = personQueries.get(0);
        PersonQuery min = personQueries.get(personQueries.size() - 1);

        if (max.getQueryNum().equals(min.getQueryNum())) {
            return avgPerson;
        }

        double sum = (personQueries.stream().mapToDouble(PersonQuery::getQueryNum).sum() - personQueries.size() * min.getQueryNum()) / (max.getQueryNum() - min.getQueryNum() + 0.0);
        avgPerson = sum / 30;
        avgPerson = scale(avgPerson, 10);

        return avgPerson;
    }

    private Double get30Avg(List<TaskUsage> taskUsages) {
        Double avgPerson = 0.0;
        if (taskUsages.size() == 0) {
            return avgPerson;
        }

        TaskUsage max = taskUsages.get(0);
        TaskUsage min = taskUsages.get(taskUsages.size() - 1);

        if (max.getNum().equals(min.getNum())) {
            return avgPerson;
        }

        double sum = (taskUsages.stream().mapToDouble(TaskUsage::getNum).sum() - taskUsages.size() * min.getNum()) / (max.getNum() - min.getNum() + 0.0);
        avgPerson = sum / 30;
        avgPerson = scale(avgPerson, 10);

        return avgPerson;
    }


    private Map<String, Object> getQueryTaskStatics(Integer recent,String userGroup) {
        Map<String, Object> result = new HashMap<>(6);
        String current = DateUtil.getNowDateStr();
        List<String> days = getRecent(recent, current);

        Map<String, Object> executeDurationMap = new HashMap<>(2);
        Map<String, Object> scanMap = new HashMap<>(2);
        executeDurationMap.put(P90, 0.0);
        scanMap.put(P90, 0.0);

        String currentDate = DateUtil.getNowDateStr();
        String createBy = InfTraceContextHolder.get().getUserName();
        userGroup=StringUtils.isBlank(userGroup)?null:userGroup;
        List<TaskAndScan> taskAndScans = queryMapper.selectTaskAndScan(currentDate, days.get(1), userGroup);

        if (taskAndScans.size() == 0) {
            generateBlankTaskStatics(result, executeDurationMap, scanMap);
            return result;
        }

        List<TaskAndScan> lastTaskAndScans = taskAndScans.stream().filter(taskAndScan -> taskAndScan.getCreateTime().compareTo(days.get(0)) >= 0).collect(Collectors.toList());
        List<TaskAndScan> last2TaskAndScans = taskAndScans.stream().filter(taskAndScan -> taskAndScan.getCreateTime().compareTo(days.get(0)) < 0).collect(Collectors.toList());

        // 最近查询数
        Long lastTasks = lastTaskAndScans.stream().mapToLong(TaskAndScan::getQueryNum).sum();
        result.put(NUM, lastTasks);
        Long last2Tasks = last2TaskAndScans.stream().mapToLong(TaskAndScan::getQueryNum).sum();

        // 查询数环比
        String percent = division(lastTasks - last2Tasks, last2Tasks);
        result.put(RATIO, percent);

        // 平均任务时长
        Double totalExcuteDuration = lastTaskAndScans.stream().mapToDouble(TaskAndScan::getSumExecuteDuration).sum();
        Double avgExcuteDuration = divide(totalExcuteDuration, lastTasks + 0.0, 10);
        executeDurationMap.put(AVG, avgExcuteDuration);

        // 平均扫描量
        Double totalProcessBytes = lastTaskAndScans.stream().mapToDouble(TaskAndScan::getSumProcessedBytes).sum();
        Double divide = divide(totalProcessBytes / 1024 / 1024, lastTasks + 0.0, 10);
        scanMap.put(AVG, divide);

        // 成功率
        long totalSuccessNum = lastTaskAndScans.stream().mapToLong(TaskAndScan::getSuccessNum).sum();
        String successRate = division(totalSuccessNum, lastTasks);
        result.put(SUCCESS, successRate);

        // 所有的90分位
        generateNinePercent(lastTaskAndScans, result, executeDurationMap, scanMap);

        // 查询任务数-折线图
        List<TaskAndScan> sorted = lastTaskAndScans.stream().sorted(Comparator.comparing(TaskAndScan::getCreateTime)).collect(Collectors.toList());
        Map<String, Object> executionMap = generateLineChart(sorted, QUERY);

        // 扫描数据量-折线图
        Map<String, Object> finalScanMap = generateLineChart(sorted, SCAN);

        List<Map<String, Object>> list = Arrays.asList(executionMap, finalScanMap);
        result.put("list", list);


        return result;
    }

    private void generateNinePercent(List<TaskAndScan> lastTaskAndScans,
                                     Map<String, Object> result,
                                     Map<String, Object> executeDurationMap,
                                     Map<String, Object> scanMap) {
        if (lastTaskAndScans.size() > 0) {
            int processCeil = (int) Math.ceil(lastTaskAndScans.size() * 0.9) - 1;
            // 求扫描量90分位
            lastTaskAndScans.sort(Comparator.comparing(TaskAndScan::getSumProcessedBytes));
            Double sumProcessedBytesP90 = lastTaskAndScans.get(processCeil).getSumProcessedBytes();
            scanMap.put(P90, scale(sumProcessedBytesP90 / 1024 / 1024, 10));

            // 求执行时长90分位
            lastTaskAndScans.sort(Comparator.comparing(TaskAndScan::getSumExecuteDuration));
            Double sumExecuteDurationP90 = lastTaskAndScans.get(processCeil).getSumExecuteDuration();
            executeDurationMap.put(P90, scale(sumExecuteDurationP90, 10));
        }

        result.put("totalDuration", executeDurationMap);
        result.put(SCAN, scanMap);
    }

    private void generateBlankTaskStatics(Map<String, Object> result,
                                          Map<String, Object> executeDurationMap,
                                          Map<String, Object> scanMap) {
        result.put(NUM, 0);
        result.put(RATIO, 0);

        executeDurationMap.put(AVG, 0.0);
        result.put("totalDuration", executeDurationMap);

        result.put(SUCCESS, 0);

        scanMap.put(AVG, 0.0);
        result.put(SCAN, scanMap);
        result.put("list", new ArrayList<>());
    }

    private Map<String, Object> generateLineChart(List<TaskAndScan> taskAndScans, String type) {
        Map<String, Object> result = new HashMap<>(2);
        result.put(NAME, SCAN.equalsIgnoreCase(type) ? "扫描数据量" : "查询任务数");
        List<Map<String, Object>> scanListMap = taskAndScans.stream().map(taskAndScan -> {
            Map<String, Object> map = new HashMap<>(2);
            map.put(DT, taskAndScan.getCreateTime());

            if (SCAN.equalsIgnoreCase(type)) {
                String numStr = new BigDecimal(divide(taskAndScan.getSumProcessedBytes(), 1024.0, 100)).toPlainString();
                map.put(NUM, !numStr.contains(".") ? numStr : numStr.substring(0, numStr.indexOf(".")));
                map.put("displayNum", CommonUtil.convertSize(taskAndScan.getSumProcessedBytes()));
                return map;
            }

            map.put(NUM, taskAndScan.getQueryNum());
            return map;
        }).collect(Collectors.toList());
        result.put(DATA, scanListMap);
        return result;
    }

    private Double divide(Double num1, Double num2, Integer scale) {
        return (double) Math.round(num1 / num2 * scale) / scale;
    }

    private Double scale(Double num, Integer scale) {
        return (double) Math.round(num * scale) / scale;
    }

    private List<String> getRecent(Integer recent, String current) {
        String last = "";
        String last2 = "";
        switch (recent) {
            case 0:
                last = DateUtil.lastWeek(current);
                last2 = DateUtil.last2Week(current);
                break;
            case 1:
                last = DateUtil.lastMonth(current);
                last2 = DateUtil.last2Month(current);
                break;
            case 2:
                last = DateUtil.last3Month(current);
                last2 = DateUtil.last6Month(current);
                break;
        }

        List<String> strings = Arrays.asList(last, last2);
        return strings;
    }

    public String division(long num1, long num2) {
        String rate = "0%";
        //定义格式化起始位数
        String format = "0";
        if (num2 != 0 && num1 != 0) {
            DecimalFormat dec = new DecimalFormat(format);
            rate = dec.format((double) num1 / num2 * 100) + "%";
//            while(true){
//                if(rate.equals(format+"%")){
//                    format=format+"0";
//                    DecimalFormat dec1 = new DecimalFormat(format);
//                    rate =  dec1.format((double) num1 / num2*100)+"%";
//                }else {
//                    break;
//                }
//            }
        } else if (num1 != 0 && num2 == 0) {
            rate = "0%";
        }
        return rate;
    }

    public String division(double num1, double num2) {
        String rate = "0%";
        //定义格式化起始位数
        String format = "0";
        if (num2 != 0 && num1 != 0) {
            DecimalFormat dec = new DecimalFormat(format);
            rate = dec.format((double) num1 / num2 * 100) + "%";
//            while(true){
//                if(rate.equals(format+"%")){
//                    format=format+"0";
//                    DecimalFormat dec1 = new DecimalFormat(format);
//                    rate =  dec1.format((double) num1 / num2*100)+"%";
//                }else {
//                    break;
//                }
//            }
        } else if (num1 != 0 && num2 == 0) {
            rate = "0%";
        }
        return rate;
    }
}
