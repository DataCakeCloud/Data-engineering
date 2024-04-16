package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;


@Data
@Builder
@Table(name = "file_manager")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileManager extends BaseEntity{
    @Column(name = "is_folder")
    private Boolean isFolder;

    @Column(name = "entity_id")
    private Integer entityId;

    private String name;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "file_nums")
    private Integer fileNums;

    private String module;

    @Column(name = "user_group")
    private String userGroup;

    /**
     * 模块类型的定义
     */
    public enum Module {
        TASK,  //任务模块
        ARTIFACT //工件模块
    }

    public static Boolean checkModule(String module) {
        for (Module m : Module.values()) {
            if (module.equals(m.name())) {
                return true;
            }
        }
        return false;
    }
}


