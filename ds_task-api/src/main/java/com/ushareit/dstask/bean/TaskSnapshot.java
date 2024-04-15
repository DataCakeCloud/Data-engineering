package com.ushareit.dstask.bean;

import com.ushareit.dstask.constant.DsTaskConstant;
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
 * @date 2020/5/22
 */
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "task_snapshot")
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("保存点封装类")
public class TaskSnapshot extends DeleteEntity {
    @Column(name = "name")
    private String name;

    @ApiModelProperty("任务id")
    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "trigger_kind")
    private String triggerKind;

    @Column(name = "url")
    private String url;

    public static TaskSnapshot CreateLatestState(TaskSnapshot taskSnapshot) {
        TaskSnapshot latestState = TaskSnapshot.builder().taskId(taskSnapshot.taskId).name(DsTaskConstant.TASK_RESTORE_STRATEGY_LATEST_STATE).build();
        latestState.setId(-1);

        return latestState;
    }

    public static TaskSnapshot convert(ObsS3Object o) {
        TaskSnapshot taskSnapshot = TaskSnapshot.builder().name(o.getName()).url(o.getPath()).build();
        taskSnapshot.setCreateTime(new Timestamp(o.getLastModified().getTime()))
                .setUpdateTime(new Timestamp(o.getLastModified().getTime()))
                .setCreateBy("monitor")
                .setUpdateBy("monitor");
        return taskSnapshot;
    }
}