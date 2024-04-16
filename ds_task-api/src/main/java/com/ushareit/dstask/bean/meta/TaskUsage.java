package com.ushareit.dstask.bean.meta;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/8/26
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("taskusage")
public class TaskUsage {
    private String dt;
    private Double num;
}
