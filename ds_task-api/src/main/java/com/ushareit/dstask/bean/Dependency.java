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
import javax.persistence.Transient;
import java.sql.Timestamp;

/**
 * @author wuyan
 * @date 2022/2/21
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("血缘实例类")
public class Dependency {
    @Transient
    @JSONField(name = "id")
    private String id;

    @Transient
    @JSONField(name = "task_id", alternateNames = "taskId")
    private Integer taskId;

    @Transient
    @JSONField(name = "dag_id", alternateNames = "dagId")
    private String dagId;

    @Transient
    @JSONField(name = "execution_date", alternateNames = "executionDate")
    private Timestamp executionDate;

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
    @JSONField(name = "state")
    private String state;

    @Transient
    @JSONField(name = "table")
    private String table;

    @Transient
    @JSONField(name = "num_of_upstream", alternateNames = "numOfUpstream")
    private String numOfUpstream;

    @Transient
    @JSONField(name = "num_of_downstream", alternateNames = "numOfDownstream")
    private String numOfDownstream;

}
