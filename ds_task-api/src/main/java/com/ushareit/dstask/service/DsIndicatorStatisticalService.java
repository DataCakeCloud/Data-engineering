package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.DsIndicatorStatistical;


/**
 * @author: xuebotao
 * @create: 2021-12-03
 */
public interface DsIndicatorStatisticalService extends BaseService<DsIndicatorStatistical> {


    /**
     * 统计DS相关指标并插入
     */
    void insertIndicators();

    void insertIndicatorsInternal();

    DsIndicatorStatistical getDataByDtAndName(String dt, String name);
}
