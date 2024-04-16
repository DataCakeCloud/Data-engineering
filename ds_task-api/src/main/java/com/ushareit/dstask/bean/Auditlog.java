package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;

@Data
@Entity
@Builder
@Accessors(chain = true)
@Table(name = "auditlog")
@AllArgsConstructor
@NoArgsConstructor
public class Auditlog extends BaseEntity {
    @Column(name = "module")
    private String module;

    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "event_version")
    private Integer eventVersion;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "event_message")
    private String eventMessage;

    @Column(name = "event_snapshot")
    private String eventSnapshot;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Transient
    private boolean currentVersion = false;

}