package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;


@Data
@Entity
@Builder
@Table(name = "lock_info")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("分布式锁类")
public class JDBCLock extends BaseEntity {

    public final static Integer LOCKED_STATUS = 1;
    public final static Integer UNLOCKED_STATUS = 0;
    private static final long serialVersionUID = 4361697543225005899L;

    /**
     * 锁的标识，以任务为例，可以锁任务名称
     */
    @Column(name = "tag", nullable = false)
    private String tag;

    private String hostname;

    /**
     * 过期时间
     */
    @Column(name = "expirationTime", nullable = false)
    private Timestamp expirationTime;

    /**
     * 锁状态，0，未锁，1，已经上锁
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

}
