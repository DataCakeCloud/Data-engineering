package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * @author wuyan
 * @date 2021/8/26
 */
@Data
@Entity
@Builder
@Table(name = "hive_tables")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("hive_tables类")
public class HiveTable extends BaseEntity{
    @ApiModelProperty("表路径")
    @Column(name = "path")
    private String path;

    @ApiModelProperty("区域")
    @Column(name = "region")
    private String region;

    @ApiModelProperty("provider")
    @Column(name = "provider")
    private String provider;

    @ApiModelProperty("模式")
    @Column(name = "mode")
    private String mode;

    @ApiModelProperty("表名")
    @Column(name = "name")
    private String name;

    @ApiModelProperty("删除状态")
    @Column(name = "delete_status")
    private Integer deleteStatus;

    @Column(name = "CREATE_BY")
    private String createBy;

    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @ApiModelProperty("数据库")
    @Column(name = "`database`")
    private String database;

    @ApiModelProperty("所属任务ID")
    @Column(name = "task_id")
    private Integer taskId;

    @ApiModelProperty("catalog")
    @Column(name = "catalog")
    private String catalog;
}
