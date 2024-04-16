package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.FileManager;

import java.util.List;
import java.util.Map;


public interface FileManagerService extends BaseService<FileManager> {
    void add(FileManager fm);

    void delete(Integer id);

    void move(Integer id,Integer oldParentId,Integer newParentId);

    void changeName(Integer id,String name,String module,Integer entityId);

    Map<String,Object> listFolderSubset(String module, Integer id);

    Map<String,Object> searchByName(String module, String name);

    void deleteByEntityId(Integer entityId,String module);

    void changeNameByEntityId(Integer entityId,String module,String newName);

    void batchAdd(List<FileManager> fms);

    Map<String,Object> fresh(String ids);

    Map<String,Object> pfolderList(Integer id);

    List<Integer> getEntityIdContainedInFolder(Integer folderId,String name);
}
