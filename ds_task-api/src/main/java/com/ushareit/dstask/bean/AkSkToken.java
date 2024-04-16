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
import java.sql.Timestamp;

/**
 * @author tianxu
 * @date 2023/4/19 16:02
 **/
@Data
@Entity
@Builder
@Table(name = "aksk_token")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("aksk_token")
public class AkSkToken extends BaseEntity {

    private String token;

    @Column(name = "tenant_name")
    private String tenantName;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "user_name")
    private String userName;

    private Timestamp expiration;

    @Column(name = "is_admin")
    private Integer isAdmin;

    @Column(name = "effective_time")
    private Integer EffectiveTime;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_time")
    private Timestamp createTime;
}
