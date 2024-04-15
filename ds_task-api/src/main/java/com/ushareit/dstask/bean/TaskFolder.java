package com.ushareit.dstask.bean;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 已经废弃，新的文件管理使用FileManager
 */
@Data
@Table(name = "task_folder")
public class TaskFolder {
    /**
     * id是主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer parentId;
    private String uuid;//用户组id
    private Integer type;
}
