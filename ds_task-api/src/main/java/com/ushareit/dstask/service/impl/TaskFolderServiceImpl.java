package com.ushareit.dstask.service.impl;

import com.google.common.collect.Lists;
import com.ushareit.dstask.bean.TaskFolderRelation;
import com.ushareit.dstask.bean.TaskFolder;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.vo.TaskFolderRelationVo;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskFolderMapper;
import com.ushareit.dstask.mapper.TaskFolderRelationMapper;
import com.ushareit.dstask.mapper.UserGroupMapper;
import com.ushareit.dstask.service.TaskFolderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TaskFolderServiceImpl implements TaskFolderService {

    @Resource
    private TaskFolderMapper taskFolderMapper;

    @Resource
    private TaskFolderRelationMapper taskFolderRelationMapper;


    @Override
    public List<TaskFolder> list() {
        return taskFolderMapper.selectAll();
    }

    @Override
    public List<TaskFolderRelation> listByFolderId(Integer folderId) {
        return taskFolderRelationMapper.selectByFolderId(folderId);
    }

    @Override
    public void save(TaskFolder taskFolder) {
        List<TaskFolder> taskFolderList=taskFolderMapper.selectByNameAndParentId(taskFolder);
        if (CollectionUtils.isNotEmpty(taskFolderList)){
            throw new ServiceException(BaseResponseCodeEnum.TASKFOLDER_EXIST, BaseResponseCodeEnum.TASKFOLDER_EXIST.getMessage());
        }
        taskFolderMapper.insert(taskFolder);
    }

    @Override
    public void edit(TaskFolder taskFolder) {
        List<TaskFolder> taskFolderList=taskFolderMapper.selectByNameAndParentId(taskFolder);
        if (CollectionUtils.isNotEmpty(taskFolderList)&&taskFolderList.size()>1){
            throw new ServiceException(BaseResponseCodeEnum.TASKFOLDER_EXIST, BaseResponseCodeEnum.TASKFOLDER_EXIST.getMessage());
        }
        taskFolderMapper.updateByPrimaryKey(taskFolder);
    }

    @Override
    public void addTask(TaskFolderRelationVo taskFolderRelationVo) {
        if (taskFolderRelationVo.getFolderId()!=null&&CollectionUtils.isNotEmpty(taskFolderRelationVo.getTaskIds())){
            for (TaskFolderRelation taskFolderRelation:taskFolderRelationVo.getTaskIds()){
                TaskFolderRelation t=new TaskFolderRelation();
                t.setTaskFolderId(taskFolderRelationVo.getFolderId());
                t.setTaskId(taskFolderRelation.getTaskId());
                t.setTaskName(taskFolderRelation.getTaskName());
                taskFolderRelationMapper.insert(t);
            }
        }
    }

    @Override
    public void updateTask(TaskFolderRelationVo taskFolderRelationVo) {
        taskFolderRelationMapper.deleteByFolderIds(Lists.newArrayList(taskFolderRelationVo.getFolderId().intValue()+""));
        addTask(taskFolderRelationVo);
    }

    @Override
    public void deleteFolder(String ids) {
        if (StringUtils.isNoneBlank(ids)){
            taskFolderMapper.deleteByIds(ids);
            taskFolderRelationMapper.deleteByFolderIds(Arrays.asList(ids.split(",")));
        }
    }

    @Override
    public void deleteTask(String ids) {
        taskFolderRelationMapper.deleteByIds(ids);
    }

}
