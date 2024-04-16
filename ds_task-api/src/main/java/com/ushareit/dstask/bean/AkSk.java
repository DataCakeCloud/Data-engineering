package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
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

/**
 * @author tianxu
 * @date 2023/4/18 17:50
 **/
@Data
@Entity
@Builder
@Table(name = "aksk")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("aksk")
public class AkSk extends OperatorEntity {
    private String ak;

    private String sk;

    private Integer valid;//是否有效

    @Column(name = "tenant_name")
    private String tenantName;//租户名称

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "apply_user_id")
    private String applyUserId;

    @Transient
    private String email;

    @Transient
    private String description;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;
}
