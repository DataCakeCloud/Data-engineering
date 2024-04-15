package com.ushareit.dstask.third.sharestore.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2023/2/17
 */
@Data
public class SimpleFieldVO {

    @JsonProperty("NUM_PARTITIONS")
    private String numPartition;

}
