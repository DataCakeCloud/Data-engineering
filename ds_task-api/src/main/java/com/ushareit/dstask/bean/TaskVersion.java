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
import javax.persistence.Transient;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "task_version")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("任务类")
public class TaskVersion extends TaskBase {
    @ApiModelProperty("目录id")
    @Column(name = "task_id")
    private Integer taskId;

    @ApiModelProperty("版本号")
    private Integer version;

    @Transient
    private String displayVersion;

    @Transient
    private String statusCode;

    @Transient
    private Boolean canEdit = false;

    public TaskVersion(TaskBase taskBase) {
        super.copy(taskBase);
    }

    public static TaskVersion CreateCurrent(Task task){
        TaskVersion current = TaskVersion.builder().taskId(task.getId()).version(-1).displayVersion("Current").build();
        current.copy(task);
        current.setId(-1);
        return current;
    }



}
