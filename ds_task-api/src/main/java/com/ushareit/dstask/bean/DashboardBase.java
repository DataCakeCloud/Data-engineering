package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("看板类")
public class DashboardBase extends BaseEntity{
    @ApiModelProperty(value = "tableau看板id")
    private Integer viewId;

    @ApiModelProperty(value = "部门全路径")
    private List<DashboardParams> params;

    @Data
    public static  class DashboardParams{
        private String key;

        private String value;
    }
}


