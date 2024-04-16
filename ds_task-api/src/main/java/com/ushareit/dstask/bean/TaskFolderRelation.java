package com.ushareit.dstask.bean;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 已经废弃，新的文件管理使用FileManager
 */
@Data
@Table(name = "task_folder_relation")
public class TaskFolderRelation {
    /**
     * id是主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer taskFolderId;
    private Integer taskId;
    private String taskName;
}
