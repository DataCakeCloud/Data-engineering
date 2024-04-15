package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.bean.TaskInstance;
import com.ushareit.dstask.bean.TaskSnapshot;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface TaskSnapshotService extends BaseService<TaskSnapshot> {
    /**
     * 手动触发保存点
     *
     * @param taskSnapshot
     * @return
     */
    void trigger(TaskSnapshot taskSnapshot);

    /**
     * 终止并保存savepoint
     *
     * @param taskInstance
     * @return
     */
    void stopWithSavepoint(TaskInstance taskInstance);

    /**
     * 查询savepoint和checkpoint列表
     *
     * @param taskSnapshot
     * @return
     */
    Map<String, Object> list(TaskSnapshot taskSnapshot);


    /**
     * 查询最新的checkpoint
     *
     * @param taskid
     * @return
     */
    ObsS3Object getLatestCheckpoint(Integer taskid);

    /**
     *
     * @param taskid
     * @return
     */
    List<TaskSnapshot> getCheckpoints(Integer taskid);
}
