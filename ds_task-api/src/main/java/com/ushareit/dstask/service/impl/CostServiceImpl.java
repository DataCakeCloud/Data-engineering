package com.ushareit.dstask.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.cost.*;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.CostType;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.mapper.ArtifactMapper;
import com.ushareit.dstask.mapper.CostMonitorMapper;
import com.ushareit.dstask.mapper.CostMonitorNoticeMapper;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.out.CostMapper;
import com.ushareit.dstask.service.CostService;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.third.dingding.DingDingService;
import com.ushareit.dstask.service.LabelService;
import com.ushareit.dstask.utils.GsonUtil;
import com.ushareit.dstask.web.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

//@Service
@Slf4j
public class CostServiceImpl implements CostService, CommandLineRunner {

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${server-url.host}")
    private String noticeUrl = "";

    @Autowired
    CostMonitorMapper costMonitorMapper;

    @Autowired
    CostMapper costMapper;

    @Autowired
    ArtifactMapper artifactMapper;

    @Autowired
    private CostMonitorNoticeMapper costMonitorNoticeMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private DingDingService dingDingService;

    @Autowired
    private LabelService labelService;

    private Map<String, List<CostResponseVo>> jobMap = Maps.newHashMap();

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private List<String> regions = Lists.newArrayList();
    private List<String> products = Lists.newArrayList();

    //@PostConstruct
    public void init() {
        wrapJob();
    }

