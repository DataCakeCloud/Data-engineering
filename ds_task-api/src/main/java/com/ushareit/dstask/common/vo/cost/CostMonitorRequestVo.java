package com.ushareit.dstask.common.vo.cost;

import lombok.Data;

import java.util.List;

@Data
public class CostMonitorRequestVo {
    private Integer id;

    private String name;//监控名称

    private int type;//监控类型 1 新任务 2：同比分析  3： 环比分析

    private int monitorLevel;//监控维度 1部门 2PU 3owner 4任务

    private int trialRange;//试用范围 1所有维度  2自定义

    private String ratio;//新任务用量 环比 同比的比例

    private String noticeType;//1 钉钉  2 邮件

    private List<String> noticePersons;//通知范围人

    private String noticeSelf;//通知自己

    private List<String> dpList;//部门列表


    private List<String> puList;//pu列表

    private List<String> ownerList;//个人列表

    private List<String> jobList;//个人列表

    private List<String> frep;//通知频率 0每天  1  2  3  4  5  6  7

    private String createShareitId;


}
