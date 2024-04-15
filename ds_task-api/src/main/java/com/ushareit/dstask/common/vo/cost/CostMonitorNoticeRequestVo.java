package com.ushareit.dstask.common.vo.cost;

import lombok.Data;

import java.util.List;

@Data
public class CostMonitorNoticeRequestVo {

    private String shareitId;

    private Integer id;//监控任务id

    private List<String> dates;

    private List<String> jobNames;

    private List<String> owners;//拥有者

    private List<String> departments;//部门

    private List<String> pus;

    private List<String> products;//产品

    private List<String> regions;//地区

    private Integer pageNum;

    private Integer pageSize;
}
