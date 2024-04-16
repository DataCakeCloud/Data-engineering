package com.ushareit.dstask.web.factory.flink.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2021/8/6
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithInfo {
    /**
     * for metis
     */
    public static final String METIS_CONNECTOR = "metis";
    public static final String PROPS_AUTO_OFFSET_RESET_LATEST = "latest";
    public static final String PROPS_AUTO_OFFSET_RESET_EARLIEST = "earliest";
    private String project;
    private String logStore;
    private String region;
    private String group;


    /**
     * for mysql-cdc
     */
}
