package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/2/24
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("血缘关系类")
public class SourceTarget {
    private String source;
    private String target;
}
