package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.FileManager;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.FileManagerMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.FileManagerService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileManagerServiceImpl  extends AbstractBaseServiceImpl<FileManager> implements FileManagerService {
    @Resource
    private FileManagerMapper fileManagerMapper;

    @Resource
    private TaskServiceImpl taskService;

    @Override
    public CrudMapper<FileManager> getBaseMapper() {
        return fileManagerMapper;
    }


    @Override
    public void add(FileManager fm) {
        if (!FileManager.checkModule(fm.getModule())) {
            // 检查一下module是否正确
            throw new ServiceException(BaseResponseCodeEnum.FM_MODULE_ERROR.name(),
                    String.format(BaseResponseCodeEnum.FM_MODULE_ERROR.getMessage(), fm.getModule()));
        }

        FileManager fmFromDb = fileManagerMapper.selectByEntityId(fm.getEntityId(),fm.getModule());
        if(fmFromDb != null){
            // 已经添加过文件的不能再次添加
            return;
        }
        super.save(fm);
        if(!fm.getIsFolder()){
            // 递归增加父文件夹的fileNum
            recursiveIncreaseFileNums(fm.getParentId(),1);
        }
    }

    @Override
    public void batchAdd(List<FileManager> fms) {
        int cnt = 0;
        int parentId = 0;
        for(FileManager fm:fms){
            FileManager fmFromDb = fileManagerMapper.selectByEntityId(fm.getEntityId(),fm.getModule());
            if(fmFromDb != null){
                // 已经添加过文件的不能再次添加
                continue;
            }
            // 批次添加限制了所有的文件添加到一个目录下
            parentId = fm.getParentId();
            super.save(fm);
            if(!fm.getIsFolder()){
                cnt += 1;
            }
        }
        // 递归增加父文件夹的fileNum
        recursiveIncreaseFileNums(parentId,cnt);
    }

    public void recursiveIncreaseFileNums(Integer parentId,Integer cnt){
        if(parentId != 0){
            fileManagerMapper.increaseFileNums(parentId,cnt);
            recursiveIncreaseFileNums(fileManagerMapper.selectParentId(parentId),cnt);
        }
    }

    public void recursiveDecreaseFileNums(Integer parentId,Integer cnt){
        if(parentId != 0){
            fileManagerMapper.decreaseFileNums(parentId,cnt);
            recursiveDecreaseFileNums(fileManagerMapper.selectParentId(parentId),cnt);
        }
    }

    @Override
    public void delete(Integer id) {
        FileManager fm = fileManagerMapper.selectById(id);
        if (fm.getParentId() == 0) {
            // 不能删除根目录
            throw new ServiceException(BaseResponseCodeEnum.FM_CANT_DEL_ROOT_FOLDER);
        }
        if(!fm.getIsFolder()){
            super.delete(id);
            recursiveDecreaseFileNums(fm.getParentId(),1);
        }else{
            // 只能删除没有子文件的文件夹
            if(fm.getFileNums() == 0){
                recursiveDeleteChild(fm.getId());
            } else {
                // 无法直接删除包含子文件的文件夹
                throw new ServiceException(BaseResponseCodeEnum.FM_CANT_DEL_CONTAINS_SUB_FOLDER);
            }
        }
    }

    public void recursiveDeleteChild(Integer curId){
        List<FileManager> childrenFms = fileManagerMapper.selectChildren(curId);
        super.delete(curId);
        if (childrenFms != null) {
            for (FileManager fm : childrenFms) {
                recursiveDeleteChild(fm.getId());
            }
        }
    }


    @Override
    public void move(Integer id, Integer oldParentId, Integer newParentId) {
        if(oldParentId.equals(newParentId) || id.equals(newParentId)){
            return;
        }
        FileManager fm = fileManagerMapper.selectById(id);
        if (!fm.getIsFolder()) {
            fm.setParentId(newParentId);
            super.update(fm);
            recursiveDecreaseFileNums(oldParentId, 1);
            recursiveIncreaseFileNums(newParentId, 1);
        } else {
            // 校验，文件夹一定不能移动到其子文件夹中，否则会出现环，产生恐怖的结果
            FileManager newFm = fileManagerMapper.selectById(newParentId);
            while (newFm.getParentId() != 0) {
                if (newFm.getParentId().equals(id)) {
                    throw new ServiceException(BaseResponseCodeEnum.FM_MOVE_CYCLE_ERROR);
                }
                newFm = fileManagerMapper.selectById(newFm.getParentId());
            }

            Integer fileNums = moveLock(id, newParentId);
            recursiveDecreaseFileNums(oldParentId, fileNums);
            recursiveIncreaseFileNums(newParentId, fileNums);
        }
    }

    @Transactional
    public Integer moveLock(Integer id, Integer newParentId){
        FileManager fm = fileManagerMapper.selectByIdForUpdate(id);
        fm.setParentId(newParentId);
        super.update(fm);
        return fm.getFileNums();
    }

    @Override
    public void changeName(Integer id, String name, String module, Integer entityId) {
        FileManager fm = fileManagerMapper.selectById(id);
        if (fm.getIsFolder()) {
            fm.setName(name);
            super.update(fm);
        } else {
            if (module.equals(FileManager.Module.TASK.name())) {
                taskService.changeName(entityId, name);
                // 现在只有task可以修改名称
                fm.setName(name);
                super.update(fm);
            }
        }
    }

    @Override
    public Map<String, Object> listFolderSubset(String module, Integer id) {
        List<FileManager> fileManagers;

        if (id == 0) {
            // 查找根目录
            fileManagers = fileManagerMapper.selectRootFolder(module,InfTraceContextHolder.get().getUuid());
        } else {
            fileManagers = fileManagerMapper.selectFolderSubset(id);
        }

        return assemblyResult(fileManagers);
    }

    private Map<String, Object> assemblyResult(List<FileManager> fileManagers){
        // 后面的代码都是为了组装最终的结果
        List<Map<String, Object>> entityList = new ArrayList<>();

        // 使用 Collator 进行中文拼音排序
        Collator collator = Collator.getInstance(Locale.CHINA);
        // 文件夹排到前面,然后按照name排序
        fileManagers.sort(Comparator.comparing(FileManager::getIsFolder,Comparator.reverseOrder())
                .thenComparing(FileManager::getName,collator));

        for (FileManager fm : fileManagers) {
            Map<String, Object> fmMap = createFileManagerMap(fm);
            entityList.add(fmMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("entityList", entityList);

        return result;

    }

    private Map<String, Object> createFileManagerMap(FileManager fm) {
        Map<String, Object> fmMap = new HashMap<>();
        fmMap.put("id", fm.getId());
        fmMap.put("name", fm.getName());
        fmMap.put("parentId", fm.getParentId());
        fmMap.put("fileNums", fm.getFileNums());
        fmMap.put("entityId", fm.getEntityId());
        fmMap.put("leaf", !fm.getIsFolder());
        return fmMap;
    }

    @Override
    public Map<String, Object> searchByName(String module, String name) {
        List<FileManager> fms = fileManagerMapper.selectByNameLike(name, module, InfTraceContextHolder.get().getUuid());
        Map<Integer, FileManager> cache = new HashMap<>();

        for (FileManager fm : fms) {
            // 取出来的文件夹，它们的fileNums都是有值的，这里初始化一下都置为0
            fm.setFileNums(0);
            cache.put(fm.getId(), fm);
        }

        for (FileManager fm : fms) {
            // 开始递归遍历parent
            recursiveTraversalParentNodes(fm, cache);
        }
        List<FileManager> resList = new ArrayList<>(cache.values());
        return assemblyResult(resList);
    }

    @Override
    public void deleteByEntityId(Integer entityId, String module) {
        FileManager fm = fileManagerMapper.selectByEntityId(entityId, module);
        if (fm != null) {
            super.delete(fm.getId());
            recursiveDecreaseFileNums(fm.getParentId(), 1);
        }
    }

    @Override
    public void changeNameByEntityId(Integer entityId, String module, String newName) {
        FileManager fm = fileManagerMapper.selectByEntityId(entityId, module);
        fm.setName(newName);
        super.update(fm);
    }

    @Override
    public Map<String, Object> fresh(String ids) {
        List<Integer> idList = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        List<FileManager> fms = fileManagerMapper.selectByIdList(idList);

        return assemblyResult(fms);
    }

    @Override
    public Map<String, Object> pfolderList(Integer id) {
        FileManager curFm = fileManagerMapper.selectById(id);
        Map<String, Object> res = new HashMap<>();
        List<FileManager> pfmList = new ArrayList<>();
        res.put("pfolderList", pfmList);
        if (curFm == null) {
            return res;
        }
        if (curFm.getIsFolder()) {
            pfmList.add(curFm);
        }
        while (curFm.getParentId() != 0) {
            curFm = fileManagerMapper.selectById(curFm.getParentId());
            pfmList.add(curFm);
        }
        Collections.reverse(pfmList);
        return res;
    }

    /**
     * 递归的获取fm的上游，且计算出每个文件夹在整个递归中包含的所有子文件数。
     * 所有子文件数指的是子文件加上所有子文件夹的子文件数
     *
     * 记数的实现：
     * 递归中会出现---1.从文件开始递归 2.从文件夹开始递归 两种情况。
     * 记数是对文件记数，所以只有从文件开始递归时，要开始对搜所有的父节点进行累加、
     * @param fm
     * @param cache
     */
    public void recursiveTraversalParentNodes(FileManager fm, Map<Integer, FileManager> cache) {
        int parentId = fm.getParentId();
        if (parentId == 0) {
            return;
        }
        if (fm.getIsFolder()) {
            // 如果当前的fm是个文件夹的话，且它的父目录不存在于cache中，我们才需要递归
            // （如果已经存在于cache中，说明父目录已经被递归过一次了，且文件夹递归不需要记数。）
            if (!cache.containsKey(parentId)) {
                FileManager parentFm = cache.computeIfAbsent(parentId, id -> {
                    FileManager newParent = fileManagerMapper.selectById(id);
                    if(newParent!=null){
                        // 理论上parent是不可能为null的，除非有人从数据库里把数据删除了
                        newParent.setFileNums(0);  // 初始化fileNums为0
                    }
                    return newParent;
                });
                if(parentFm != null){
                    recursiveTraversalParentNodes(parentFm, cache);
                }

            }
        } else {
            // 如果当前的fm是个文件的话，我们需要递归的增加其父目录的fileNums，所以进入另一个递归函数
            recursiveIncreaseParentNodeFileNums(fm, cache);
        }
    }

    /**
     *  参考函数recursiveIncreaseParentNodeFileNums的注释。
     *  此函数实现的就是从文件开始递归的情况，是需要对父节点进行累加的。
     * @param fm
     * @param cache
     */
    public void recursiveIncreaseParentNodeFileNums(FileManager fm, Map<Integer, FileManager> cache) {
        int parentId = fm.getParentId();
        if (parentId != 0) {
            FileManager parentFm = cache.computeIfAbsent(parentId, id -> {
                FileManager newParent = fileManagerMapper.selectById(id);
                if(newParent!=null){
                    // 理论上parent是不可能为null的，除非有人从数据库里把数据删除了
                    newParent.setFileNums(0);  // 初始化fileNums为0
                }
                return newParent;
            });
            if(parentFm != null){
                parentFm.setFileNums(parentFm.getFileNums() + 1);
                recursiveIncreaseParentNodeFileNums(parentFm, cache);
            }
        }
    }

    @Override
    public List<Integer> getEntityIdContainedInFolder(Integer folderId, String name) {
        List<FileManager> childFm = fileManagerMapper.selectChildren(folderId);
        List<Integer> entityIdList = new ArrayList<>();

        if (childFm != null) {
            if (StringUtils.isNotBlank(name)) {
                childFm.removeIf(fm -> !fm.getName().toLowerCase().contains(name.toLowerCase()));
            }

            childFm.forEach(fm -> entityIdList.add(fm.getEntityId()));
        }

        return entityIdList;
    }
}
