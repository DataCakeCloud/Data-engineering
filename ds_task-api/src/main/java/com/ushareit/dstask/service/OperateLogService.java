package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.OperateLog;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
public interface OperateLogService extends BaseService<OperateLog> {

    /**
     * 获取指标，一天故障数、接口平均响应时长
     */
    Map<String, Integer> getIndicators();

    /**
     * 每天用户数，不包含查询当天
     * @return
     */
    List<Map<String, Integer>> getDayUsers();

    /**
     * 周用户数
     * @param start
     * @param end
     * @return
     */
    Map<String, Integer> getWeekUsers(Timestamp start, Timestamp end);

    /**
     * 获取累计用户数
     * @return
     */
    Map<String, Integer> getCumulativeUsers();

}