    public List<CostResponseVo> selectCost(CostRequestVo costRequestVo) {
        if (costRequestVo.getLabelId() != null && costRequestVo.getLabelId() > 0) {
            Label label = labelService.getLabel(costRequestVo.getLabelId());
            if (label != null && !StringUtils.isEmpty(label.getTasks())) {
                List<Integer> list = Arrays.stream(label.getTasks().split(",")).map(s -> Integer.valueOf(s)).collect(Collectors.toList());
                List<Task> tasks = taskMapper.queryByIds(list);
                if (!CollectionUtils.isEmpty(tasks)) {
                    costRequestVo.setJobNames(tasks.stream().map(Task::getName).collect(Collectors.toList()));
                }
            }
        }

        List<CostResponseVo> costResponseVoList = Lists.newArrayList();
        costRequestVo.bak();
        if (costRequestVo.isPuAndDp() && CollectionUtils.isEmpty(costRequestVo.getPus())) {
            costRequestVo.setCostType(CostType.DP.name());
        }
        costResponseVoList = cost(costRequestVo);
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            CostUtils.wrapStatName(costRequestVo, costResponseVoList);
            costRequestVo.unBak();
            // wrapCumulativeCost(costRequestVo, costResponseVoList);
        }
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            CostUtils.wrapStatName(costRequestVo, costResponseVoList);
            if (costRequestVo.getGroupby() != null && costRequestVo.getGroupby() > 0 && (CostType.PU.name().equals(costRequestVo.getCostType()) || CostType.DP.name().equals(costRequestVo.getCostType()))) {
                costResponseVoList = CostUtils.wraPuGroupBy(costResponseVoList, costRequestVo);
            }
            if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
                selectJobOwner(costResponseVoList, costRequestVo);
            }
            if (costRequestVo.isPuAndDp()) {
                CostUtils.wrapPuAndDp(costResponseVoList);
            }
        }
        return costResponseVoList;
    }

    private List<CostResponseVo> cost(CostRequestVo costRequestVo) {
        wrapCostQueryPus(costRequestVo);
        List<CostResponseVo> costResponseVos = Collections.synchronizedList(Lists.newArrayList());
        if ((costRequestVo.getGroupby() == null || costRequestVo.getGroupby() == 0) && (!CollectionUtils.isEmpty(costRequestVo.getDates()) || costRequestVo.getStartDate().equals(costRequestVo.getEndDate()))) {
            Set<String> searchDates = Sets.newHashSet();
            Set<String> dates = Sets.newHashSet();
            Map<String, String> lastDayMap = Maps.newLinkedHashMap();
            Map<String, String> lastWeekMap = Maps.newLinkedHashMap();
            if (CollectionUtils.isEmpty(costRequestVo.getDates())) {
                String date = costRequestVo.getStartDate();
                String lastDay = DateUtil.lastDay(date);
                String lastWeek = DateUtil.lastWeek(date);
                costRequestVo.setStartDate(null);
                costRequestVo.setEndDate(null);
                lastDayMap.put(date, lastDay);
                lastWeekMap.put(date, lastWeek);
                dates.add(date);
                dates.add(lastDay);
                dates.add(lastWeek);
                searchDates.add(date);
            } else {
                for (String date : costRequestVo.getDates()) {
                    dates.add(date);
                    String lastDay = DateUtil.lastDay(date);
                    dates.add(lastDay);
                    String lastWeek = DateUtil.lastWeek(date);
                    dates.add(lastWeek);
                    lastDayMap.put(date, lastDay);
                    lastWeekMap.put(date, lastWeek);
                    searchDates.add(date);
                }
                costRequestVo.setDates(Lists.newArrayList(dates));
                costRequestVo.setStartDate(null);
                costRequestVo.setEndDate(null);
            }

            if (CollectionUtils.isEmpty(costRequestVo.getDates())) {
                costResponseVos = costRequestVo.getCostType().equals(CostType.PU.name()) ? costMapper.selectCostPu(costRequestVo) : costRequestVo.getCostType().equals(CostType.DP.name()) ? costMapper.selectCostDp(costRequestVo) : costMapper.selectCost(costRequestVo);
            } else {
                List<List<String>> ds = PubMethod.subList(costRequestVo.getDates(), 3);
                CountDownLatch countDownLatch = new CountDownLatch(ds.size());
                for (List<String> d : ds) {
                    CostRequestVo n = new CostRequestVo();
                    BeanUtils.copyProperties(costRequestVo, n);
                    n.setDates(d);
                    executorService.execute(new Handle(countDownLatch, costMapper, costResponseVos, n));
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            costResponseVos = CostUtils.wrapCostResponseVo(costRequestVo, costResponseVos, lastDayMap, lastWeekMap, searchDates);
        } else {
            costResponseVos = costRequestVo.getCostType().equals(CostType.PU.name()) ? costMapper.selectCostPu(costRequestVo) : costRequestVo.getCostType().equals(CostType.DP.name()) ? costMapper.selectCostDp(costRequestVo) : costMapper.selectCost(costRequestVo);

        }
        return costResponseVos;
    }


    private void wrapCumulativeCost(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVos) {
        if (costRequestVo.isNeedCumulativeCost() && !CollectionUtils.isEmpty(costResponseVos)) {
            if (CollectionUtils.isEmpty(costRequestVo.getDates())) {
                Set<String> sets = CostUtils.addSet(costRequestVo, costResponseVos);
                CostUtils.wrapCostRequestVo(costRequestVo, sets);
                List<CostResponseVo> cuList = Lists.newArrayList();
                if (costRequestVo.getCostType().equals(CostType.DP.name())) {
                    cuList = costMapper.selectCumulativeCostDp(costRequestVo);
                } else {
                    cuList = costMapper.selectCumulativeCost(costRequestVo);
                }
                CostUtils.wrapStatName(costRequestVo, cuList);
                CostUtils.wrapCumulativeCost(costRequestVo, costResponseVos, cuList);
            } else {
                for (String date : costRequestVo.getDates()) {
                    //Set<String> sets = CostUtils.addSet(costRequestVo, costResponseVos);
                    //CostUtils.wrapCostRequestVo(costRequestVo, sets);
                    costRequestVo.setStartDate(date);
                    costRequestVo.setEndDate(date);
                    List<CostResponseVo> cuList = Lists.newArrayList();
                    if (costRequestVo.getCostType().equals(CostType.DP.name())) {
                        cuList = costMapper.selectCumulativeCostDp(costRequestVo);
                    } else {
                        cuList = costMapper.selectCumulativeCost(costRequestVo);
                    }
                    CostUtils.wrapStatName(costRequestVo, cuList);
                    CostUtils.wrapCumulativeCost(costRequestVo, costResponseVos, cuList);
                }
            }
        }
    }


    @Override
    public void saveAndEditCostMonitor(CostMonitorRequestVo costMonitorRequestVo) {
        if (costMonitorRequestVo.getId() != null) {
            CostMonitor costMonitor = costMonitorMapper.selectByPrimaryKey(costMonitorRequestVo.getId());
            BeanUtils.copyProperties(costMonitorRequestVo, costMonitor, "id", "valid", "createTime", "createShareitId");
            CostUtils.listToStr(costMonitor, costMonitorRequestVo);
            costMonitorMapper.updateByPrimaryKey(costMonitor);
        } else {
            CostMonitor costMonitor = new CostMonitor();
            BeanUtils.copyProperties(costMonitorRequestVo, costMonitor);
            costMonitor.setValid(true);
            costMonitor.setSendNotice(false);
            costMonitor.setCreateTime(DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD_HH_MM));
            CostUtils.listToStr(costMonitor, costMonitorRequestVo);
            costMonitorMapper.insert(costMonitor);
        }
    }

    @Override
    public void deleteCostMonitor(Integer id) {
        CostMonitor costMonitor = costMonitorMapper.selectByPrimaryKey(id);
        costMonitor.setValid(false);
        costMonitorMapper.updateByPrimaryKey(costMonitor);
    }

    @Override
    public List<CostMonitorRequestVo> list(String createShareitId) {
        List<CostMonitorRequestVo> costMonitorRequestVoList = Lists.newArrayList();
        List<CostMonitor> costMonitorList = admin(createShareitId) ? costMonitorMapper.selectAllByValid() : costMonitorMapper.selectByCreateShareitId(createShareitId);
        if (!CollectionUtils.isEmpty(costMonitorList)) {
            costMonitorList.forEach(costMonitor -> {
                CostMonitorRequestVo costMonitorRequestVo = new CostMonitorRequestVo();
                BeanUtils.copyProperties(costMonitor, costMonitorRequestVo);
                CostUtils.strToList(costMonitor, costMonitorRequestVo);
                costMonitorRequestVoList.add(costMonitorRequestVo);
            });
        }
        return costMonitorRequestVoList;
    }

    @Override
    public CostMonitorRequestVo getById(Integer id) {
        CostMonitor costMonitor = costMonitorMapper.selectByPrimaryKey(id);
        CostMonitorRequestVo costMonitorRequestVo = new CostMonitorRequestVo();
        BeanUtils.copyProperties(costMonitor, costMonitorRequestVo);
        CostUtils.strToList(costMonitor, costMonitorRequestVo);
        return costMonitorRequestVo;
    }

    @Scheduled(cron = "1 30 * * * ?")
    public void wrapJob() {
        jobMap.clear();
        jobMap=Maps.newHashMap();
        regions=costMapper.selectRegions(new CostDictionaryVo());
        products=costMapper.selectProducts(new CostDictionaryVo());
        List<CostResponseVo> ownerAndDepartment = costMapper.selectJobOwner();
       /* if (!CollectionUtils.isEmpty(ownerAndDepartment)) {
            Map<String, CostResponseVo> map = Maps.newHashMap();
            int i = 0;
            for (CostResponseVo costResponseVo : ownerAndDepartment) {
                if (!map.containsKey(costResponseVo.getJobName())) {
                    map.put(costResponseVo.getJobName(), costResponseVo);
                }
            }
            ownerAndDepartment = Lists.newArrayList(map.values());
        }*/
        List<CostResponseVo> departmentAndPu = costMapper.selectDepartmentAndPu(new CostRequestVo());
        Set<String> jobNames = Sets.newHashSet();
        for (CostResponseVo c : ownerAndDepartment) {
            if (!StringUtils.isEmpty(c.getDepartmentName())) {
                for (CostResponseVo dp : departmentAndPu) {
                    if (c.getDepartmentName().equals(dp.getDepartmentName())) {
                        if (StringUtils.isEmpty(c.getPuName())) {
                            c.setPuName(dp.getPuName());
                        } else {
                            c.setPuName(c.getPuName() + "," + dp.getPuName());
                        }
                    }
                }
            }
            if (jobMap.containsKey(c.getJobName())) {
                jobMap.get(c.getJobName()).add(c);
            } else {
                jobMap.put(c.getJobName(), Lists.newArrayList(c));
            }
            jobNames.add(c.getJobName());
        }
        List<List<String>> listList = PubMethod.subList(Lists.newArrayList(jobNames), 200);
        for (List<String> list : listList) {
            List<Task> tasks = taskMapper.selectWithNames(list);
            for (Task task : tasks) {
                List<CostResponseVo> costResponseVos = jobMap.get(task.getName());
                if (!CollectionUtils.isEmpty(costResponseVos)) {
                    for (CostResponseVo costResponseVo : costResponseVos) {
                        costResponseVo.setJobId(String.valueOf(task.getId()));
                        if (task.getCreateTime() != null) {
                            Date date = new Date(task.getCreateTime().getTime());
                            costResponseVo.setJobCreateTime(DateTimeUtils.getDateStr(date, DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD));
                        }
                    }

                }
            }
        }
        /*String monthDay = DateTimeUtils.getDateStr(DateTimeUtils.before(new Date(), 31, ChronoUnit.DAYS), DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
        String weekDay = DateTimeUtils.getDateStr(DateTimeUtils.before(new Date(), 8, ChronoUnit.DAYS), DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
        CostRequestVo costRequestVo = CostRequestVo.builder().costType(CostType.JOB.name()).endDate(DateUtil.lastDay())
                .startDate(monthDay).build();
        List<CostResponseVo> costResponseVoListMonth = selectCost(costRequestVo);
        costRequestVo.setStartDate(weekDay);
        List<CostResponseVo> costResponseVoListWeek = selectCost(costRequestVo);
        CostUtils.wrapJobCost(costResponseVoListMonth, costResponseVoListWeek, jobMap);*/
        try {
            if (!StringUtils.isEmpty(active) && active.toLowerCase().indexOf(DsTaskConstant.PROD) > -1) {
                costMapper.deleteJobDetail();
                if (jobMap != null && jobMap.size() > 0) {
                    List<CostResponseVo> all = Lists.newArrayList();
                    jobMap.values().forEach(costResponseVoList -> {
                        all.addAll(costResponseVoList);
                    });
                    List<List<CostResponseVo>> lists = PubMethod.subList(all, 1000);
                    for (List<CostResponseVo> list : lists) {
                        costMapper.insertBatchJobDetail(list);
                    }
                }
            }

        } catch (Exception e) {

        }

    }

    private Random random = new Random();

    @Scheduled(cron = "1 10 2 * * ?")
    @DisLock(key = "startCostMonitorJob", expiredSeconds = 30, isRelease = false)
    public void startCostMonitorJob() throws InterruptedException {
        Thread.currentThread().sleep(random.nextInt(1000));
        List<CostMonitorNotice> costMonitorNoticeList = costMonitorNoticeMapper.selectByShareitIdAndNoticeTime("system", DateUtil.getNowDateStr());
        if (!CollectionUtils.isEmpty(costMonitorNoticeList)) {
            return;
        }
        CostMonitorNotice costMonitorNotice = CostMonitorNotice.builder().noticeTime(DateUtil.getNowDateStr()).createShareitId("system").build();
        costMonitorNoticeMapper.insert(costMonitorNotice);
        List<CostMonitor> costMonitorList = costMonitorMapper.selectAllByValid();
        if (!CollectionUtils.isEmpty(costMonitorList)) {
            for (CostMonitor costMonitor : costMonitorList) {
                switch (costMonitor.getType()) {
                    case 1:
                        handleNewJobMonitor(costMonitor);
                        continue;
                    case 2:
                        handleRatioJobMonitor(costMonitor);
                        continue;
                    case 3:
                        handleRatioJobMonitor(costMonitor);
                        continue;
                    default:
                        continue;
                }
            }
        }
    }

    //@Scheduled(cron = "1 */10 * * * ?")
    //@DisLock(key = "checkCostMonitorJob", expiredSeconds = 1, isRelease = false)
    public void checkCostMonitorJob() {
        String time = DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.HH_MM);
        if (time.compareTo("10:29") > 0) {
            List<CostMonitorNotice> costMonitorNoticeList = costMonitorNoticeMapper.selectByShareitIdAndNoticeTime("system", DateUtil.getNowDateStr());
            if (CollectionUtils.isEmpty(costMonitorNoticeList)) {
                //startCostMonitorJob();
            }
        }
    }


    //private int type;/1部门 2PU 3owner 4任务  5 区域 6 产品
    //    private String name;//
    @Override
    public List<String> listDictionary(CostDictionaryVo costDictionaryVo) {
        if (costDictionaryVo.getId() != null) {
            CostMonitor costMonitor = costMonitorMapper.selectByPrimaryKey(costDictionaryVo.getId());
            if (costMonitor != null) {
                if (costDictionaryVo.getType() == 2 && !StringUtils.isEmpty(costMonitor.getPus())) {
                    return GsonUtil.parseFromJson(costMonitor.getPus(), new TypeToken<List<String>>() {
                    }.getType());
                }
                if (costDictionaryVo.getType() == 1 && !StringUtils.isEmpty(costMonitor.getDps())) {
                    return GsonUtil.parseFromJson(costMonitor.getDps(), new TypeToken<List<String>>() {
                    }.getType());
                }
                if (costDictionaryVo.getType() == 3 && !StringUtils.isEmpty(costMonitor.getOwners())) {
                    return GsonUtil.parseFromJson(costMonitor.getOwners(), new TypeToken<List<String>>() {
                    }.getType());
                }
                if (costDictionaryVo.getType() == 4 && !StringUtils.isEmpty(costMonitor.getJobs())) {
                    return GsonUtil.parseFromJson(costMonitor.getJobs(), new TypeToken<List<String>>() {
                    }.getType());
                }
            }
        }
        if (costDictionaryVo.getType() == 2) {
            return BaseConstant.PUS;
        }
        if (costDictionaryVo.getType() == 1) {
            return costMapper.selectDepartment(costDictionaryVo);
        }
        if (costDictionaryVo.getType() == 3) {
            if (!CollectionUtils.isEmpty(costDictionaryVo.getDepartments()) || !CollectionUtils.isEmpty(costDictionaryVo.getRegions()) || !CollectionUtils.isEmpty(costDictionaryVo.getProducts())) {
                return costMapper.selectOwners(costDictionaryVo);
            }else {
                ItUtil itUtil = new ItUtil();
                List<UserBase> userBases=itUtil.getSubordinate(costDictionaryVo.getShareitId());
                if (CollectionUtils.isEmpty(userBases)){
                    return Lists.newArrayList(costDictionaryVo.getShareitId());
                } else {
                    return userBases.stream().map(userBase -> userBase.getShareId()).collect(Collectors.toList());
                }
            }

        }
        if (costDictionaryVo.getType() == 4) {
            /*List<UserBase> userBases=ItUtil.getSubordinate(costDictionaryVo.getShareitId());
            if (!CollectionUtils.isEmpty(userBases)){
                costDictionaryVo.setOwners(userBases.stream().map(userBase -> userBase.getShareId()).collect(Collectors.toList()));
                costDictionaryVo.setShareitId(null);
            }*/
            return costMapper.selectJobs(costDictionaryVo);
            /*if (!StringUtils.isEmpty(costDictionaryVo.getName())){
                //return taskMapper.selectLikeNameLimit50(costDictionaryVo.getName());
            }else {
                return costMapper.selectJobs(costDictionaryVo);
            }*/
        }
        if (costDictionaryVo.getType() == 5) {
            return regions;//costMapper.selectRegions(costDictionaryVo);
        }
        if (costDictionaryVo.getType() == 6) {
            return products;//costMapper.selectProducts(costDictionaryVo);
        }
        return null;

    }

    @Override
    public List<CostResponseVo> selectNewJob(CostRequestVo costRequestVo, CostMonitor costMonitor) {
        if (costMonitor.getMonitorLevel() == 2) {//pu
            if (CollectionUtils.isEmpty(costRequestVo.getPus())) {
                costRequestVo.setPus(GsonUtil.parseFromJson(costMonitor.getPus(), new TypeToken<List<String>>() {
                }.getType()));
            }
            List<String> dps = costMapper.selectPuDepartment(costRequestVo);
            if (!CollectionUtils.isEmpty(dps)) {
                costRequestVo.setCostType(CostType.DP.name());
                costRequestVo.setDepartments(dps);
            }
        }
        if (costMonitor.getMonitorLevel() == 1) {
            costRequestVo.setCostType(CostType.DP.name());
            if (CollectionUtils.isEmpty(costRequestVo.getDepartments())) {
                costRequestVo.setDepartments(GsonUtil.parseFromJson(costMonitor.getDps(), new TypeToken<List<String>>() {
                }.getType()));
            }

        }
        if (costMonitor.getMonitorLevel() == 3) {
            costRequestVo.setCostType(CostType.OWNER.name());
            if (CollectionUtils.isEmpty(costRequestVo.getOwners())) {
                costRequestVo.setOwners(GsonUtil.parseFromJson(costMonitor.getOwners(), new TypeToken<List<String>>() {
                }.getType()));
            }

        }
        List<CostResponseVo> jobs = costMapper.selectNewJob(costRequestVo);
        if (!CollectionUtils.isEmpty(jobs)) {
            selectJobOwner(jobs, costRequestVo);
        }
        return jobs;
    }

    @Override
    public List<CostResponseVo> selectRatioJob(CostRequestVo costRequestVo, CostMonitor costMonitor) {
        if (costMonitor.getMonitorLevel() == 1) {//部门
            if (CollectionUtils.isEmpty(costRequestVo.getDepartments())) {
                costRequestVo.setDepartments(GsonUtil.parseFromJson(costMonitor.getDps(), new TypeToken<List<String>>() {
                }.getType()));
            }
            costRequestVo.setCostType(CostType.DP.name());
        }
        if (costMonitor.getMonitorLevel() == 2) {//pu
            if (CollectionUtils.isEmpty(costRequestVo.getPus())) {
                costRequestVo.setPus(GsonUtil.parseFromJson(costMonitor.getPus(), new TypeToken<List<String>>() {
                }.getType()));
            }
            costRequestVo.setCostType(CostType.PU.name());
        }
        if (costMonitor.getMonitorLevel() == 3) {//owners
            if (CollectionUtils.isEmpty(costRequestVo.getOwners())) {
                costRequestVo.setOwners(GsonUtil.parseFromJson(costMonitor.getOwners(), new TypeToken<List<String>>() {
                }.getType()));
            }
            costRequestVo.setCostType(CostType.OWNER.name());
        }
        if (costMonitor.getMonitorLevel() == 4) {//jobs
            if (CollectionUtils.isEmpty(costRequestVo.getJobNames())) {
                costRequestVo.setJobNames(GsonUtil.parseFromJson(costMonitor.getJobs(), new TypeToken<List<String>>() {
                }.getType()));
            }
            costRequestVo.setCostType(CostType.JOB.name());
        }
        List<CostResponseVo> costResponseVoList = selectCost(costRequestVo);
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            costResponseVoList = costResponseVoList.stream().filter(costResponseVo ->
                    PubMethod.compareRatio(costResponseVo, costMonitor)
            ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(costResponseVoList) && CostType.JOB.name().equals(costRequestVo.getCostType())) {
                selectJobOwner(costResponseVoList, costRequestVo);
            }
        }
        return costResponseVoList;
    }

    @Override
    public List<CostResponseVo> monitorJob(CostMonitorNoticeRequestVo costMonitorNoticeRequestVo) {
        List<CostResponseVo> costResponseVoList = Lists.newArrayList();
        CostMonitor costMonitor = costMonitorMapper.selectByPrimaryKey(costMonitorNoticeRequestVo.getId());
        if (costMonitor != null) {
            CostRequestVo costRequestVo = new CostRequestVo();
            costRequestVo.setDates(costMonitorNoticeRequestVo.getDates());
            costRequestVo.setJobNames(costMonitorNoticeRequestVo.getJobNames());
            costRequestVo.setOwners(costMonitorNoticeRequestVo.getOwners());
            costRequestVo.setDepartments(costMonitorNoticeRequestVo.getDepartments());
            costRequestVo.setPus(costMonitorNoticeRequestVo.getPus());
            if (costMonitor.getType() == 1) {
                if (!CollectionUtils.isEmpty(costMonitorNoticeRequestVo.getDates())) {
                    for (String date : costMonitorNoticeRequestVo.getDates()) {
                        costRequestVo.setStartDate(date);
                        List<CostResponseVo> cs = selectNewJob(costRequestVo, costMonitor);
                        if (!CollectionUtils.isEmpty(cs)) {
                            costResponseVoList.addAll(cs);
                        }
                    }
                }
            } else {
                costResponseVoList = selectRatioJob(costRequestVo, costMonitor);
            }
        }
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            Collections.sort(costResponseVoList, new Comparator<CostResponseVo>() {
                @Override
                public int compare(CostResponseVo o1, CostResponseVo o2) {
                    if (!StringUtils.isEmpty(o1.getDt()) && !StringUtils.isEmpty(o2.getDt())) {
                        return o1.getDt().compareTo(o2.getDt());
                    }
                    return 0;
                }
            });
        }
        return costResponseVoList;
    }

    @Override
    public void selectJobOwner(List<CostResponseVo> costResponseVos, CostRequestVo costRequestVo) {
        try {
            if (!CollectionUtils.isEmpty(costResponseVos)) {
                costResponseVos.forEach(costResponseVo -> {
                    if (!StringUtils.isEmpty(costResponseVo.getJobName())) {
                        List<CostResponseVo> cs = jobMap.get(costResponseVo.getJobName());
                        if (!CollectionUtils.isEmpty(cs)) {
                            CostResponseVo result = cs.get(0);
                            costResponseVo.setOwner(result.getOwner());
                            costResponseVo.setDepartmentName(result.getDepartmentName());
                            costResponseVo.setPuName(result.getPuName());
                            costResponseVo.setJobId(result.getJobId());
                            costResponseVo.setCumulativejobNameQuantity30(result.getCumulativejobNameQuantity30());
                            costResponseVo.setCumulativejobNameQuantity7(result.getCumulativejobNameQuantity7());
                            costResponseVo.setCumulativeCost30(result.getCumulativeCost30());
                            costResponseVo.setCumulativeCost7(result.getCumulativeCost7());
                            costResponseVo.setJobCreateTime(result.getJobCreateTime());
                            if (cs.size() > 1) {
                                if (!CollectionUtils.isEmpty(costRequestVo.getDepartments())) {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (String s : costRequestVo.getDepartments()) {
                                        for (CostResponseVo c : cs) {
                                            if (s.equals(c.getDepartmentName())) {
                                                stringBuffer.append(c.getOwner()).append(" ");
                                            }
                                        }
                                    }
                                    if (!StringUtils.isEmpty(stringBuffer.toString())){
                                        costResponseVo.setOwner(stringBuffer.toString().trim());
                                    }
                                } else {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (CostResponseVo c : cs) {
                                        if (!StringUtils.isEmpty(c.getOwner())) {
                                            stringBuffer.append(c.getOwner()).append(" ");
                                        }
                                    }
                                    if (!StringUtils.isEmpty(stringBuffer.toString())){
                                        costResponseVo.setOwner(stringBuffer.toString().trim());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("", e);
        }

    }


    @Override
    public List<CostMonitorNotice> jobNoticeList(CostMonitorNoticeRequestVo costMonitorNoticeRequestVo) {
        List<CostMonitorNotice> costMonitorNoticeList = costMonitorNoticeMapper.selectByShareitId(costMonitorNoticeRequestVo.getShareitId());
        return costMonitorNoticeList;
    }

    /**
     * 新任务
     *
     * @param costMonitor
     */
    private void handleNewJobMonitor(CostMonitor costMonitor) {
        CostRequestVo costRequestVo = new CostRequestVo();
        costRequestVo.setStartDate(DateUtil.lastDay());
        List<CostResponseVo> jobs = selectNewJob(costRequestVo, costMonitor);
        if (!CollectionUtils.isEmpty(jobs)) {
            String notice = CostUtils.wrapNewJobNotice(costRequestVo, jobs, costMonitor, noticeUrl + "admin/cost/monitor/detail?type=%s&ratio=%s&monitorLevel=%s&id=%s&shareitId=%s&dates=%s");
            dingDingService.notify(CostUtils.wrapNotice(costMonitor), notice);
            CostMonitorNotice costMonitorNotice = CostMonitorNotice.builder().content(notice).costMonitorId(costMonitor.getId()).name(costMonitor.getName()).noticeTime(DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD_HH_MM_SS)).createShareitId(costMonitor.getCreateShareitId()).build();
            costMonitorNoticeMapper.insert(costMonitorNotice);
            costMonitor.setExecuteTime(costMonitorNotice.getNoticeTime());
            costMonitor.setSendNotice(true);
            costMonitorMapper.updateByPrimaryKey(costMonitor);
        }
    }

    private void wrapCostQueryPus(CostRequestVo costRequestVo) {
        if (!StringUtils.isEmpty(costRequestVo.getCostQueryPus())) {
            CostRequestVo newc = CostRequestVo.builder().pus(costRequestVo.getCostQueryPus()).build();
            List<String> departments = costMapper.selectPuDepartment(newc);
            if (!CollectionUtils.isEmpty(departments)) {
                costRequestVo.setDepartments(departments);
            }

        }
    }

    /**
     * 同比
     *
     * @param costMonitor
     */
    ///cost/monitor/detail?type=%s&ratio=%s&monitorLevel=%s&id=%s&shareitId=%s&dates=%s
    private void handleRatioJobMonitor(CostMonitor costMonitor) {
        if (!PubMethod.executeMonitor(costMonitor)) {
            return;
        }
        CostRequestVo costRequestVo = new CostRequestVo();
        costRequestVo.setStartDate(DateUtil.lastDay());
        costRequestVo.setEndDate(DateUtil.lastDay());
        List<CostResponseVo> costResponseVoList = selectRatioJob(costRequestVo, costMonitor);
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            String notice = CostUtils.wrapRatioNotice(costRequestVo, costResponseVoList, costMonitor, noticeUrl + "admin/cost/monitor/detail?type=%s&ratio=%s&monitorLevel=%s&id=%s&shareitId=%s&dates=%s");
            dingDingService.notify(CostUtils.wrapNotice(costMonitor), notice);
            CostMonitorNotice costMonitorNotice = CostMonitorNotice.builder().content(notice).costMonitorId(costMonitor.getId()).name(costMonitor.getName()).noticeTime(DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD_HH_MM_SS)).createShareitId(costMonitor.getCreateShareitId()).build();
            costMonitorNoticeMapper.insert(costMonitorNotice);
            costMonitor.setExecuteTime(costMonitorNotice.getNoticeTime());
            costMonitor.setSendNotice(true);
            costMonitorMapper.updateByPrimaryKey(costMonitor);
        }
    }


    @Scheduled(cron = "59 */10 * * * ?")
    public void initSearch() {
        CostRequestVo costRequestVo = new CostRequestVo();
        costRequestVo.setDates(Lists.newArrayList(DateUtil.lastDay()));
        costRequestVo.setCostType(CostType.OWNER.name());
        costRequestVo.setNeedCumulativeCost(true);
        cost(costRequestVo);
        costRequestVo.setCostType(CostType.JOB.name());
        cost(costRequestVo);
        costRequestVo.setCostType(CostType.PRODUCT.name());
        cost(costRequestVo);
        costRequestVo.setDates(null);
        costRequestVo.setStartDate(DateTimeUtils.getDateStr(DateTimeUtils.before(new Date(), 30, ChronoUnit.DAYS), DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD));
        costRequestVo.setEndDate(DateUtil.lastDay());
        costRequestVo.setCostType(CostType.DP.name());
        cost(costRequestVo);
        costRequestVo.setCostType(CostType.PU.name());
        cost(costRequestVo);
        costRequestVo.setStartDate(DateTimeUtils.getCurrentDateStr(DateTimeUtils.DateTimeFormatterEnum.YYYY_MM) + "-01");
        costRequestVo.setEndDate(DateUtil.lastDay());
        costRequestVo.setCostType(CostType.DP.name());
        cost(costRequestVo);
        costRequestVo.setCostType(CostType.PU.name());
        cost(costRequestVo);
        costRequestVo.setStartDate(DateUtil.firstDayOfQuarter());
        costRequestVo.setEndDate(DateUtil.lastDay());
        costRequestVo.setCostType(CostType.DP.name());
        cost(costRequestVo);
        costRequestVo.setCostType(CostType.PU.name());
        cost(costRequestVo);
    }

    //insert into cost_monitor_notice(create_shareit_id,content) values('root','hanzenggui,zhuzhe,'linyang');
    private boolean admin(String shareitId) {
        List<CostMonitorNotice> costMonitorNoticeList = costMonitorNoticeMapper.selectByShareitId("admin");
        if (!CollectionUtils.isEmpty(costMonitorNoticeList)) {
            String admin = costMonitorNoticeList.get(0).getContent();
            if (!StringUtils.isEmpty(admin) && admin.indexOf(shareitId) > -1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(String... args) throws Exception {
//        wrapJob();
    }


    public static class Handle implements Runnable {
        public CountDownLatch countDownLatch;

        private CostMapper costMapper;

        private List<CostResponseVo> costResponseVoList;

        private CostRequestVo costRequestVo;

        public Handle(CountDownLatch countDownLatch, CostMapper costMapper, List<CostResponseVo> costResponseVoList, CostRequestVo costRequestVo) {
            this.countDownLatch = countDownLatch;
            this.costMapper = costMapper;
            this.costResponseVoList = costResponseVoList;
            this.costRequestVo = costRequestVo;
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                List<CostResponseVo> responses = costRequestVo.getCostType().equals(CostType.PU.name()) ? costMapper.selectCostPu(costRequestVo) : costRequestVo.getCostType().equals(CostType.DP.name()) ? costMapper.selectCostDp(costRequestVo) : costMapper.selectCost(costRequestVo);
                if (!CollectionUtils.isEmpty(responses)) {
                    this.costResponseVoList.addAll(responses);
                }
                //System.out.println(System.currentTimeMillis()-start);
            } catch (Exception e) {

            } finally {
                countDownLatch.countDown();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new StringBuffer().toString());
    }

}
