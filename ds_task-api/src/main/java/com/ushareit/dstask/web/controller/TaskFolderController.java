package com.ushareit.dstask.web.controller;

import com.google.api.client.util.Lists;
import com.ushareit.dstask.bean.TaskFolder;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.vo.TaskFolderRelationVo;
import com.ushareit.dstask.mapper.UserGroupMapper;
import com.ushareit.dstask.service.TaskFolderService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskfolder")
public class TaskFolderController {

    @Autowired
    private TaskFolderService taskFolderService;

    /**
     * int tyoe   0 是task  1是 文件
     * @return
     */
    @RequestMapping("/listFolder")
    public BaseResponse list(@RequestParam(name = "type",required = false) Integer type){
        if (type==null){
            type=0;
        }
        final int t=type;
        return BaseResponse.success(taskFolderService.list().stream().filter(taskfolder->t==taskfolder.getType())
                .filter(taskFolder -> InfTraceContextHolder.get().getUuid().equals(taskFolder.getUuid())).collect(Collectors.toList()));
    }

    @RequestMapping("/listTask")
    public BaseResponse listTask(Integer folderId){
        return BaseResponse.success(taskFolderService.listByFolderId(folderId));
    }

    @RequestMapping("/saveFolder")
    public BaseResponse saveFolder(@RequestBody TaskFolder taskFolder){
        taskFolder.setUuid(InfTraceContextHolder.get().getUuid());
        taskFolderService.save(taskFolder);
        return BaseResponse.success();
    }

    @RequestMapping("/editFolder")
    public BaseResponse editFolder(@RequestBody TaskFolder taskFolder){
        taskFolder.setUuid(InfTraceContextHolder.get().getUuid());
        taskFolderService.edit(taskFolder);
        return BaseResponse.success();
    }

    @RequestMapping("/addTask")
    public BaseResponse addTask(@RequestBody TaskFolderRelationVo taskFolderRelationVo){
        taskFolderService.addTask(taskFolderRelationVo);
        return BaseResponse.success();
    }

    @RequestMapping("/updateTask")
    public BaseResponse updateTask(@RequestBody TaskFolderRelationVo taskFolderRelationVo){
        taskFolderService.updateTask(taskFolderRelationVo);
        return BaseResponse.success();
    }

    @RequestMapping("/deleteFolder")
    public BaseResponse deleteFolder(String ids){
        taskFolderService.deleteFolder(ids);
        return BaseResponse.success();
    }

    @RequestMapping("/deleteTask")
    public BaseResponse editFolder(String ids){
        taskFolderService.deleteTask(ids);
        return BaseResponse.success();
    }

}
