package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.FileManager;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.FileManagerService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "文件管理")
@RestController
@RequestMapping("/filemanager")
public class FileManagerController extends BaseBusinessController<FileManager> {

    @Autowired
    private FileManagerService fileManagerService;

    @Override
    public BaseService<FileManager> getBaseService() {
        return fileManagerService;
    }

    @ApiOperation(value = "获取某个文件夹的子文件和子文件夹")
    @GetMapping("/subset/list")
    public BaseResponse subsetList(@RequestParam("module") String module,
                                   @RequestParam("id") Integer id) throws Exception {
        Map<String, Object> stringObjectMap = fileManagerService.listFolderSubset(module, id);
        return BaseResponse.success(stringObjectMap);
    }

    @ApiOperation(value = "模糊查找文件及文件夹")
    @GetMapping("/search")
    public BaseResponse search(@RequestParam("module") String module,
                               @RequestParam("name") String name) throws Exception {
        return BaseResponse.success(fileManagerService.searchByName(module, name));
    }

    @ApiOperation(value = "添加文件/文件夹")
    @PostMapping("/add")
    public BaseResponse add(@RequestBody @Valid FileManager fm) {
        fm.setUserGroup(InfTraceContextHolder.get().getUuid());
        fileManagerService.add(fm);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "批量添加添加文件/文件夹")
    @PostMapping("/batch/add")
    public BaseResponse add(@RequestBody @Valid List<FileManager> fms) {
        for (FileManager fm:fms){
            fm.setUserGroup(InfTraceContextHolder.get().getUuid());
        }
        fileManagerService.batchAdd(fms);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }


    @ApiOperation(value = "删除文件夹")
    @PutMapping("/delete")
    public BaseResponse delete(@RequestParam("id") Integer id) throws Exception {
        fileManagerService.delete(id);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "移动文件")
    @PutMapping("/move")
    public BaseResponse move(@RequestParam("id") Integer id,
                             @RequestParam("oldParentId") Integer oldParentId,
                             @RequestParam("newParentId") Integer newParentId) throws Exception {
        fileManagerService.move(id,oldParentId,newParentId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "改名")
    @PutMapping("/changeName")
    public BaseResponse changeName(@RequestParam("id") Integer id,
                                   @RequestParam("newName") String newName,
                                   @RequestParam("module") String module,
                                   @RequestParam(value = "entityId", required = false) Integer entityId) throws Exception {
        fileManagerService.changeName(id, newName, module, entityId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "刷新文件夹")
    @PutMapping("/fresh")
    public BaseResponse changeName(@RequestParam("ids") String folderIds) throws Exception {
        return BaseResponse.success(fileManagerService.fresh(folderIds));
    }

    @ApiOperation(value = "获取某个文件或文件夹的父文件夹链路")
    @GetMapping("/pfolder/list")
    public BaseResponse pfolderList(@RequestParam("id") Integer id) throws Exception {
        return BaseResponse.success(fileManagerService.pfolderList(id));
    }
}
