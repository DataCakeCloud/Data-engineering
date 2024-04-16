package com.ushareit.dstask.web.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.ushareit.dstask.bean.CostMonitor;
import com.ushareit.dstask.common.vo.cost.*;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.CostType;
import com.ushareit.dstask.utils.GsonUtil;
import com.ushareit.dstask.web.utils.excel.ExcelSheetVO;
import com.ushareit.dstask.web.utils.excel.FileRenderUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

public class CostUtils {

    public static Set<String> addSet(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVos) {
        Set<String> sets = Sets.newHashSet();
        for (CostResponseVo costResponseVo : costResponseVos) {
            if (CostType.OWNER.name().equals(costRequestVo.getCostType())) {
                addSet(sets, costResponseVo.getOwner());
            }
            if (CostType.PU.name().equals(costRequestVo.getCostType())) {
                addSet(sets, costResponseVo.getPuName());
            }
            if (CostType.PRODUCT.name().equals(costRequestVo.getCostType())) {
                addSet(sets, costResponseVo.getProductName());
            }
            if (CostType.DP.name().equals(costRequestVo.getCostType())) {
                addSet(sets, costResponseVo.getDepartmentName());
            }
            if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
                addSet(sets, costResponseVo.getJobName());
            }
        }
        return sets;
    }

    public static void wrapCostRequestVo(CostRequestVo costRequestVo, Set<String> sets) {
        if (CostType.OWNER.name().equals(costRequestVo.getCostType())) {
            costRequestVo.setOwners(Lists.newArrayList(sets));
        }
        if (CostType.PU.name().equals(costRequestVo.getCostType())) {
            costRequestVo.setPus(Lists.newArrayList(sets));
        }
        if (CostType.PRODUCT.name().equals(costRequestVo.getCostType())) {
            costRequestVo.setProducts(Lists.newArrayList(sets));
        }
        if (CostType.DP.name().equals(costRequestVo.getCostType())) {
            costRequestVo.setDepartments(Lists.newArrayList(sets));
        }
        if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
            costRequestVo.setJobNames(Lists.newArrayList(sets));
        }
    }

    public static void addSet(Set<String> sets, String str) {
        if (!StringUtils.isEmpty(str) && !"-".equals(str)) {
            sets.add(str);
        }
    }

    public static void wrapCumulativeCost(CostRequestVo costRequestVo, List<CostResponseVo> old, List<CostResponseVo> cumulativeCosts) {
        if (!CollectionUtils.isEmpty(cumulativeCosts)) {
            for (CostResponseVo costResponseVo : old) {
                for (CostResponseVo c : cumulativeCosts) {
                    if (c.getStatName().equals(costResponseVo.getStatName()) && costResponseVo.getDt().equals(c.getDt())) {
                        costResponseVo.setCumulativeCost(c.getCumulativeCost());
                        costResponseVo.setDt(costRequestVo.getStartDate());
                        break;
                    }
                }
            }
        }
    }

    public static void wrapStatName(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVos) {
        if (!CollectionUtils.isEmpty(costResponseVos)) {
            for (CostResponseVo costResponseVo : costResponseVos) {
                if (CostType.OWNER.name().equals(costRequestVo.getCostType())) {
                    costResponseVo.setStatName(StringUtils.isEmpty(costResponseVo.getOwner()) ? BaseConstant.EMPTY : costResponseVo.getOwner());
                }
                if (CostType.PU.name().equals(costRequestVo.getCostType())) {
                    try {
                        costResponseVo.setStatName(StringUtils.isEmpty(costResponseVo.getPuName()) ? BaseConstant.EMPTY : costResponseVo.getPuName());
                    } catch (Exception e) {
                        System.out.println("1");
                    }
                }
                if (CostType.PRODUCT.name().equals(costRequestVo.getCostType())) {
                    costResponseVo.setStatName(StringUtils.isEmpty(costResponseVo.getProductName()) ? BaseConstant.EMPTY : costResponseVo.getProductName());
                }
                if (CostType.DP.name().equals(costRequestVo.getCostType())) {
                    costResponseVo.setStatName(StringUtils.isEmpty(costResponseVo.getDepartmentName()) ? BaseConstant.AIRFLOW : costResponseVo.getDepartmentName());
                    costResponseVo.setDepartmentName(costResponseVo.getStatName());
                }
                if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
                    costResponseVo.setStatName(StringUtils.isEmpty(costResponseVo.getJobName()) ? BaseConstant.EMPTY : costResponseVo.getJobName());
                }
            }
        }
    }

    public static List<CostResponseVo> wraPuGroupBy(List<CostResponseVo> costResponseVoList, CostRequestVo costRequestVo) {
        Map<String, List<CostResponseVo>> map = Maps.newHashMap();
        if (costRequestVo.getCostType().equals(CostType.DP.name())) {
            map = PubMethod.listToListMap("departmentName", costResponseVoList);
        } else {
            map = PubMethod.listToListMap("puName", costResponseVoList);
        }
        List<CostResponseVo> result = Lists.newArrayList();
        Double total = 0d;
        for (CostResponseVo costResponseVo : costResponseVoList) {
            total += costResponseVo.getCost();
        }
        for (Map.Entry<String, List<CostResponseVo>> entry : map.entrySet()) {
            if (entry.getValue().size() == 1) {
                result.add(entry.getValue().get(0));
            } else {
                CostResponseVo costResponseVo = entry.getValue().get(0);
                for (int i = 1; i < entry.getValue().size(); i++) {
                    costResponseVo.addCost(entry.getValue().get(i).getCost());
                }
                result.add(costResponseVo);
            }
        }
        for (CostResponseVo costResponseVo : result) {
            costResponseVo.setProportion(PubMethod.divHundred2Scale(costResponseVo.getCost(), total));
        }
        return result;
    }

    public static List<CostResponseVo> wrapCostResponseVo(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVoList, Map<String, String> lastDayDates, Map<String, String> lastWeekDates, Set<String> daySets) {
        Map<String, Map<String, CostResponseVo>> keyMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            for (CostResponseVo costResponseVo : costResponseVoList) {
                if (StringUtils.isEmpty(costResponseVo.key(costRequestVo))) {
                    continue;
                }
                if (keyMap.containsKey(costResponseVo.key(costRequestVo))) {
                    keyMap.get(costResponseVo.key(costRequestVo)).put(costResponseVo.getDt(), costResponseVo);
                } else {
                    Map<String, CostResponseVo> map = Maps.newHashMap();
                    map.put(costResponseVo.getDt(), costResponseVo);
                    keyMap.put(costResponseVo.key(costRequestVo), map);
                }
            }
        }
        List<CostResponseVo> costResponseVos = Lists.newArrayList();
        for (Map.Entry<String, Map<String, CostResponseVo>> entry : keyMap.entrySet()) {
            Map<String, CostResponseVo> values = entry.getValue();
            for (Map.Entry<String, CostResponseVo> costRequestVoEntry : values.entrySet()) {
                if (daySets.contains(costRequestVoEntry.getKey())) {
                    CostResponseVo costResponseVo = costRequestVoEntry.getValue();
                    costResponseVo.wrapRelativeRatio(values.get(lastDayDates.get(costRequestVoEntry.getKey())));
                    costResponseVo.wrapBasisRatio(values.get(lastWeekDates.get(costRequestVoEntry.getKey())));
                    costResponseVos.add(costResponseVo);
                }
            }

        }
        return costResponseVos;
    }


    public static void strToList(CostMonitor costMonitor, CostMonitorRequestVo costMonitorRequestVo) {
        if (!StringUtils.isEmpty(costMonitor.getDps())) {
            costMonitorRequestVo.setDpList(GsonUtil.parseFromJson(costMonitor.getDps(), new TypeToken<List<String>>() {
            }.getType()));
        }
        if (!StringUtils.isEmpty(costMonitor.getPus())) {
            costMonitorRequestVo.setPuList(GsonUtil.parseFromJson(costMonitor.getPus(), new TypeToken<List<String>>() {
            }.getType()));
        }
        if (!StringUtils.isEmpty(costMonitor.getOwners())) {
            costMonitorRequestVo.setOwnerList(GsonUtil.parseFromJson(costMonitor.getOwners(), new TypeToken<List<String>>() {
            }.getType()));
        }
        if (!StringUtils.isEmpty(costMonitor.getJobs())) {
            costMonitorRequestVo.setJobList(GsonUtil.parseFromJson(costMonitor.getJobs(), new TypeToken<List<String>>() {
            }.getType()));
        }
        if (!StringUtils.isEmpty(costMonitor.getNoticePersons())) {
            costMonitorRequestVo.setNoticePersons(GsonUtil.parseFromJson(costMonitor.getNoticePersons(), new TypeToken<List<String>>() {
            }.getType()));
        }
        if (!StringUtils.isEmpty(costMonitor.getFrep())) {
            costMonitorRequestVo.setFrep(GsonUtil.parseFromJson(costMonitor.getFrep(), new TypeToken<List<String>>() {
            }.getType()));
        }
    }

    public static void listToStr(CostMonitor costMonitor, CostMonitorRequestVo costMonitorRequestVo) {
        costMonitor.setDps(null);
        costMonitor.setPus(null);
        costMonitor.setOwners(null);
        costMonitor.setJobs(null);
        costMonitor.setNoticePersons(null);
        costMonitor.setFrep(null);
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getDpList())) {
            costMonitor.setDps(GsonUtil.toJson(costMonitorRequestVo.getDpList(), false));
        }
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getPuList())) {
            costMonitor.setPus(GsonUtil.toJson(costMonitorRequestVo.getPuList(), false));
        }
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getOwnerList())) {
            costMonitor.setOwners(GsonUtil.toJson(costMonitorRequestVo.getOwnerList(), false));
        }
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getJobList())) {
            costMonitor.setJobs(GsonUtil.toJson(costMonitorRequestVo.getJobList(), false));
        }
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getNoticePersons())) {
            costMonitor.setNoticePersons(GsonUtil.toJson(costMonitorRequestVo.getNoticePersons(), false));
        }
        if (!CollectionUtils.isEmpty(costMonitorRequestVo.getFrep())) {
            costMonitor.setFrep(GsonUtil.toJson(costMonitorRequestVo.getFrep(), false));
        }
    }

    public static final String ratioNotice = "%s用量'%s'比异常告警\n'%s'等'%s'用量'%s'比增长超出阀值'%s'\n告警时间 '%s' \n 详情：'%s'";
    public static final String newJobNotice = "%s:%s\n 日期: %s 新增任务: %s \n";

    ////cost/monitor/detail?type=%s&ratio=%s&monitorLevel=%s&id=%s&shareitId=%s&dates=%s
    public static String wrapRatioNotice(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVos, CostMonitor costMonitor, String noticeUrl) {
        String ratio = costMonitor.getType() == 2 ? "同" : "环";
        String type = "";
        String name = "";
        if (costRequestVo.getCostType().equals(CostType.DP.name())) {
            type = "部门";
            name = costResponseVos.get(0).getDepartmentName();
        }
        if (costRequestVo.getCostType().equals(CostType.OWNER.name())) {
            type = "owner";
            name = costResponseVos.get(0).getOwner();
        }
        if (costRequestVo.getCostType().equals(CostType.PU.name())) {
            type = "PU";
            name = costResponseVos.get(0).getPuName();
        }
        if (costRequestVo.getCostType().equals(CostType.JOB.name())) {
            type = "任务";
            name = costResponseVos.get(0).getJobName();
        }
        String url = String.format(noticeUrl, costMonitor.getType(), costMonitor.getRatio(), costMonitor.getMonitorLevel(), costMonitor.getId(), costMonitor.getCreateShareitId(), costRequestVo.getStartDate());
        return String.format(ratioNotice, type, ratio, name, type, ratio, costMonitor.getRatio(), costRequestVo.getStartDate(), url);
    }

    //cost/monitor/detail?type=%s&ratio=%s&monitorLevel=%s&id=%s&shareitId=%s&dates=%s
    public static String wrapNewJobNotice(CostRequestVo costRequestVo, List<CostResponseVo> costJobVoList, CostMonitor costMonitor, String noticeUrl) {
        Map<String, List<CostResponseVo>> map = Maps.newHashMap();
        //String url = String.format(noticeUrl, costMonitor.getId(), costRequestVo.getStartDate());

        if (costMonitor.getMonitorLevel() == 1) {
            map = PubMethod.listToListMap("departmentName", costJobVoList);
        }
        if (costMonitor.getMonitorLevel() == 3) {
            map = PubMethod.listToListMap("owner", costJobVoList);
        }
        if (costMonitor.getMonitorLevel() == 2) {
            Map<String, List<CostResponseVo>> puMaps = Maps.newHashMap();
            map = PubMethod.listToListMap("puName", costJobVoList);
            for (Map.Entry<String, List<CostResponseVo>> entry : map.entrySet()) {
                if (entry.getKey().indexOf(",") > 0) {
                    String[] pus = entry.getKey().split(",");
                    for (String pu : pus) {
                        if (puMaps.containsKey(pu)) {
                            puMaps.get(pu).addAll(entry.getValue());
                        } else {
                            puMaps.put(pu, entry.getValue());
                        }
                    }
                } else {
                    if (puMaps.containsKey(entry.getKey())) {
                        puMaps.get(entry.getKey()).addAll(entry.getValue());
                    } else {
                        puMaps.put(entry.getKey(), entry.getValue());
                    }
                }
                map = puMaps;
            }
        }
        StringBuffer stringBuffer = new StringBuffer("当日新任务提醒\n");
        for (Map.Entry<String, List<CostResponseVo>> entry : map.entrySet()) {
            String notice = "";
            if (costMonitor.getMonitorLevel() == 1) {
                notice = String.format(newJobNotice, "部门", entry.getKey(), costRequestVo.getStartDate(), entry.getValue().size());
            }
            if (costMonitor.getMonitorLevel() == 3) {
                notice = String.format(newJobNotice, "OWNER", entry.getKey(), costRequestVo.getStartDate(), entry.getValue().size());
            }
            if (costMonitor.getMonitorLevel() == 2) {
                notice = String.format(newJobNotice, "PU", entry.getKey(), costRequestVo.getStartDate(), entry.getValue().size());
            }
            stringBuffer.append(notice);
        }
        String str = String.format(noticeUrl, costMonitor.getType(), costMonitor.getRatio(), costMonitor.getMonitorLevel(), costMonitor.getId(), costMonitor.getCreateShareitId(), costRequestVo.getStartDate());
        stringBuffer.append(" 详情 ").append(str);
        return stringBuffer.toString();
    }


    public static List<String> wrapNotice(CostMonitor costMonitor) {
        List<String> list = Lists.newArrayList();
        if (!StringUtils.isEmpty(costMonitor.getNoticeSelf())) {
            list.add(costMonitor.getNoticeSelf());
        }
        if (!StringUtils.isEmpty(costMonitor.getNoticePersons())) {
            List<String> strings = GsonUtil.parseFromJson(costMonitor.getNoticePersons(), new TypeToken<List<String>>() {
            }.getType());
            list.addAll(strings);
        }
        return list;
    }

    public static void sortCostResponse(List<CostResponseVo> costResponseVos, CostRequestVo costRequestVo) {
        if (!StringUtils.isEmpty(costRequestVo.getSort()) && !StringUtils.isEmpty(costRequestVo.getOrder())) {
            Collections.sort(costResponseVos, new Comparator<CostResponseVo>() {
                @Override
                public int compare(CostResponseVo o1, CostResponseVo o2) {
                    if (costRequestVo.getOrder().equals("jobNum")) {
                        o1.setJobNum(o1.getJobNum() == null ? 0 : o1.getJobNum());
                        o2.setJobNum(o2.getJobNum() == null ? 0 : o2.getJobNum());
                        return costRequestVo.getSort().equals("ascending") ? o1.getJobNum().compareTo(o2.getJobNum()) : o2.getJobNum().compareTo(o1.getJobNum());
                    } else if (costRequestVo.getOrder().equals("jobNameQuantity") || costRequestVo.getOrder().equals("cost")) {
                        o1.setJobNameQuantity(o1.getJobNameQuantity() == null ? 0 : o1.getJobNameQuantity());
                        o2.setJobNameQuantity(o2.getJobNameQuantity() == null ? 0 : o2.getJobNameQuantity());
                        return costRequestVo.getSort().equals("ascending") ? o1.getJobNameQuantity().compareTo(o2.getJobNameQuantity()) : o2.getJobNameQuantity().compareTo(o1.getJobNameQuantity());
                    } else if (costRequestVo.getOrder().equals("relativeRatio")) {
                        return costRequestVo.getSort().equals("ascending") ? PubMethod.percentToDouble(o1.getRelativeRatio()).compareTo(PubMethod.percentToDouble(o2.getRelativeRatio())) : PubMethod.percentToDouble(o2.getRelativeRatio()).compareTo(PubMethod.percentToDouble(o1.getRelativeRatio()));
                    } else if (costRequestVo.getOrder().equals("basisRatio")) {
                        return costRequestVo.getSort().equals("ascending") ? PubMethod.percentToDouble(o1.getBasisRatio()).compareTo(PubMethod.percentToDouble(o2.getBasisRatio())) : PubMethod.percentToDouble(o2.getBasisRatio()).compareTo(PubMethod.percentToDouble(o1.getBasisRatio()));
                    } else if (costRequestVo.getOrder().equals("dt")) {
                        return costRequestVo.getSort().equals("ascending") ? o1.getDt().compareTo(o2.getDt()) : o2.getDt().compareTo(o1.getDt());
                    } else if (costRequestVo.getOrder().equals("jobCreateTime")) {
                        if (StringUtils.isEmpty(o1.getJobCreateTime())) {
                            o1.setJobCreateTime("");
                        }
                        if (StringUtils.isEmpty(o2.getJobCreateTime())) {
                            o2.setJobCreateTime("");
                        }
                        return costRequestVo.getSort().equals("ascending") ? o1.getJobCreateTime().compareTo(o2.getJobCreateTime()) : o2.getJobCreateTime().compareTo(o1.getJobCreateTime());
                    } else if (costRequestVo.getOrder().equals("cumulativeCost7") || costRequestVo.getOrder().equals("cumulativejobNameQuantity7")) {
                        o1.setCumulativeCost7(o1.getCumulativeCost7() == null ? 0 : o1.getCumulativeCost7());
                        o2.setCumulativeCost7(o2.getCumulativeCost7() == null ? 0 : o2.getCumulativeCost7());
                        return costRequestVo.getSort().equals("ascending") ? o1.getCumulativeCost7().compareTo(o2.getCumulativeCost7()) : o2.getCumulativeCost7().compareTo(o1.getCumulativeCost7());
                    } else if (costRequestVo.getOrder().equals("cumulativeCost30") || costRequestVo.getOrder().equals("cumulativejobNameQuantity30")) {
                        o1.setCumulativeCost30(o1.getCumulativeCost30() == null ? 0 : o1.getCumulativeCost30());
                        o2.setCumulativeCost30(o2.getCumulativeCost30() == null ? 0 : o2.getCumulativeCost30());
                        return costRequestVo.getSort().equals("ascending") ? o1.getCumulativeCost30().compareTo(o2.getCumulativeCost30()) : o2.getCumulativeCost30().compareTo(o1.getCumulativeCost30());
                    } else if (costRequestVo.getOrder().equals("dailyIncrementCost") || costRequestVo.getOrder().equals("dailyIncrementJobNameQuantity")) {
                        o1.setDailyIncrementCost(o1.getDailyIncrementCost() == null ? 0 : o1.getDailyIncrementCost());
                        o2.setDailyIncrementCost(o2.getDailyIncrementCost() == null ? 0 : o2.getDailyIncrementCost());
                        return costRequestVo.getSort().equals("ascending") ? o1.getDailyIncrementCost().compareTo(o2.getDailyIncrementCost()) : o2.getDailyIncrementCost().compareTo(o1.getDailyIncrementCost());
                    } else if (costRequestVo.getOrder().equals("cumulativeCost")) {
                        o1.setCumulativeCost(o1.getCumulativeCost() == null ? 0 : o1.getCumulativeCost());
                        o2.setCumulativeCost(o2.getCumulativeCost() == null ? 0 : o2.getCumulativeCost());
                        return costRequestVo.getSort().equals("ascending") ? o1.getCumulativeCost().compareTo(o2.getCumulativeCost()) : o2.getCumulativeCost().compareTo(o1.getCumulativeCost());
                    }
                    return 0;
                }
            });
        }
    }

    public static CostTotalVo wrapJobTotal(List<CostResponseVo> costResponseVoList, CostRequestVo costRequestVo) {
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            Set<String> jobNameSet = Sets.newHashSet();
            Set<String> ownerSet = Sets.newHashSet();
            String maxDate = "";
            String minDate = "";
            Double totalJobNameQuantity = 0d;//任务总用量
            Double totalCost = 0d;//任务总用量
            Set<String> dt = Sets.newHashSet();
            for (CostResponseVo costResponseVo : costResponseVoList) {
                jobNameSet.add(costResponseVo.getJobName());
                ownerSet.add(costResponseVo.getOwner());
                if (StringUtils.isEmpty(maxDate) || costResponseVo.getDt().compareTo(maxDate) > 0) {
                    maxDate = costResponseVo.getDt();
                }
                if (StringUtils.isEmpty(minDate) || costResponseVo.getDt().compareTo(minDate) < 0) {
                    minDate = costResponseVo.getDt();
                }
                totalJobNameQuantity += costResponseVo.getJobNameQuantityBefore();
                totalCost += costResponseVo.getCostBefore();
                dt.add(costResponseVo.getDt());
            }
            int intervalDays = DateTimeUtils.getIntervalDays(DateTimeUtils.getStartOfDay(minDate), DateTimeUtils.getEndOfDay(maxDate)) + 1;
            return CostTotalVo.builder().dataCycle(dt.size()).totalCost(PubMethod.doubleScale2(totalCost))
                    .totalJobNameQuantity(PubMethod.doubleScale2(totalJobNameQuantity))
                    .totalJob(jobNameSet.size())
                    .totalOwner(ownerSet.size())
                    .averageDayCost(PubMethod.doubleScale2(totalCost / jobNameSet.size() / intervalDays))
                    .averageDayJobNameQuantity(PubMethod.doubleScale2(totalJobNameQuantity / jobNameSet.size() / intervalDays))
                    .build();
        }
        return null;
    }


    public static void wrapJobCost(List<CostResponseVo> costResponseVoListMonth, List<CostResponseVo> costResponseVoListWeek, Map<String, CostResponseVo> jobMap) {
        Map<String, List<CostResponseVo>> monthMap = PubMethod.listToListMap("jobName", costResponseVoListMonth);
        Map<String, List<CostResponseVo>> weekMap = PubMethod.listToListMap("jobName", costResponseVoListWeek);
        for (Map.Entry<String, List<CostResponseVo>> entry : monthMap.entrySet()) {
            if (jobMap.containsKey(entry.getKey())) {
                CostResponseVo costResponseVo = jobMap.get(entry.getKey());
                for (CostResponseVo c : entry.getValue()) {
                    costResponseVo.addCost(c.getCost());
                    costResponseVo.addjobNameQuantity(c.getJobNameQuantity());
                }
                costResponseVo.setCumulativeCost30(costResponseVo.getCost());
                costResponseVo.setCumulativejobNameQuantity30(costResponseVo.getJobNameQuantity());
                costResponseVo.setCost(null);
                costResponseVo.setJobNameQuantity(null);
            }
        }
        for (Map.Entry<String, List<CostResponseVo>> entry : weekMap.entrySet()) {
            if (jobMap.containsKey(entry.getKey())) {
                CostResponseVo costResponseVo = jobMap.get(entry.getKey());
                for (CostResponseVo c : entry.getValue()) {
                    costResponseVo.addCost(c.getCost());
                    costResponseVo.addjobNameQuantity(c.getJobNameQuantity());
                }
                costResponseVo.setCumulativeCost7(costResponseVo.getCost());
                costResponseVo.setCumulativejobNameQuantity7(costResponseVo.getJobNameQuantity());
                costResponseVo.setCost(null);
                costResponseVo.setJobNameQuantity(null);
            }
        }
    }

    public static void wrapPuAndDp(List<CostResponseVo> costResponseVoList) {
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            Map<String, List<CostResponseVo>> map = PubMethod.listToListMap("departmentName", costResponseVoList);
            for (Map.Entry<String, List<CostResponseVo>> entry : map.entrySet()) {
                CostResponseVo costResponseVo = new CostResponseVo();
                for (CostResponseVo c : entry.getValue()) {
                    costResponseVo.addCost(c.getCost());
                    costResponseVo.addjobNameQuantity(c.getJobNameQuantity());
                }
                for (CostResponseVo c : entry.getValue()) {
                    c.setTotalCostDp(costResponseVo.getCost());
                    c.setTotaljobNameQuantityDp(costResponseVo.getJobNameQuantity());
                }
            }

            Map<String, List<CostResponseVo>> mapDt = PubMethod.listToListMap("dt", costResponseVoList);
            for (Map.Entry<String, List<CostResponseVo>> entry : mapDt.entrySet()) {
                CostResponseVo costResponseVo = new CostResponseVo();
                for (CostResponseVo c : entry.getValue()) {
                    costResponseVo.addCost(c.getCost());
                    costResponseVo.addjobNameQuantity(c.getJobNameQuantity());
                }
                for (CostResponseVo c : entry.getValue()) {
                    c.setTotalCost(costResponseVo.getCost());
                    c.setTotaljobNameQuantity(costResponseVo.getJobNameQuantity());
                }
            }
        }
    }


    public static void excelJob(CostRequestVo costRequestVo, List<CostResponseVo> costResponseVoList, CostTotalVo costTotalVo, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        ExcelSheetVO excelSheetVOTotal = new ExcelSheetVO();
        excelSheetVOTotal.setDatas(buildTotalData(costTotalVo,costRequestVo));
        excelSheetVOTotal.setName("总览");

        ExcelSheetVO excelSheetVO = new ExcelSheetVO();
        excelSheetVO.setDatas(buildJobData(costResponseVoList,costRequestVo));
        excelSheetVO.setName("详单");
        //生成并输出excel
        List<ExcelSheetVO> excelSheetVOS = com.google.common.collect.Lists.newArrayList(excelSheetVOTotal,excelSheetVO);
        FileRenderUtil.renderExcel(excelSheetVOS, "report.xls", httpServletRequest, httpServletResponse);
    }

    private static List<List<String>> buildTotalData(CostTotalVo costTotalVo,CostRequestVo costRequestVo) {
        List<List<String>> listList = Lists.newArrayList();
        if (costRequestVo.getModel()!=null&&costRequestVo.getModel()==2){
            listList.add(Lists.newArrayList("任务总数", "owner总数", "数据周期", "任务总成本", "任务平均日成本"));
            if (costTotalVo != null) {
                listList.add(Lists.newArrayList(costTotalVo.getTotalJob() + "", costTotalVo.getTotalOwner() + "",
                        costTotalVo.getDataCycle() + "", costTotalVo.getTotalCost() + "", costTotalVo.getAverageDayCost() + ""));
            }
        }else {
            listList.add(Lists.newArrayList("任务总数", "owner总数", "数据周期", "任务总量", "任务平均日用量"));
            if (costTotalVo != null) {
                listList.add(Lists.newArrayList(costTotalVo.getTotalJob() + "", costTotalVo.getTotalOwner() + "",
                        costTotalVo.getDataCycle() + "", costTotalVo.getTotalJobNameQuantity() + "", costTotalVo.getAverageDayJobNameQuantity() + ""));
            }
        }
        return listList;
    }

    private static List<List<String>> buildJobData(List<CostResponseVo> costResponseVoList,CostRequestVo costRequestVo) {
        List<List<String>> listList = Lists.newArrayList();
        if (costRequestVo.getModel()!=null&&costRequestVo.getModel()==2){
            listList.add(Lists.newArrayList("任务", "owner", "任务创建时间", "账单日期", "任务成本", "成本日增量", "成本周同比", "成本日环比"));
        }else {
            listList.add(Lists.newArrayList("任务", "owner", "任务创建时间", "账单日期", "任务用量", "用量日增量", "用量周同比", "用量日环比"));
        }
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            for (CostResponseVo costResponseVo : costResponseVoList) {
                if (costRequestVo.getModel()!=null&&costRequestVo.getModel()==2){
                    listList.add(Lists.newArrayList(costResponseVo.getJobName(), costResponseVo.getOwner(), costResponseVo.getJobCreateTime()
                            , costResponseVo.getDt(), costResponseVo.getCost()+"", costResponseVo.getDailyIncrementCost()+"",
                            costResponseVo.getBasisRatio(), costResponseVo.getRelativeRatio()
                    ));
                }else {
                    listList.add(Lists.newArrayList(costResponseVo.getJobName(), costResponseVo.getOwner(), costResponseVo.getJobCreateTime()
                            , costResponseVo.getDt(), costResponseVo.getJobNameQuantity()+"", costResponseVo.getDailyIncrementJobNameQuantity()+"",
                            costResponseVo.getBasisRatio(), costResponseVo.getRelativeRatio()
                    ));
                }

            }
        }
        return listList;
    }
}
