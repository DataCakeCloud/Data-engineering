package com.ushareit.dstask.service;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableMap;
import com.ushareit.dstask.bean.qe.QueryTop;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/9/2
 */
public interface HomeService {
    void monitorCache();

    /**
     * 查询任务统计
     * @param recent
     * @return
     */
    Map<String, Object> queryTaskStatics(Integer recent,String userGroup);

    /**
     * top排行-扫描
     * @return
     */
    List<QueryTop> queryScanTop(@RequestParam("userGroup") String userGroup);

    /**
     * top排行-执行时长
     * @return
     */
    List<QueryTop> queryExecutionTop();

    /**
     * 综合评分
     * @return
     */
    Map<String, Object> overallScore();

    Map<String, Object> metaTaskStatics(Integer recent,String userGroup);

    List<ImmutableMap<String, String>> keyIndex();

    List<ImmutableMap<String, Object>> dataResource();

    JSONArray metaTop(Integer type);
}
