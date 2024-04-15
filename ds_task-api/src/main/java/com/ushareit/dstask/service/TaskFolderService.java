package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.TaskFolderRelation;
import com.ushareit.dstask.bean.TaskFolder;
import com.ushareit.dstask.common.vo.TaskFolderRelationVo;

import java.util.List;

public interface TaskFolderService {

    List<TaskFolder> list();

    List<TaskFolderRelation> listByFolderId(Integer folderId);

    void save(TaskFolder taskFolder);

    void edit(TaskFolder taskFolder);

    void addTask(TaskFolderRelationVo taskFolderRelationVo);

    void updateTask(TaskFolderRelationVo taskFolderRelationVo);

    void deleteFolder(String ids);

    void deleteTask(String ids);
}
