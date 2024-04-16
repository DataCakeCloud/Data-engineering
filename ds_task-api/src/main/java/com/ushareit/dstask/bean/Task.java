package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("任务类")
public class Task extends TaskBase {

    @Column(name = "status_code")
    private String statusCode;

    @Transient
    private String displayStatus;

    @Transient
    private Boolean canEdit = false;

    /**
     * 给workflow用
     */
    @Transient
    @JSONField(serialzeFeatures = {
            SerializerFeature.DisableCircularReferenceDetect
    })
    private TaskInstance taskInstance;

    @Transient
    private Boolean canDelete = false;
    private List<TaskInstance> taskInstances;

    @Column(name = "delete_status")
    private Integer deleteStatus;

    @Column(name = "current_version")
    private Integer currentVersion;

    //自动伸缩重启时间
    @Column(name = "restart_time")
    private Timestamp restartTime;

    @Transient
    private String pathType = "table";

    @Transient
    private Boolean invokingStatus = true;

    @Transient
    private String auditStatus;

    @Transient
    private String sparkConfParam;

    @Transient
    private Map<String, String[]> args;

    @Transient
    private String callbackUrl;

    @Transient
    private Boolean realtimeExecute = false;

    @Transient
    private String md5Sql;

    @Transient
    private Integer isIrregularSheduler = 1;

    @Transient
    private Integer folderId;

    @Transient
    private String userGroups ;

    public Task(TaskBase taskBase) {
        super.copy(taskBase);
    }

    public static Task cloneByTaskVersion(TaskVersion taskVersion) {
        Task task = new Task(taskVersion);
        task.setId(taskVersion.getTaskId());
        task.setCurrentVersion(taskVersion.getVersion());
        task.setCanEdit(taskVersion.getCanEdit());
        return task;
    }

    @Override
    public Task clone() {
        Task app = null;
        try {
            app = (Task) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(this.getId(), task.getId());
    }
}
