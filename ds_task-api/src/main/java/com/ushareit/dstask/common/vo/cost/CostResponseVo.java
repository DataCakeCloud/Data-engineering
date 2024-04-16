package com.ushareit.dstask.common.vo.cost;

import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.CostType;
import com.ushareit.dstask.web.utils.PubMethod;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Data
public class CostResponseVo {
    private String departmentName;//部门名称
    private String puName;//
    private String jobId;
    private String jobName;
    private String owner;
    private String productName;
    private String region;
    private Integer jobNum; //任务数
    private Integer jobNumRelative; //任务数
    private Integer jobNumBasis; //同比任务数
    private Double jobNameQuantity;//用量
    private Double cost;//成本
    private Double cumulativeCost;//累计成本
    private String relativeRatio;//环比 是昨天
    private String basisRatio;//同比 是周

    private String jobRelativeRatio;//环比 是昨天
    private String jobBasisRatio;//同比 是周
    private String dt;
    private String statName;
    private transient boolean add;
    private String proportion;//占比

    private Double cumulativeCost7;//累计7天成本

    private Double cumulativeCost30;//累计30天成本

    private Double cumulativejobNameQuantity7;//累计7天用量

    private Double cumulativejobNameQuantity30;//累计30天用量


    private Double totalCost;//

    private Double totaljobNameQuantity;

    private Double totalCostDp;//

    private Double totaljobNameQuantityDp;

    private String jobCreateTime;
    private Double dailyIncrementJobNameQuantity;
    private Double dailyIncrementCost;
    private CostTotalVo costTotalVo;

    public void addCost(Double cost) {
        add = true;
        if (cost == null) {
            cost = 0d;
        }
        if (this.cost == null) {
            this.cost = 0d;
        }
        this.cost = (this.cost + cost);
    }

    public void addjobNameQuantity(Double jobNameQuantity) {
        if (jobNameQuantity == null) {
            jobNameQuantity = 0d;
        }
        if (this.jobNameQuantity == null) {
            this.jobNameQuantity = 0d;
        }
        this.jobNameQuantity = (this.jobNameQuantity + jobNameQuantity);
    }

    public void wrapRelativeRatio(CostResponseVo other) {
        dailyIncrementJobNameQuantity = PubMethod.subtract(this, other);
        dailyIncrementCost = dailyIncrementJobNameQuantity * 0.12672;
        if (this.jobNameQuantity == null || this.jobNameQuantity.doubleValue() == 0) {
            this.jobNameQuantity = BaseConstant.LITTLE;
        }
        if (other == null) {
            other = new CostResponseVo();
            other.setJobNameQuantity(BaseConstant.LITTLE);
        }
        if (other.jobNameQuantity == null || other.jobNameQuantity.doubleValue() == 0) {
            other.jobNameQuantity = BaseConstant.LITTLE;
        }
        relativeRatio = new BigDecimal(this.jobNameQuantity - other.jobNameQuantity).divide(new BigDecimal(other.jobNameQuantity), 4, RoundingMode.HALF_UP).multiply(BaseConstant.HUNDRED).setScale(2, RoundingMode.HALF_UP).toString() + "%";

        if (this.jobNum == null || this.jobNum == 0 || other == null || other.jobNum == null || other.jobNum == 0) {
            jobRelativeRatio = BaseConstant.NODATA;
        } else {
            jobRelativeRatio = new BigDecimal(this.jobNum - other.jobNum).divide(new BigDecimal(other.jobNum), 4, RoundingMode.HALF_UP).multiply(BaseConstant.HUNDRED).setScale(2, RoundingMode.HALF_UP).toString() + "%";
        }
        if (other == null) {
            this.jobNumRelative = 0;
        } else {
            this.jobNumRelative = other.getJobNum();
        }
    }

