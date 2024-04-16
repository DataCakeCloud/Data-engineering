package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.List;

/**
 * @author tianxu
 * @date 2023/12/25
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "dataAudit")
public class DataAudit {
    private String catalogName;
    private String database;
    private String table;
    private List<String> operations;
    private String nextMarker;
    private Long startTime;
    private Long endTime;
    private String userId;
}
