package com.ushareit.dstask.common.vo.cost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostRequestVo {

    private String startDate;

    private String endDate;

    private List<String> dates;//选择时间数组

    private List<String> jobNames;

    private List<String> owners;//拥有者

    private List<String> departments;//部门

    private List<String> pus;

    private List<String> products;//产品

    private List<String> regions;//地区

    private String shareitId;

    private String costType;//DP,PU,OWNER,JOB,PRODUCT

    //private int queryType;//查询类型是 0  用量 1 成本

    private List<String> costQueryPus;//成本账单查下查询pu

    private Integer groupby;//0或者空 按时间  1按部门  2按pu

    private boolean needCumulativeCost;//是否需要累计成本


    private String groupName;

    private Integer pageSize;

    private Integer pageNum;

    private String order;

    private String sort;

    private boolean puAndDp;//pu和dp一起查询

    private boolean both;//是否两个合并

    private Integer labelId;

    private boolean excel;

    private transient String bakStartDate;

    private transient String bakEndDate;
    private transient List<String> bakDates;

    private Integer model;//0 用量  1 成本

    public void wrapDate(String date){
        this.startDate=this.endDate=date;

    }

    public List<String> getDepartments() {
        return CollectionUtils.isEmpty(departments)?null:departments;
    }

    public List<String> getJobNames() {
        return CollectionUtils.isEmpty(jobNames)?null:jobNames;
    }

    public List<String> getOwners() {
        return CollectionUtils.isEmpty(owners)?null:owners;
    }

    public List<String> getProducts() {
        return CollectionUtils.isEmpty(products)?null:products;
    }

    public List<String> getPus() {
        return CollectionUtils.isEmpty(pus)?null:pus;
    }

    public List<String> getRegions() {
        return CollectionUtils.isEmpty(regions)?null:regions;
    }

    public List<String> getCostQueryPus() {
        return CollectionUtils.isEmpty(costQueryPus)?null:costQueryPus;
    }

    public List<String> getDates() {
        return CollectionUtils.isEmpty(dates)?null:dates;
    }

    public void bak(){
        this.bakStartDate=startDate;
        this.bakEndDate=endDate;
        this.bakDates=dates;
    }

    public void unBak(){
        this.startDate=bakStartDate;
        this.endDate=bakEndDate;
        this.dates=bakDates;
    }
}
