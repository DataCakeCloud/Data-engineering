package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.CostMonitor;
import com.ushareit.dstask.bean.CostMonitorNotice;
import com.ushareit.dstask.common.vo.cost.*;

import java.util.List;

public interface CostService {
    List<CostResponseVo> selectCost(CostRequestVo costRequestVo);

    void saveAndEditCostMonitor(CostMonitorRequestVo costMonitorRequestVo);

    void deleteCostMonitor(Integer id);

    List<CostMonitorRequestVo> list(String createShareitId);

    CostMonitorRequestVo getById(Integer id);

    void startCostMonitorJob() throws InterruptedException;

    List<String> listDictionary(CostDictionaryVo costDictionaryVo);

    List<CostResponseVo> selectNewJob(CostRequestVo costRequestVo, CostMonitor costMonitor);

    List<CostResponseVo> selectRatioJob(CostRequestVo costRequestVo, CostMonitor costMonitor);

    void initSearch();
    List<CostResponseVo> monitorJob(CostMonitorNoticeRequestVo costMonitorNoticeRequestVo);

    void selectJobOwner(List<CostResponseVo> costResponseVos,CostRequestVo costRequestVo);

    List<CostMonitorNotice> jobNoticeList(CostMonitorNoticeRequestVo costMonitorNoticeRequestVo);
}
