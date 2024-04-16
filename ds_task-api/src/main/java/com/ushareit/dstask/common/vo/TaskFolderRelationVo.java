package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.TaskFolderRelation;
import lombok.Data;

import java.util.List;

@Data
public class TaskFolderRelationVo {
    private Integer folderId;
    private List<TaskFolderRelation> taskIds;
}
