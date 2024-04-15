package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
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
import javax.persistence.Transient;
import java.sql.Timestamp;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "task_instance")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("任务实例类")
public class TaskInstance extends OperatorEntity {
    @ApiModelProperty("目录id")
    @Column(name = "task_id")
    private Integer taskId;

    @ApiModelProperty("任务版本ID")
    @Column(name = "version_id")
    private Integer versionId;

    @ApiModelProperty("集群ID")
    @Column(name = "cluster_id")
    private Integer clusterId;

    @ApiModelProperty("保存点ID")
    @Column(name = "snapshot_id")
    private Integer snapshotId;

    @ApiModelProperty("运行实例ID flinkjob id或者genie id")
    @Column(name = "engine_instance_id")
    private String engineInstanceId;

    @ApiModelProperty("服务UI地址")
    @Column(name = "service_address")
    private String serviceAddress;

    @ApiModelProperty("任务当前状态")
    @Column(name = "status_code")
    private String statusCode;

    @Transient
    private String clusterName;

    @Transient
    private String snapshotName;

    @Transient
    private String name;

    @Transient
    @JSONField(name = "execution_date", alternateNames = "executionDate")
    private Timestamp executionDate;

    @Transient
    @JSONField(name = "try_number", alternateNames = "tryNumber")
    private Integer tryNumber;

    @Transient
    @JSONField(name = "start_date", alternateNames = "startDate")
    private Timestamp startDate;

    @Transient
    @JSONField(name = "end_date", alternateNames = "endDate")
    private Timestamp endDate;

    @Transient
    @JSONField(name = "duration", alternateNames = "duration")
    private String duration;

    @Transient
    @JSONField(name = "state", alternateNames = "state")
    private String state;

    @Transient
    @JSONField(name = "type", alternateNames = "type")
    private String type;

    @Transient
    @JSONField(name = "genie_job_id", alternateNames = "genieJobId")
    private String genieJobId;

    @Transient
    @JSONField(name = "genie_job_url", alternateNames = "genieJobUrl")
    private String genieJobUrl;

    @Transient
    @JSONField(name = "flink_url", alternateNames = "flinkUrl")
    private String flinkUrl;

    @Transient
    @JSONField(name = "version", alternateNames = "version")
    private Integer version = 0;

    @Transient
    @JSONField(name = "is_kyuubi_job", alternateNames = "isKyuubiJob")
    private Boolean isKyuubiJob = false;

    @Transient
    @JSONField(name = "spark_ui", alternateNames = "sparkUi")
    private String spark_ui;
}
