package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;

import com.google.common.collect.Maps;
import com.ushareit.dstask.bean.CostMonitorNotice;
import com.ushareit.dstask.bean.UserBase;
import com.ushareit.dstask.common.vo.cost.*;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.CostType;
import com.ushareit.dstask.service.CostService;
import com.ushareit.dstask.utils.GsonUtil;
import com.ushareit.dstask.web.utils.CostUtils;
import com.ushareit.dstask.web.utils.DateUtil;
import com.ushareit.dstask.web.utils.ItUtil;
import com.ushareit.dstask.web.utils.PubMethod;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

//@RestController
//@RequestMapping("/cost")
public class CostController {
    //@Autowired
    private CostService costService;

    @PostMapping("/stat")
    public BaseResponse stat(@RequestBody CostRequestVo costRequestVo, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (costRequestVo.isBoth()) {
            Map<String, List<CostResponseVo>> map = Maps.newHashMap();
            costRequestVo.setCostType(CostType.DP.name());
            List<CostResponseVo> costResponseVoListDp = costService.selectCost(costRequestVo);
            map.put(costRequestVo.getCostType(), costResponseVoListDp);
            costRequestVo.setCostType(CostType.PU.name());
            List<CostResponseVo> costResponseVoListPu = costService.selectCost(costRequestVo);
            map.put(costRequestVo.getCostType(), costResponseVoListPu);
            return BaseResponse.success(map);
        }
        List<CostResponseVo> costResponseVoList = costService.selectCost(costRequestVo);
        /*if (!StringUtils.isEmpty(costRequestVo.getGroupName())&& !CollectionUtils.isEmpty(costResponseVoList)){
            return BaseResponse.success(PubMethod.convertCostMap("statName",costResponseVoList));
        }*/
        if (!CollectionUtils.isEmpty(costResponseVoList)) {
            CostUtils.sortCostResponse(costResponseVoList, costRequestVo);
            CostTotalVo costTotalVo = null;
            if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
                costTotalVo = CostUtils.wrapJobTotal(costResponseVoList, costRequestVo);
                if (costTotalVo != null) {
                    costResponseVoList.get(0).setCostTotalVo(costTotalVo);
                }
            }
            if (costRequestVo.getPageNum() != null && costRequestVo.getPageSize() != null) {
                PageInfo<CostResponseVo> pageInfo = PubMethod.getPageInfo(costRequestVo.getPageNum(), costRequestVo.getPageSize(), costResponseVoList);
                if (costTotalVo != null) {
                    pageInfo.getList().get(0).setCostTotalVo(costTotalVo);
                }
                return BaseResponse.success(pageInfo);
            }
        }
        return BaseResponse.success(costResponseVoList);
    }
    @GetMapping("/excel")
    public void excel(CostRequestVo costRequestVo, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        List<CostResponseVo> costResponseVoList = costService.selectCost(costRequestVo);
        CostTotalVo costTotalVo = null;
        if (CostType.JOB.name().equals(costRequestVo.getCostType())) {
            costTotalVo = CostUtils.wrapJobTotal(costResponseVoList, costRequestVo);
            if (costTotalVo != null) {
                costResponseVoList.get(0).setCostTotalVo(costTotalVo);
            }
        }
        CostUtils.excelJob(costRequestVo,costResponseVoList,costTotalVo,httpServletRequest,httpServletResponse);
    }

    @RequestMapping("/job/list")
    public BaseResponse list(String shareitId) {
        return BaseResponse.success(costService.list(shareitId));
    }

    @PostMapping("/job/saveEdit")
    public BaseResponse job(@RequestBody CostMonitorRequestVo costMonitorRequestVo) {
        costService.saveAndEditCostMonitor(costMonitorRequestVo);
        return BaseResponse.success();
    }

    @RequestMapping("/job/delete")
    public BaseResponse jobDelete(Integer id) {
        costService.deleteCostMonitor(id);
        return BaseResponse.success();
    }

    @RequestMapping("/job/noticeList")
    public BaseResponse jobnoticeList(@RequestBody CostMonitorNoticeRequestVo costMonitorNoticeRequestVo) {
        return BaseResponse.success(PubMethod.getPageInfo(costMonitorNoticeRequestVo.getPageNum(), costMonitorNoticeRequestVo.getPageSize(), costService.monitorJob(costMonitorNoticeRequestVo)));
    }


    @PostMapping("/listDictionary")
    public BaseResponse listDictionary(@RequestBody CostDictionaryVo costDictionaryVo) {
        return BaseResponse.success(costService.listDictionary(costDictionaryVo));
    }

    @PostMapping("/startjob")
    public BaseResponse startjob() {
        try {
            costService.startCostMonitorJob();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        return BaseResponse.success();
    }

    @GetMapping("/job/noticeDetail")
    public BaseResponse monitorjob(Integer id, String date) {
        CostMonitorNoticeRequestVo costMonitorNoticeRequestVo = new CostMonitorNoticeRequestVo();
        costMonitorNoticeRequestVo.setId(id);
        costMonitorNoticeRequestVo.setDates(Lists.newArrayList(date));
        costMonitorNoticeRequestVo.setPageNum(1);
        costMonitorNoticeRequestVo.setPageSize(20);
        return BaseResponse.success(PubMethod.getPageInfo(costMonitorNoticeRequestVo.getPageNum(), costMonitorNoticeRequestVo.getPageSize(), costService.monitorJob(costMonitorNoticeRequestVo)));
    }


    public static void main(String[] args) {
        System.out.println(DateUtil.firstDayOfQuarter());
        System.out.println(GsonUtil.toJson(BaseResponse.success(Lists.newArrayList("1")), false));
        CostRequestVo costRequestVo = new CostRequestVo();
        costRequestVo.setStartDate("2022-06-15");
        costRequestVo.setEndDate("2022-06-15");
        costRequestVo.setCostType("DP");
        costRequestVo.setJobNames(Lists.newArrayList("a"));
        costRequestVo.setOwners(Lists.newArrayList("b"));
        costRequestVo.setShareitId("hanzenggui");
        System.out.println(GsonUtil.toJson(costRequestVo, false));
    /*    CostMonitorRequestVo costMonitorRequestVo=new CostMonitorRequestVo();
        costMonitorRequestVo.setId(1L
        costMonitorRequestVo.setName("监控1");
        costMonitorRequestVo.setMonitorLevel("1,2");
        costMonitorRequestVo.setTrialRange(2);
        costMonitorRequestVo.setRatio("0.1");
        costMonitorRequestVo.setNoticeType("1,2");
        costMonitorRequestVo.setNoticePersons(Lists.newArrayList("zs","ls"));
        costMonitorRequestVo.setNoticeSelf("ww");
        costMonitorRequestVo.setDpList(Lists.newArrayList("zs","ls"));
        costMonitorRequestVo.setJobList(Lists.newArrayList("zs","ls"));
        costMonitorRequestVo.setPuList(Lists.newArrayList("zs","ls"));
        costMonitorRequestVo.setOwnerList(Lists.newArrayList("zs","ls"));
        costMonitorRequestVo.setCreateShareitId("zs");
        System.out.println(GsonUtil.toJson(costMonitorRequestVo,false));*/
       /* CostMonitorNoticeEntity costMonitorNoticeEntity=new CostMonitorNoticeEntity();
        costMonitorNoticeEntity
                .setContent("告警通知");
        costMonitorNoticeEntity.setCostMonitorId(1L);
        costMonitorNoticeEntity.setName("任务1");
        costMonitorNoticeEntity.setNoticeTime("2022-06-17 11:11:11");
        Response s=Response.success(Lists.newArrayList(costMonitorNoticeEntity));
        System.out.println(GsonUtil.toJson(s,false));
        System.out.println(JSON.toJSONString(new CostRequestVo(),false));
        System.out.println(JSON.toJSONString(new CostRequestVo(),true));*/
    }
}
