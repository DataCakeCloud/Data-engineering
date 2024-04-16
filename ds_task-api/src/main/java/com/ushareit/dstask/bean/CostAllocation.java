package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuebotao
 * @date 2022/12/13
 */
@Data
@Entity
@Builder
@Table(name = "cost_allocation")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("组类")
public class CostAllocation extends DeleteEntity {

    private Integer taskId;

    private Integer groupId;

    private BigDecimal value;

    @Transient
    private String name ;



}
