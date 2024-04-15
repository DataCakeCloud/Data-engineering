package com.ushareit.dstask.bean;


import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import scala.math.BigInt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author xuebotao
 * @date 2023-02-17
 */
@Data
@Entity
@Builder
@Table(name = "spark_param_restrict")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("任务类")
public class SparkParamRestrict extends DeleteEntity {

    @Column(name = "spark_version")
    public String sparkVersion;

    @Column(name = "prefix")
    public String prefix;

    @Column(name = "name")
    public String name;

    @Column(name = "type")
    public String type;

    @Column(name = "is_value_check")
    public String isValueCheck;

    @Column(name = "param_strategy")
    public String paramStrategy;
    //{"units":[],"valueRange":{"min":"1","max":"2147483647"},"isIgnoreCase":"false","fixedValue":[]}


}
