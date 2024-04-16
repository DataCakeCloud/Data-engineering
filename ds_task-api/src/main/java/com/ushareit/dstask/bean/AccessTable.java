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

/**
 * @author tianxu
 * @date 2023/12/14
 */
@Data
@Entity
@Builder
@Table(name = "access_table")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("表的访问频次")
public class AccessTable extends BaseEntity {

    @ApiModelProperty(value = "表名")
    @Column(name = "table_name")
    private String tableName;

    @ApiModelProperty(value = "库名")
    @Column(name = "database_name")
    private String databaseName;

    @ApiModelProperty(value = "每天访问次数")
    @Column(name = "count")
    private Integer count;

    @ApiModelProperty(value = "用户组")
    @Column(name = "user_group")
    private String userGroup;

    @ApiModelProperty(value = "统计时间")
    @Column(name = "stat_date")
    private String stat_date;

//    @ApiModelProperty(value = "catalog")  # todo 司内可用
//    @Column(name = "catalog_name")
//    private String catalogName;
}