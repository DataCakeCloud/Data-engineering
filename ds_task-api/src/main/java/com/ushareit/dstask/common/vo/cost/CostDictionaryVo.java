package com.ushareit.dstask.common.vo.cost;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class CostDictionaryVo {
    private int type;//1部门 2PU 3owner 4任务  5 区域 6 产品 7 时间段
    private String name;//

    private String startDate;

    private String endDate;

    private String shareitId;

    private Integer id;
    private List<String> dps;
    private List<String> regions;
    private List<String> products;
    private List<String> owners;



    private List<String> departments;//部门

    public List<String> getDps() {
        return getDepartments();
    }

    public List<String> getDepartments() {
        return CollectionUtils.isEmpty(departments)?null:departments;
    }

    public List<String> getOwners() {
        return CollectionUtils.isEmpty(owners)?null:owners;
    }

    public List<String> getProducts() {
        return CollectionUtils.isEmpty(products)?null:products;
    }

    public List<String> getRegions() {
        return CollectionUtils.isEmpty(regions)?null:regions;
    }


}
