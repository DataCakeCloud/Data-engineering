package com.ushareit.dstask.bean.qe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/9/2
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class QueryTop {
    private Integer id;
    private String uuid;
    private String createTime;
    private Double executeDuration;
    private Double processedBytes;
    private String displayProcessedBytes;
}