    public void wrapBasisRatio(CostResponseVo other) {
        if (this.jobNameQuantity == null || this.jobNameQuantity.doubleValue() == 0) {
            this.jobNameQuantity = BaseConstant.LITTLE;
        }
        if (other == null) {
            other = new CostResponseVo();
            other.setJobNameQuantity(BaseConstant.LITTLE);
        }
        if (other.jobNameQuantity == null || other.jobNameQuantity.doubleValue() == 0) {
            other.jobNameQuantity = BaseConstant.LITTLE;
        }
        basisRatio = new BigDecimal(this.jobNameQuantity - other.jobNameQuantity).divide(new BigDecimal(other.jobNameQuantity), 4, RoundingMode.HALF_UP).multiply(BaseConstant.HUNDRED).setScale(2, RoundingMode.HALF_UP).toString() + "%";
        if (this.jobNum == null || this.jobNum == 0 || other == null || other.jobNum == null || other.jobNum == 0) {
            jobBasisRatio = BaseConstant.NODATA;
        } else {
            jobBasisRatio = new BigDecimal(this.jobNum - other.jobNum).divide(new BigDecimal(other.jobNum), 4, RoundingMode.HALF_UP).multiply(BaseConstant.HUNDRED).setScale(2, RoundingMode.HALF_UP).toString() + "%";
        }
        if (other == null) {
            this.jobNumBasis = 0;
        } else {
            this.jobNumBasis = other.getJobNum();
        }
    }

    public String key(CostRequestVo costRequestVo) {
        if (costRequestVo.isPuAndDp()) {
            return puName + departmentName;
        }
        if (costRequestVo.getCostType().equals(CostType.JOB.name())) {
            return jobName;
        }
        if (costRequestVo.getCostType().equals(CostType.OWNER.name())) {
            return owner;
        }
        if (costRequestVo.getCostType().equals(CostType.DP.name())) {
            return departmentName;
        }
        if (costRequestVo.getCostType().equals(CostType.PU.name())) {
            return puName;
        }
        if (costRequestVo.getCostType().equals(CostType.PRODUCT.name())) {
            return productName;
        }
        return null;
    }

    public Double getJobNameQuantity() {
        if (jobNameQuantity != null) {
            return PubMethod.doubleScale2(jobNameQuantity);
        }
        return jobNameQuantity;
    }

    public Double getCost() {
        if (cost != null) {
            return PubMethod.doubleScale2(cost);
        }
        return cost;
    }

    public Double getDailyIncrementCost() {
        return PubMethod.doubleScale2(dailyIncrementCost);
    }

    public Double getDailyIncrementJobNameQuantity() {
        return PubMethod.doubleScale2(dailyIncrementJobNameQuantity);
    }

    public Double getCumulativeCost7() {
        return PubMethod.doubleScale2(cumulativeCost7);
    }

    public Double getCumulativeCost30() {
        return PubMethod.doubleScale2(cumulativeCost30);
    }

    public Double getTotalCost() {
        return PubMethod.doubleScale2(totalCost);
    }

    public Double getTotalCostDp() {
        return PubMethod.doubleScale2(totalCostDp);
    }

    public Double getTotaljobNameQuantity() {
        return PubMethod.doubleScale2(totaljobNameQuantity);
    }

    public Double getTotaljobNameQuantityDp() {
        return PubMethod.doubleScale2(totaljobNameQuantityDp);
    }

    public Double getCumulativejobNameQuantity7() {
        return PubMethod.doubleScale2(cumulativejobNameQuantity7);
    }

    public Double getCumulativejobNameQuantity30() {
        return PubMethod.doubleScale2(cumulativejobNameQuantity30);
    }

    public void setJobNameQuantity(Double jobNameQuantity) {
        this.jobNameQuantity = jobNameQuantity;
        this.cost = jobNameQuantity == null ? 0 : jobNameQuantity * 0.12672;
    }

    public Double getCumulativeCost() {
        if (cumulativeCost != null) {
            return PubMethod.doubleScale2(cumulativeCost);
        }
        return cumulativeCost;
    }


    public Double getJobNameQuantityBefore() {
        if (jobNameQuantity == null) {
            return 0d;
        }
        return jobNameQuantity;
    }

    public Double getCostBefore() {
        if (cost == null) {
            return 0d;
        }
        return cost;
    }
}
